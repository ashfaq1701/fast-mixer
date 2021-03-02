//
// Created by asalehin on 7/9/20.
//

#include <oboe/Oboe.h>

#include <cassert>
#include <utility>
#include "../jvm_env.h"
#include "RecordingEngine.h"
#include "../logging_macros.h"

RecordingEngine::RecordingEngine(
        string recordingFileDir,
        bool recordingScreenViewModelPassed): mRecordingFileDir(move(recordingFileDir)) {

    assert(RecordingStreamConstants::mInputChannelCount == RecordingStreamConstants::mOutputChannelCount);
    auto recordingFilePath = mRecordingFileDir + "/recording.wav";

    mRecordingIO.setRecordingFilePath(recordingFilePath);
    mRecordingIO.setStopPlaybackCallback([&] () {
        setStopPlayback();
    });
    mRecordingScreenViewModelPassed = recordingScreenViewModelPassed;

    mSourceMapStore = SourceMapStore::getInstance();
}

RecordingEngine::~RecordingEngine() {
    stopRecording();
    stopLivePlayback();
    stopPlayback();
}

void RecordingEngine::startLivePlayback() {
    lock_guard<mutex> lock(livePlaybackStreamMtx);
    LOGD(TAG, "startLivePlayback(): ");
    mRecordingIO.sync_live_playback();
    livePlaybackStream.startStream();
}

void RecordingEngine::stopLivePlayback() {
    lock_guard<mutex> lock(livePlaybackStreamMtx);
    LOGD(TAG, "stopLivePlayback(): %d");

    if (livePlaybackStream.mStream) {
        if (livePlaybackStream.mStream->getState() != oboe::StreamState::Closed) {
            livePlaybackStream.stopStream();
        } else {
            livePlaybackStream.resetStream();
        }
    }
}

bool RecordingEngine::startPlayback(int fd) {
    lock_guard<mutex> lock(playbackStreamMtx);
    LOGD(TAG, "startPlayback(): ");

    return startPlaybackCallable(fd);
}

bool RecordingEngine::startPlaybackWithMixingTracks(int fd) {
    lock_guard<mutex> lock(playbackStreamMtx);
    LOGD(TAG, "startPlaybackWithMixingTracks(): ");

    return startPlaybackWithMixingTracksCallable(fd);
}

void RecordingEngine::startPlayingWithMixingTracksWithoutSetup() {
    lock_guard<mutex> lock(playbackStreamMtx);
    LOGD(TAG, "startPlayingWithMixingTracksWithoutSetup(): ");

    if (!playbackStream.mStream) {
        if (playbackStream.openStream() != oboe::Result::OK) {
            return;
        }
    }

    mRecordingIO.setPlaybackPlaying(true);
    playbackStream.startStream();
}

bool RecordingEngine::startPlaybackCallable(int fd) {
    if (!playbackStream.mStream) {
        if (playbackStream.openStream() != oboe::Result::OK) {
            return false;
        }
    }

    mDataSource = shared_ptr<FileDataSource> {
            move(mRecordingIO.setup_audio_source(fd))
    };

    if (mDataSource) {
        mRecordingIO.clearPlayerSources();
        mRecordingIO.add_source_to_player(mDataSource);
        return playbackStream.startStream() == oboe::Result::OK;
    }

    return false;
}

bool RecordingEngine::startPlaybackWithMixingTracksCallable(int fd) {
    if (!playbackStream.mStream) {
        if (playbackStream.openStream() != oboe::Result::OK) {
            return false;
        }
    }

    mDataSource = shared_ptr<FileDataSource> {
            move(mRecordingIO.setup_audio_source(fd))
    };

    map<string, shared_ptr<DataSource>> sourceMap;
    for (auto it = mSourceMapStore->sourceMap.begin(); it != mSourceMapStore->sourceMap.end(); it++) {
        sourceMap.insert(pair<string, shared_ptr<DataSource>>(it->first, it->second));
    }

    mRecordingIO.clearPlayerSources();

    if (mDataSource) {
        mRecordingIO.addSourceMapWithRecordedSource(sourceMap, mDataSource);
    } else {
        mRecordingIO.addSourceMap(sourceMap);
    }

    if (sourceMap.size() == 0 && mDataSource == nullptr) {
        return false;
    }

    mRecordingIO.setPlaybackPlaying(true);
    return playbackStream.startStream() == oboe::Result::OK;
}

bool RecordingEngine::startMixingTracksPlayback(int fd) {
    lock_guard<mutex> lock(playbackStreamMtx);
    LOGD(TAG, "startMixingTracksPlayback(): ");

    return startMixingTracksPlaybackCallable(fd);
}

