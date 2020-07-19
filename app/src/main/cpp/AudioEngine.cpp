//
// Created by asalehin on 7/9/20.
//

#include <oboe/Oboe.h>
#include "AudioEngine.h"
#include "logging_macros.h"

AudioEngine::AudioEngine(char* appDir, char* recordingSessionId) {
    assert(mInputChannelCount == mOutputChannelCount);
    mAppDir = appDir;
    mRecordingSessionId = recordingSessionId;

    char* recordingFilePath = strcat(mAppDir, "/recording");
    mSoundRecording.setRecordingFilePath(recordingFilePath);

    recordingCallback = RecordingCallback(&mSoundRecording);
    livePlaybackCallback = LivePlaybackCallback(&mSoundRecording);
    playbackCallback = PlaybackCallback();
}

AudioEngine::~AudioEngine() {
    stopRecording();
    stopLivePlayback();
}

void AudioEngine::startLivePlayback() {
    LOGD(TAG, "startLivePlayback(): ");
    openLivePlaybackStream();
    if (mLivePlaybackStream) {
        startStream(mLivePlaybackStream);
    } else {
        LOGE(TAG, "startLivePlayback(): Failed to create live playback (%p) stream", mLivePlaybackStream);
        closeStream(mLivePlaybackStream);
    }
}

void AudioEngine::stopLivePlayback() {
    LOGD(TAG, "stopLivePlayback(): %d");

    if (!mLivePlaybackStream) {
        return;
    }

    if (mLivePlaybackStream->getState() != oboe::StreamState::Closed) {
        stopStream(mLivePlaybackStream);
        closeStream(mLivePlaybackStream);
    }
}

void AudioEngine::pauseLivePlayback() {
    LOGD(TAG, "pauseLivePlayback(): ");
    stopStream(mLivePlaybackStream);
}

void AudioEngine::startRecording() {
    LOGD(TAG, "startRecording(): ");
    openRecordingStream();
    if (mRecordingStream) {
        startStream(mRecordingStream);
    } else {
        LOGE(TAG, "startRecording(): Failed to create recording (%p) stream", mRecordingStream);
        closeStream(mRecordingStream);
    }
}

void AudioEngine::stopRecording() {
    LOGD(TAG, "stopRecording(): %d");

    if (!mRecordingStream) {
        return;
    }

    if (mRecordingStream->getState() != oboe::StreamState::Closed) {
        stopStream(mRecordingStream);
        closeStream(mRecordingStream);
        mSoundRecording.flush_buffer();
    }
}

void AudioEngine::pauseRecording() {
    LOGD(TAG, "pauseRecording(): ");
    stopStream(mRecordingStream);
    mSoundRecording.flush_buffer();
}

void AudioEngine::openRecordingStream() {
    LOGD(TAG, "openRecordingStream(): ");
    oboe::AudioStreamBuilder builder;
    setupRecordingStreamParameters(&builder);
    oboe::Result result = builder.openStream(&mRecordingStream);

    if (result == oboe::Result::OK && mRecordingStream) {
        assert(mRecordingStream->getChannelCount() == mInputChannelCount);
        mSampleRate = mRecordingStream->getSampleRate();
        mFormat = mRecordingStream->getFormat();
        LOGV(TAG, "openRecordingStream(): mSampleRate = ");
        LOGV(TAG, std::to_string(mSampleRate).c_str());

        LOGV(TAG, "openRecordingStream(): mFormat = ");
        LOGV(TAG, oboe::convertToText(mFormat));
    } else {
        LOGE(TAG, "Failed to create recording stream. Error: %s", oboe::convertToText(result));
    }
}

void AudioEngine::openLivePlaybackStream() {
    LOGD(TAG, "openLivePlaybackStream(): ");
    oboe::AudioStreamBuilder builder;
    setupLivePlaybackStreamParameters(&builder,mAudioApi, mFormat, &livePlaybackCallback,
                                      mPlaybackDeviceId, mSampleRate, mOutputChannelCount);
    oboe::Result result = builder.openStream(&mLivePlaybackStream);
    if (result == oboe::Result::OK && mLivePlaybackStream) {
        assert(mLivePlaybackStream->getChannelCount() == mOutputChannelCount);
//        assert(mLivePlaybackStream->getSampleRate() == mSampleRate);
        assert(mLivePlaybackStream->getFormat() == mFormat);

        mSampleRate = mLivePlaybackStream->getSampleRate();
        LOGV(TAG, "openLivePlaybackStream(): mSampleRate = ");
        LOGV(TAG, std::to_string(mSampleRate).c_str());

        mFramesPerBurst = mLivePlaybackStream->getFramesPerBurst();
        LOGV(TAG, "openLivePlaybackStream(): mFramesPerBurst = ");
        LOGV(TAG, std::to_string(mFramesPerBurst).c_str());

        mLivePlaybackStream->setBufferSizeInFrames(mFramesPerBurst);

    } else {
        LOGE(TAG, "openLivePlaybackStream(): Failed to create live playback stream. Error: %s",
             oboe::convertToText(result));
    }
}

