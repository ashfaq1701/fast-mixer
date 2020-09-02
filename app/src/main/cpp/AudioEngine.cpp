//
// Created by asalehin on 7/9/20.
//

#include <oboe/Oboe.h>

#include <utility>
#include "jni_env.h"
#include "AudioEngine.h"
#include "logging_macros.h"

AudioEngine::AudioEngine(
        char* appDir,
        char* recordingSessionId,
        bool recordingScreenViewModelPassed) {
    assert(StreamConstants::mInputChannelCount == StreamConstants::mOutputChannelCount);
    mAppDir = appDir;
    mRecordingSessionId = recordingSessionId;

    char* recordingFilePath = strcat(mAppDir, "/recording.wav");
    mRecordingIO.setRecordingFilePath(recordingFilePath);
    mRecordingIO.setTogglePlaybackCallback([&] () {
        togglePlayback();
    });
    mRecordingScreenViewModelPassed = recordingScreenViewModelPassed;
}

AudioEngine::~AudioEngine() {
    stopRecording();
    stopLivePlayback();
}

void AudioEngine::startLivePlayback() {
    LOGD(TAG, "startLivePlayback(): ");
    livePlaybackStream.openLivePlaybackStream();
    if (livePlaybackStream.mLivePlaybackStream != nullptr) {
        mRecordingIO.sync_live_playback();
        livePlaybackStream.startStream(livePlaybackStream.mLivePlaybackStream);
    } else {
        LOGE(TAG, "startLivePlayback(): Failed to create live playback (%p) stream", livePlaybackStream.mLivePlaybackStream);
        livePlaybackStream.closeStream(livePlaybackStream.mLivePlaybackStream);
    }
}

void AudioEngine::stopLivePlayback() {
    LOGD(TAG, "stopLivePlayback(): %d");

    if (livePlaybackStream.mLivePlaybackStream == nullptr) {
        return;
    }

    if (livePlaybackStream.mLivePlaybackStream->getState() != oboe::StreamState::Closed) {
        livePlaybackStream.stopStream(livePlaybackStream.mLivePlaybackStream);
        livePlaybackStream.closeStream(livePlaybackStream.mLivePlaybackStream);
    }
}

void AudioEngine::pauseLivePlayback() {
    LOGD(TAG, "pauseLivePlayback(): ");
    livePlaybackStream.stopStream(livePlaybackStream.mLivePlaybackStream);
}

void AudioEngine::startPlayback() {
    LOGD(TAG, "startPlayback(): ");
    playbackStream.openPlaybackStream();
    if (playbackStream.mPlaybackStream) {
        if(mRecordingIO.setup_audio_source()) {
            playbackStream.startStream(playbackStream.mPlaybackStream);
        } else {
            LOGD(TAG, "Could not open recorded file");
            playbackStream.closeStream(playbackStream.mPlaybackStream);
        }
    } else {
        LOGE(TAG, "startPlayback(): Failed to create playback (%p) stream", playbackStream.mPlaybackStream);
        playbackStream.closeStream(playbackStream.mPlaybackStream);
    }
}

void AudioEngine::stopAndResetPlayback() {
    LOGD(TAG, "stopAndResetPlayback()");
    if (playbackStream.mPlaybackStream == nullptr) {
        mRecordingIO.stop_audio_source();
        return;
    }

    closePlaybackStream();
}

void AudioEngine::stopPlayback() {
    if (playbackStream.mPlaybackStream == nullptr) {
        return;
    }
    closePlaybackStream();
}

void AudioEngine::closePlaybackStream() {
    if (playbackStream.mPlaybackStream != nullptr && playbackStream.mPlaybackStream->getState() != oboe::StreamState::Closed) {
        playbackStream.stopStream(playbackStream.mPlaybackStream);
        playbackStream.closeStream(playbackStream.mPlaybackStream);
    }
}

void AudioEngine::pausePlayback() {
    LOGD(TAG, "pausePlayback(): ");
    mRecordingIO.pause_audio_source();
    playbackStream.stopStream(playbackStream.mPlaybackStream);
}

void AudioEngine::startRecording() {
    LOGD(TAG, "startRecording(): ");
    recordingStream.openRecordingStream();
    if (recordingStream.mRecordingStream) {
        recordingStream.startStream(recordingStream.mRecordingStream);
    } else {
        LOGE(TAG, "startRecording(): Failed to create recording (%p) stream", recordingStream.mRecordingStream);
        recordingStream.closeStream(recordingStream.mRecordingStream);
    }
}

void AudioEngine::stopRecording() {
    LOGD(TAG, "stopRecording(): %d");

    if (!recordingStream.mRecordingStream) {
        return;
    }

    if (recordingStream.mRecordingStream->getState() != oboe::StreamState::Closed) {
        recordingStream.stopStream(recordingStream.mRecordingStream);
        recordingStream.closeStream(recordingStream.mRecordingStream);
        flushWriteBuffer();
    }
}

void AudioEngine::pauseRecording() {
    LOGD(TAG, "pauseRecording(): ");
    recordingStream.stopStream(recordingStream.mRecordingStream);
    flushWriteBuffer();
}

void AudioEngine::restartPlayback() {
    stopPlayback();
    startPlayback();
}

void AudioEngine::flushWriteBuffer() {
    mRecordingIO.flush_buffer();
}

int AudioEngine::getCurrentMax() {
    return mRecordingIO.getCurrentMax();
}

void AudioEngine::resetCurrentMax() {
    mRecordingIO.resetCurrentMax();
}

void AudioEngine::togglePlayback() {
    call_in_attached_thread([&](auto env) {
        if (mRecordingScreenViewModelPassed && kotlinMethodIdsPtr != nullptr) {
            env->CallStaticVoidMethod(kotlinMethodIdsPtr->recordingScreenVM, kotlinMethodIdsPtr->recordingScreenVMTogglePlay);
        }
    });
}


