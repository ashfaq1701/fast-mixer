//
// Created by asalehin on 7/9/20.
//

#include <oboe/Oboe.h>
#include "AudioEngine.h"
#include "logging_macros.h"

AudioEngine::AudioEngine(char* appDir, char* recordingSessionId) {
    assert(StreamConstants::mInputChannelCount == StreamConstants::mOutputChannelCount);
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
    livePlaybackStream.openLivePlaybackStream();
    if (livePlaybackStream.mLivePlaybackStream) {
        livePlaybackStream.startStream(livePlaybackStream.mLivePlaybackStream);
    } else {
        LOGE(TAG, "startLivePlayback(): Failed to create live playback (%p) stream", livePlaybackStream.mLivePlaybackStream);
        livePlaybackStream.closeStream(livePlaybackStream.mLivePlaybackStream);
    }
}

void AudioEngine::stopLivePlayback() {
    LOGD(TAG, "stopLivePlayback(): %d");

    if (!livePlaybackStream.mLivePlaybackStream) {
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

void AudioEngine::stopPlayback() {
    LOGD(TAG, "stopPlayback(): %d");
    if (!playbackStream.mPlaybackStream) {
        mRecordingIO.stop_audio_source();
        return;
    }

    if (playbackStream.mPlaybackStream->getState() != oboe::StreamState::Closed) {
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
        mRecordingIO.flush_buffer();
    }
}

void AudioEngine::pauseRecording() {
    LOGD(TAG, "pauseRecording(): ");
    recordingStream.stopStream(recordingStream.mRecordingStream);
    mRecordingIO.flush_buffer();
}