void AudioEngine::openPlaybackStream() {
    LOGD(TAG, "openLivePlaybackStream(): ");
    oboe::AudioStreamBuilder builder;
    setupLivePlaybackStreamParameters(&builder, mAudioApi, mFormat, &playbackCallback,
                                      mPlaybackDeviceId, mSampleRate, mOutputChannelCount);
    oboe::Result result = builder.openStream(&mPlaybackStream);
    if (result == oboe::Result::OK && mPlaybackStream) {
        assert(mPlaybackStream->getChannelCount() == mOutputChannelCount);
//        assert(mPlaybackStream->getSampleRate() == mSampleRate);
        assert(mPlaybackStream->getFormat() == mFormat);

        mSampleRate = mPlaybackStream->getSampleRate();
        LOGV(TAG, "openPlaybackStream(): mSampleRate = ");
        LOGV(TAG, std::to_string(mSampleRate).c_str());

        mFramesPerBurst = mPlaybackStream->getFramesPerBurst();
        LOGV(TAG, "openPlaybackStream(): mFramesPerBurst = ");
        LOGV(TAG, std::to_string(mFramesPerBurst).c_str());

        // Set the buffer size to the burst size - this will give us the minimum possible latency
        mPlaybackStream->setBufferSizeInFrames(mFramesPerBurst);

    } else {
        LOGE(TAG, "openPlaybackStream(): Failed to create playback stream. Error: %s",
             oboe::convertToText(result));
    }

}

void AudioEngine::startStream(oboe::AudioStream *stream) {
    LOGD(TAG, "startStream(): ");
    assert(stream);
    if (stream) {
        oboe::Result result = stream->requestStart();
        if (result != oboe::Result::OK) {
            LOGE(TAG, "Error starting the stream: %s", oboe::convertToText(result));
        }
    }
}

void AudioEngine::stopStream(oboe::AudioStream *stream) {
    LOGD("stopStream(): ");
    if (stream) {
        oboe::Result result = stream->stop(0L);
        if (result != oboe::Result::OK) {
            LOGE(TAG, "Error stopping the stream: %s");
            LOGE(TAG, oboe::convertToText(result));
        }
        LOGW(TAG, "stopStream(): Total samples = ");
        LOGW(TAG, std::to_string(mSoundRecording.getTotalSamples()).c_str());
    }
}

void AudioEngine::closeStream(oboe::AudioStream *stream) {
    LOGD("closeStream(): ");

    if (stream) {
        oboe::Result result = stream->close();
        if (result != oboe::Result::OK) {
            LOGE(TAG, "Error closing stream. %s", oboe::convertToText(result));
        } else {
            stream = nullptr;
        }

        LOGW(TAG, "closeStream(): mTotalSamples = ");
        LOGW(TAG, std::to_string(mSoundRecording.getTotalSamples()).c_str());
    }
}

oboe::AudioStreamBuilder *
AudioEngine::setupRecordingStreamParameters(oboe::AudioStreamBuilder *builder) {
    LOGD(TAG, "setUpRecordingStreamParameters(): ");
    builder->setAudioApi(mAudioApi)
        ->setFormat(mFormat)
        ->setSharingMode(oboe::SharingMode::Exclusive)
        ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
        ->setCallback(&recordingCallback)
        ->setDeviceId(mRecordingDeviceId)
        ->setDirection(oboe::Direction::Input)
        ->setChannelCount(mInputChannelCount)
        ->setFramesPerCallback(mRecordingFramesPerCallback);

    return builder;
}

oboe::AudioStreamBuilder *
AudioEngine::setupLivePlaybackStreamParameters(oboe::AudioStreamBuilder *builder,
                                               oboe::AudioApi audioApi,
                                               oboe::AudioFormat audioFormat,
                                               oboe::AudioStreamCallback *audioStreamCallback,
                                               int32_t deviceId, int32_t sampleRate,
                                               int channelCount) {
    LOGD(TAG, "setupLivePlaybackStreamParameters()");
    builder->setAudioApi(audioApi)
            ->setFormat(audioFormat)
            ->setSharingMode(oboe::SharingMode::Exclusive)
            ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
            ->setCallback(audioStreamCallback)
            ->setDeviceId(deviceId)
            ->setDirection(oboe::Direction::Output)
            ->setSampleRate(sampleRate)
            ->setChannelCount(channelCount)
            ->setFramesPerCallback(mRecordingFramesPerCallback);;
    return builder;
}

oboe::AudioStreamBuilder *
AudioEngine::setupPlaybackStreamParameters(oboe::AudioStreamBuilder *builder,
                                           oboe::AudioApi audioApi, oboe::AudioFormat audioFormat,
                                           oboe::AudioStreamCallback *audioStreamCallback,
                                           int32_t deviceId, int32_t sampleRate, int channelCount) {
    LOGD(TAG, "setupPlaybackStreamParameters()");
    builder->setAudioApi(audioApi)
            ->setFormat(audioFormat)
            ->setSharingMode(oboe::SharingMode::Exclusive)
            ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
            ->setCallback(audioStreamCallback)
            ->setDeviceId(deviceId)
            ->setDirection(oboe::Direction::Output)
            ->setSampleRate(sampleRate)
            ->setChannelCount(channelCount);
    return builder;
}