bool RecordingEngine::startMixingTracksPlaybackCallable(int fd) {
    if (mSourceMapStore->sourceMap.size() == 0) return false;

    if (!playbackStream.mStream) {
        if (playbackStream.openStream() != oboe::Result::OK) {
            return false;
        }
    }

    map<string, shared_ptr<DataSource>> sourceMap;
    for (auto it = mSourceMapStore->sourceMap.begin(); it != mSourceMapStore->sourceMap.end(); it++) {
        sourceMap.insert(pair<string, shared_ptr<DataSource>>(it->first, it->second));
    }

    bool checkResult = mRecordingIO.checkPlayerSources(sourceMap);
    if (!checkResult) {
        mRecordingIO.clearPlayerSources();
        mRecordingIO.addSourceMap(sourceMap);
    }

    mDataSource = shared_ptr<FileDataSource> {
            move(mRecordingIO.setup_audio_source(fd))
    };

    bakPlayHead = mRecordingIO.getCurrentPlaybackProgress();

    bool shouldPlay = true;
    if (mDataSource) {
        if (mDataSource->getSampleSize() < mRecordingIO.getPlayerMaxTotalSourceFrames()) {
            mRecordingIO.setPlayHead(mDataSource->getSampleSize());
        } else {
            shouldPlay = false;
        }
    } else {
        mRecordingIO.setPlayHead(0);
    }

    if (shouldPlay) {
        mRecordingIO.setPlaybackPlaying(true);
        return playbackStream.startStream() == oboe::Result::OK;
    }

    return false;
}

void RecordingEngine::stopMixingTracksPlayback() {
    lock_guard<mutex> lock(playbackStreamMtx);
    LOGD(TAG, "stopMixingTracksPlayback()");
    mRecordingIO.setPlaybackPlaying(false);
    closePlaybackStream();
    mRecordingIO.setPlayHead(bakPlayHead);
}

void RecordingEngine::stopAndResetPlayback() {
    lock_guard<mutex> lock(playbackStreamMtx);
    LOGD(TAG, "stopAndResetPlayback()");
    mRecordingIO.setPlaybackPlaying(false);
    closePlaybackStream();
}

void RecordingEngine::stopPlayback() {
    lock_guard<mutex> lock(playbackStreamMtx);
    LOGD(TAG, "startPlayback()");

    stopPlaybackCallable();
}

void RecordingEngine::stopPlaybackCallable() {
    mRecordingIO.setPlaybackPlaying(false);
    closePlaybackStream();
}

void RecordingEngine::closePlaybackStream() {
    if (playbackStream.mStream) {
        if (playbackStream.mStream->getState() != oboe::StreamState::Closed) {
            playbackStream.stopStream();
        } else {
            playbackStream.resetStream();
        }
    }
}

void RecordingEngine::pausePlayback() {
    lock_guard<mutex> lock(playbackStreamMtx);
    LOGD(TAG, "pausePlayback(): ");
    mRecordingIO.setPlaybackPlaying(false);
    playbackStream.stopStream();
}

void RecordingEngine::startRecording() {
    lock_guard<mutex> lock(recordingStreamMtx);
    LOGD(TAG, "startRecording(): ");
    recordingStream.startStream();
}

void RecordingEngine::stopRecording() {
    lock_guard<mutex> lock(recordingStreamMtx);
    LOGD(TAG, "stopRecording(): %d");

    if (recordingStream.mStream) {
        if (recordingStream.mStream->getState() != oboe::StreamState::Closed) {
            recordingStream.stopStream();
            flushWriteBuffer();
        } else {
            recordingStream.resetStream();
        }
    }
}

void RecordingEngine::restartPlayback(int fd) {
    lock_guard<mutex> lock(playbackStreamMtx);
    stopPlaybackCallable();
    startPlaybackCallable(fd);
}

void RecordingEngine::restartPlaybackWithMixingTracks(int fd) {
    lock_guard<mutex> lock(playbackStreamMtx);
    stopPlaybackCallable();
    startPlaybackWithMixingTracksCallable(fd);
}

void RecordingEngine::flushWriteBuffer() {
    mRecordingIO.flush_buffer();
}

int RecordingEngine::getCurrentMax() {
    return mRecordingIO.getCurrentMax();
}

void RecordingEngine::resetCurrentMax() {
    mRecordingIO.resetCurrentMax();
}

void RecordingEngine::addSourcesToPlayer(string* strArr, int count) {
    mRecordingIO.clearPlayerSources();

    map<string, shared_ptr<DataSource>> playMap;

    for (int i = 0; i < count; i++) {
        auto it = mSourceMapStore->sourceMap.find(strArr[i]);
        if (it != mSourceMapStore->sourceMap.end()) {
            playMap.insert(pair<string, shared_ptr<FileDataSource>>(it->first, it->second));
        }
    }

    for (int i = 0; i < count; i++) {
        strArr[i].erase();
    }

    mRecordingIO.addSourceMap(playMap);
}

void RecordingEngine::setStopPlayback() {
    call_in_attached_thread([&](auto env) {
        if (mRecordingScreenViewModelPassed && kotlinRecordingMethodIdsPtr) {
            env->CallStaticVoidMethod(kotlinRecordingMethodIdsPtr->recordingScreenVM,
                                      kotlinRecordingMethodIdsPtr->recordingScreenVMTogglePlay);
        }
    });
}

int RecordingEngine::getTotalSampleFrames() {
    return mRecordingIO.getTotalSampleFrames();
}

int RecordingEngine::getCurrentPlaybackProgress() {
    return mRecordingIO.getCurrentPlaybackProgress();
}

void RecordingEngine::setPlayHead(int position) {
    lock_guard<mutex> lock(playbackStreamMtx);
    mRecordingIO.setPlayHead(position);
}

int RecordingEngine::getDurationInSeconds() {
    return mRecordingIO.getDurationInSeconds();
}

void RecordingEngine::resetAudioEngine() {
    mRecordingIO.clearPlayerSources();
    mDataSource = shared_ptr<FileDataSource> { nullptr};
    return mRecordingIO.resetProperties();
}


