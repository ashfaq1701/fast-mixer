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
    stopPlayback();
}

void RecordingEngine::startLivePlayback() {
    LOGD(TAG, "startLivePlayback(): ");
    mRecordingIO.sync_live_playback();
    livePlaybackStream.startStream();
}

void RecordingEngine::stopLivePlayback() {
    LOGD(TAG, "stopLivePlayback(): %d");

    if (livePlaybackStream.mStream != nullptr) {
        if (livePlaybackStream.mStream->getState() != oboe::StreamState::Closed) {
            livePlaybackStream.stopStream();
        } else {
            livePlaybackStream.resetStream();
        }
    }
}

bool RecordingEngine::startPlayback() {
    LOGD(TAG, "startPlayback(): ");

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

void RecordingEngine::stopAndResetPlayback() {
    LOGD(TAG, "stopAndResetPlayback()");
    mRecordingIO.clear_audio_source();
    closePlaybackStream();
}

void RecordingEngine::stopPlayback() {
    closePlaybackStream();
}

void RecordingEngine::closePlaybackStream() {
    if (playbackStream.mStream != nullptr) {
        if (playbackStream.mStream->getState() != oboe::StreamState::Closed) {
            playbackStream.stopStream();
        } else {
            playbackStream.resetStream();
        }
    }
}

void RecordingEngine::pausePlayback() {
    LOGD(TAG, "pausePlayback(): ");
    mRecordingIO.pause_audio_source();
    playbackStream.stopStream();
}

void RecordingEngine::startRecording() {
    LOGD(TAG, "startRecording(): ");
    recordingStream.startStream();
}

void RecordingEngine::stopRecording() {
    LOGD(TAG, "stopRecording(): %d");

    if (recordingStream.mStream != nullptr) {
        if (recordingStream.mStream->getState() != oboe::StreamState::Closed) {
            recordingStream.stopStream();
            flushWriteBuffer();
        } else {
            recordingStream.resetStream();
        }
    }
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


