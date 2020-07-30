//
// Created by asalehin on 7/30/20.
//

#include "StreamProcessor.h"

int32_t StreamProcessor::mSampleRate = oboe::DefaultStreamValues::SampleRate;
int32_t StreamProcessor::mPlaybackSampleRate = mSampleRate;
int32_t StreamProcessor::mInputChannelCount = oboe::ChannelCount::Stereo;
int32_t StreamProcessor::mOutputChannelCount = oboe::ChannelCount::Stereo;

void StreamProcessor::openRecordingStream() {
    LOGD(TAG, "openRecordingStream(): ");
    oboe::AudioStreamBuilder builder;
    setupRecordingStreamParameters(&builder);
    oboe::Result result = builder.openStream(&mRecordingStream);

    if (result == oboe::Result::OK && mRecordingStream) {
        assert(mRecordingStream->getChannelCount() == mInputChannelCount);
        auto sampleRate = mRecordingStream->getSampleRate();
        auto format = mRecordingStream->getFormat();
        LOGV(TAG, "openRecordingStream(): mSampleRate = ");
        LOGV(TAG, std::to_string(sampleRate).c_str());

        LOGV(TAG, "openRecordingStream(): mFormat = ");
        LOGV(TAG, oboe::convertToText(format));
    } else {
        LOGE(TAG, "Failed to create recording stream. Error: %s", oboe::convertToText(result));
    }
}

void StreamProcessor::openLivePlaybackStream() {
    LOGD(TAG, "openLivePlaybackStream(): ");
    oboe::AudioStreamBuilder builder;
    setupLivePlaybackStreamParameters(&builder,mAudioApi, mFormat, &livePlaybackCallback,
                                      mPlaybackDeviceId, mSampleRate, mOutputChannelCount);
    oboe::Result result = builder.openStream(&mLivePlaybackStream);
    if (result == oboe::Result::OK && mLivePlaybackStream) {
        assert(mLivePlaybackStream->getChannelCount() == mOutputChannelCount);
//        assert(mLivePlaybackStream->getSampleRate() == mSampleRate);
        assert(mLivePlaybackStream->getFormat() == mFormat);

        auto sampleRate = mLivePlaybackStream->getSampleRate();
        LOGV(TAG, "openLivePlaybackStream(): mSampleRate = ");
        LOGV(TAG, std::to_string(sampleRate).c_str());

        mFramesPerBurst = mLivePlaybackStream->getFramesPerBurst();
        LOGV(TAG, "openLivePlaybackStream(): mFramesPerBurst = ");
        LOGV(TAG, std::to_string(mFramesPerBurst).c_str());

        mLivePlaybackStream->setBufferSizeInFrames(mFramesPerBurst);

    } else {
        LOGE(TAG, "openLivePlaybackStream(): Failed to create live playback stream. Error: %s",
             oboe::convertToText(result));
    }
}

void StreamProcessor::openPlaybackStream() {
    LOGD(TAG, "openLivePlaybackStream(): ");
    oboe::AudioStreamBuilder builder;
    setupPlaybackStreamParameters(&builder, mAudioApi, mPlaybackFormat, &playbackCallback,
                                  mPlaybackDeviceId, mPlaybackSampleRate, mOutputChannelCount);
    oboe::Result result = builder.openStream(&mPlaybackStream);
    if (result == oboe::Result::OK && mPlaybackStream) {
        assert(mPlaybackStream->getChannelCount() == mOutputChannelCount);
//        assert(mPlaybackStream->getSampleRate() == mSampleRate);
        assert(mPlaybackStream->getFormat() == mPlaybackFormat);

        auto sampleRate = mPlaybackStream->getSampleRate();
        LOGV(TAG, "openPlaybackStream(): mSampleRate = ");
        LOGV(TAG, std::to_string(sampleRate).c_str());

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

void StreamProcessor::startStream(oboe::AudioStream *stream) {
    LOGD(TAG, "startStream(): ");
    assert(stream);
    if (stream) {
        oboe::Result result = stream->requestStart();
        if (result != oboe::Result::OK) {
            LOGE(TAG, "Error starting the stream: %s", oboe::convertToText(result));
        }
    }
}

void StreamProcessor::stopStream(oboe::AudioStream *stream) {
    LOGD("stopStream(): ");
    if (stream && stream->getState() != oboe::StreamState::Closed) {
        oboe::Result result = stream->stop(0L);
        if (result != oboe::Result::OK) {
            LOGE(TAG, "Error stopping the stream: %s");
            LOGE(TAG, oboe::convertToText(result));
        }
        LOGW(TAG, "stopStream(): Total samples = ");
        LOGW(TAG, std::to_string(mRecordingIO->getTotalSamples()).c_str());
    }
}

void StreamProcessor::closeStream(oboe::AudioStream *stream) {
    LOGD("closeStream(): ");

    if (stream) {
        oboe::Result result = stream->close();
        if (result != oboe::Result::OK) {
            LOGE(TAG, "Error closing stream. %s", oboe::convertToText(result));
        } else {
            stream = nullptr;
        }

        LOGW(TAG, "closeStream(): mTotalSamples = ");
        LOGW(TAG, std::to_string(mRecordingIO->getTotalSamples()).c_str());
    }
}

oboe::AudioStreamBuilder *
StreamProcessor::setupRecordingStreamParameters(oboe::AudioStreamBuilder *builder) {
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
StreamProcessor::setupLivePlaybackStreamParameters(oboe::AudioStreamBuilder *builder,
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
            ->setFramesPerCallback(mLivePlaybackFramesPerCallback);
    return builder;
}

oboe::AudioStreamBuilder *
StreamProcessor::setupPlaybackStreamParameters(oboe::AudioStreamBuilder *builder,
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
