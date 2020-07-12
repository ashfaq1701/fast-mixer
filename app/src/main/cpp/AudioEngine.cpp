//
// Created by asalehin on 7/9/20.
//

#include <oboe/Oboe.h>
#include "AudioEngine.h"
#include "logging_macros.h"

AudioEngine::AudioEngine() {
    assert(mInputChannelCount == mOutputChannelCount);
}

AudioEngine::~AudioEngine() {
    stopRecording();
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
    }
}

void AudioEngine::pauseRecording() {
    LOGD(TAG, "pauseRecording(): ");
    stopStream(mRecordingStream);
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

}

void AudioEngine::openPlaybackStream() {

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
        ->setChannelCount(mInputChannelCount);
    return builder;
}

oboe::AudioStreamBuilder *
AudioEngine::setupLivePlaybackStreamParameters(oboe::AudioStreamBuilder *builder,
                                               oboe::AudioApi audioApi,
                                               oboe::AudioFormat audioFormat,
                                               oboe::AudioStreamCallback *audioStreamCallback,
                                               int32_t deviceId, int32_t sampleRate,
                                               int channelCount) {
    return nullptr;
}

oboe::AudioStreamBuilder *
AudioEngine::setupPlaybackStreamParameters(oboe::AudioStreamBuilder *builder,
                                           oboe::AudioApi audioApi, oboe::AudioFormat audioFormat,
                                           oboe::AudioStreamCallback *audioStreamCallback,
                                           int32_t deviceId, int32_t sampleRate, int channelCount) {
    return nullptr;
}
