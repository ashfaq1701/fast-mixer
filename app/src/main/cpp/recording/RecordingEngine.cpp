//
// Created by asalehin on 7/9/20.
//

#include <oboe/Oboe.h>

#include <cassert>
#include <utility>
#include "recording_jvm_env.h"
#include "RecordingEngine.h"
#include "../logging_macros.h"

RecordingEngine::RecordingEngine(
        string appDir,
        string recordingSessionId,
        bool recordingScreenViewModelPassed): mAppDir(move(appDir)), mRecordingSessionId(move(recordingSessionId)) {

    assert(StreamConstants::mInputChannelCount == StreamConstants::mOutputChannelCount);
    auto recordingFilePath = mAppDir + "/recording.wav";

    mRecordingIO.setRecordingFilePath(recordingFilePath);
    mRecordingIO.setStopPlaybackCallback([&] () {
        setStopPlayback();
    });
    mRecordingScreenViewModelPassed = recordingScreenViewModelPassed;
}

RecordingEngine::~RecordingEngine() {
    stopRecording();
    stopLivePlayback();
}

void RecordingEngine::startLivePlayback() {
    LOGD(TAG, "startLivePlayback(): ");
    livePlaybackStream.openLivePlaybackStream();
    if (livePlaybackStream.mStream != nullptr) {
        mRecordingIO.sync_live_playback();
        livePlaybackStream.startStream();
    } else {
        LOGE(TAG, "startLivePlayback(): Failed to create live playback (%p) stream", livePlaybackStream.mStream.get());
        livePlaybackStream.closeStream();
    }
}

void RecordingEngine::stopLivePlayback() {
    LOGD(TAG, "stopLivePlayback(): %d");

    if (livePlaybackStream.mStream == nullptr) {
        return;
    }

    if (livePlaybackStream.mStream->getState() != oboe::StreamState::Closed) {
        livePlaybackStream.stopStream();
        livePlaybackStream.closeStream();
    }
}

void RecordingEngine::pauseLivePlayback() {
    LOGD(TAG, "pauseLivePlayback(): ");
    livePlaybackStream.stopStream();
}

bool RecordingEngine::startPlayback() {
    LOGD(TAG, "startPlayback(): ");
    playbackStream.openPlaybackStream();
    if (playbackStream.mStream) {
        if(mRecordingIO.setup_audio_source()) {
            playbackStream.startStream();
            return true;
        } else {
            playbackStream.closeStream();
            return false;
        }
    } else {
        LOGE(TAG, "startPlayback(): Failed to create playback (%p) stream", playbackStream.mStream.get());
        playbackStream.closeStream();
        return false;
    }
}

void RecordingEngine::stopAndResetPlayback() {
    LOGD(TAG, "stopAndResetPlayback()");
    if (playbackStream.mStream == nullptr) {
        mRecordingIO.stop_audio_source();
        return;
    }

    closePlaybackStream();
}

void RecordingEngine::stopPlayback() {
    if (playbackStream.mStream == nullptr) {
        return;
    }
    closePlaybackStream();
}

void RecordingEngine::closePlaybackStream() {
    if (playbackStream.mStream != nullptr && playbackStream.mStream->getState() != oboe::StreamState::Closed) {
        playbackStream.stopStream();
        playbackStream.closeStream();
    }
}

void RecordingEngine::pausePlayback() {
    LOGD(TAG, "pausePlayback(): ");
    mRecordingIO.pause_audio_source();
    playbackStream.stopStream();
}

void RecordingEngine::startRecording() {
    LOGD(TAG, "startRecording(): ");
    recordingStream.openRecordingStream();
    if (recordingStream.mStream) {
        recordingStream.startStream();
    } else {
        LOGE(TAG, "startRecording(): Failed to create recording (%p) stream", recordingStream.mStream.get());
        recordingStream.closeStream();
    }
}

void RecordingEngine::stopRecording() {
    LOGD(TAG, "stopRecording(): %d");

    if (!recordingStream.mStream) {
        return;
    }

    if (recordingStream.mStream->getState() != oboe::StreamState::Closed) {
        recordingStream.stopStream();
        recordingStream.closeStream();
        flushWriteBuffer();
    }
}

void RecordingEngine::pauseRecording() {
    LOGD(TAG, "pauseRecording(): ");
    recordingStream.stopStream();
    flushWriteBuffer();
}

void RecordingEngine::restartPlayback() {
    stopPlayback();
    startPlayback();
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

void RecordingEngine::setStopPlayback() {
    call_in_attached_thread([&](auto env) {
        if (mRecordingScreenViewModelPassed && kotlinMethodIdsPtr != nullptr) {
            env->CallStaticVoidMethod(kotlinMethodIdsPtr->recordingScreenVM, kotlinMethodIdsPtr->recordingScreenVMTogglePlay);
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
    mRecordingIO.setPlayHead(position);
}

int RecordingEngine::getDurationInSeconds() {
    return mRecordingIO.getDurationInSeconds();
}

void RecordingEngine::resetAudioEngine() {
    return mRecordingIO.resetProperties();
}


