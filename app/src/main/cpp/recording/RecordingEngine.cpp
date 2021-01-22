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
        string appDir,
        string recordingSessionId,
        bool recordingScreenViewModelPassed): mAppDir(move(appDir)), mRecordingSessionId(move(recordingSessionId)) {

    assert(RecordingStreamConstants::mInputChannelCount == RecordingStreamConstants::mOutputChannelCount);
    auto recordingFilePath = mAppDir + "/recording.wav";

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

bool RecordingEngine::startPlaybackCallable() {
    if (!playbackStream.mStream) {
        if (playbackStream.openStream() != oboe::Result::OK) {
            return false;
        }
    }

    auto setupSourceResult = mRecordingIO.setup_audio_source();

    if (setupSourceResult) {
        return playbackStream.startStream() == oboe::Result::OK;
    }

    return setupSourceResult;
}

bool RecordingEngine::startPlayback() {
    lock_guard<mutex> lock(playbackStreamMtx);
    LOGD(TAG, "startPlayback(): ");

    return startPlaybackCallable();
}

void RecordingEngine::stopAndResetPlayback() {
    lock_guard<mutex> lock(playbackStreamMtx);
    LOGD(TAG, "stopAndResetPlayback()");
    mRecordingIO.clear_audio_source();
    closePlaybackStream();
}

void RecordingEngine::stopPlayback() {
    lock_guard<mutex> lock(playbackStreamMtx);
    LOGD(TAG, "startPlayback()");

    stopPlaybackCallable();
}

void RecordingEngine::stopPlaybackCallable() {
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
    mRecordingIO.pause_audio_source();
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

void RecordingEngine::restartPlayback() {
    lock_guard<mutex> lock(playbackStreamMtx);
    stopPlaybackCallable();
    startPlaybackCallable();
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

int RecordingEngine::getTotalRecordedFrames() {
    return mRecordingIO.getTotalRecordedFrames();
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
    return mRecordingIO.resetProperties();
}


