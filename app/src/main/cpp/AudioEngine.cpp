//
// Created by asalehin on 7/9/20.
//

#include <oboe/Oboe.h>
#include "AudioEngine.h"
#include "logging_macros.h"

AudioEngine::AudioEngine(char* appDir, char* recordingSessionId) {
    assert(streamProcessor.mInputChannelCount == streamProcessor.mOutputChannelCount);
    mAppDir = appDir;
    mRecordingSessionId = recordingSessionId;

    char* recordingFilePath = strcat(mAppDir, "/recording.wav");
    mRecordingIO.setRecordingFilePath(recordingFilePath);
}

AudioEngine::~AudioEngine() {
    stopRecording();
    stopLivePlayback();
}

void AudioEngine::startLivePlayback() {
    LOGD(TAG, "startLivePlayback(): ");
    streamProcessor.openLivePlaybackStream();
    if (streamProcessor.mLivePlaybackStream) {
        streamProcessor.startStream(streamProcessor.mLivePlaybackStream);
    } else {
        LOGE(TAG, "startLivePlayback(): Failed to create live playback (%p) stream", streamProcessor.mLivePlaybackStream);
        streamProcessor.closeStream(streamProcessor.mLivePlaybackStream);
    }
}

void AudioEngine::stopLivePlayback() {
    LOGD(TAG, "stopLivePlayback(): %d");

    if (!streamProcessor.mLivePlaybackStream) {
        return;
    }

    if (streamProcessor.mLivePlaybackStream->getState() != oboe::StreamState::Closed) {
        streamProcessor.stopStream(streamProcessor.mLivePlaybackStream);
        streamProcessor.closeStream(streamProcessor.mLivePlaybackStream);
    }
}

void AudioEngine::pauseLivePlayback() {
    LOGD(TAG, "pauseLivePlayback(): ");
    streamProcessor.stopStream(streamProcessor.mLivePlaybackStream);
}

void AudioEngine::startPlayback() {
    LOGD(TAG, "startPlayback(): ");
    streamProcessor.openPlaybackStream();
    if (streamProcessor.mPlaybackStream) {
        if(mRecordingIO.setup_audio_source()) {
            streamProcessor.startStream(streamProcessor.mPlaybackStream);
        } else {
            LOGD(TAG, "Could not open recorded file");
            streamProcessor.closeStream(streamProcessor.mPlaybackStream);
        }
    } else {
        LOGE(TAG, "startPlayback(): Failed to create playback (%p) stream", streamProcessor.mPlaybackStream);
        streamProcessor.closeStream(streamProcessor.mPlaybackStream);
    }
}

void AudioEngine::stopPlayback() {
    LOGD(TAG, "stopPlayback(): %d");
    if (!streamProcessor.mPlaybackStream) {
        mRecordingIO.stop_audio_source();
        return;
    }

    if (streamProcessor.mPlaybackStream->getState() != oboe::StreamState::Closed) {
        streamProcessor.stopStream(streamProcessor.mPlaybackStream);
        streamProcessor.closeStream(streamProcessor.mPlaybackStream);
    }
}

void AudioEngine::pausePlayback() {
    LOGD(TAG, "pausePlayback(): ");
    mRecordingIO.pause_audio_source();
    streamProcessor.stopStream(streamProcessor.mPlaybackStream);
}

void AudioEngine::startRecording() {
    LOGD(TAG, "startRecording(): ");
    streamProcessor.openRecordingStream();
    if (streamProcessor.mRecordingStream) {
        streamProcessor.startStream(streamProcessor.mRecordingStream);
    } else {
        LOGE(TAG, "startRecording(): Failed to create recording (%p) stream", streamProcessor.mRecordingStream);
        streamProcessor.closeStream(streamProcessor.mRecordingStream);
    }
}

void AudioEngine::stopRecording() {
    LOGD(TAG, "stopRecording(): %d");

    if (!streamProcessor.mRecordingStream) {
        return;
    }

    if (streamProcessor.mRecordingStream->getState() != oboe::StreamState::Closed) {
        streamProcessor.stopStream(streamProcessor.mRecordingStream);
        streamProcessor.closeStream(streamProcessor.mRecordingStream);
        mRecordingIO.flush_buffer();
    }
}

void AudioEngine::pauseRecording() {
    LOGD(TAG, "pauseRecording(): ");
    streamProcessor.stopStream(streamProcessor.mRecordingStream);
    mRecordingIO.flush_buffer();
}
