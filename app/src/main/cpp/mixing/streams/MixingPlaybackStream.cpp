//
// Created by Ashfaq Salehin on 11/1/2021 AD.
//

#include <cassert>
#include "MixingPlaybackStream.h"

MixingPlaybackStream::MixingPlaybackStream(MixingIO* mixingIO): MixingBaseStream(mixingIO) {}

oboe::Result MixingPlaybackStream::openStream() {
    LOGD(TAG, "openMixingPlaybackStream(): ");
    oboe::AudioStreamBuilder builder;
    setupMixingPlaybackStreamParameters(
            &builder,
            MixingStreamConstants::mAudioApi,
            MixingStreamConstants::mFormat,
            this,
            MixingStreamConstants::mDeviceId,
            MixingStreamConstants::mSampleRate,
            MixingStreamConstants::mChannelCount
    );
    oboe::Result result = builder.openStream(mStream);
    if (result == oboe::Result::OK && mStream) {
        assert(mStream->getChannelCount() == MixingStreamConstants::mChannelCount);
        assert(mStream->getFormat() == MixingStreamConstants::mFormat);
        int32_t mFramesPerBurst = mStream->getFramesPerBurst();
        mStream->setBufferSizeInFrames(mFramesPerBurst);
    } else {
        LOGE(TAG, "openMixingPlaybackStream(): Failed to create playback stream. Error: %s",
             oboe::convertToText(result));
    }
    return result;
}

oboe::AudioStreamBuilder *
MixingPlaybackStream::setupMixingPlaybackStreamParameters(oboe::AudioStreamBuilder *builder,
                                              oboe::AudioApi audioApi, oboe::AudioFormat audioFormat,
                                              oboe::AudioStreamDataCallback *audioStreamCallback,
                                              int32_t deviceId, int32_t sampleRate, int channelCount) {
    LOGD(TAG, "setupMixingPlaybackStreamParameters()");
    builder->setAudioApi(audioApi)
            ->setFormat(audioFormat)
            ->setSharingMode(oboe::SharingMode::Exclusive)
            ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
            ->setDataCallback(audioStreamCallback)
            ->setErrorCallback(reinterpret_cast<AudioStreamErrorCallback *>(audioStreamCallback))
            ->setDeviceId(deviceId)
            ->setDirection(oboe::Direction::Output)
            ->setSampleRate(sampleRate)
            ->setChannelCount(channelCount);
    return builder;
}

oboe::DataCallbackResult
MixingPlaybackStream::onAudioReady(oboe::AudioStream *audioStream, void *audioData,
                             int32_t numFrames) {
    if (audioStream && audioStream->getState() != oboe::StreamState::Closed) {
        return processPlaybackFrame(audioStream, static_cast<float_t *>(audioData), numFrames,
                                    audioStream->getChannelCount());
    }
    return oboe::DataCallbackResult::Stop;
}

oboe::DataCallbackResult
MixingPlaybackStream::processPlaybackFrame(oboe::AudioStream *audioStream, float *audioData,
                                     int32_t numFrames, int32_t channelCount) {
    fillArrayWithZeros(audioData, numFrames);
    mMixingIO->read_playback(audioData, numFrames);
    return oboe::DataCallbackResult::Continue;
}

void MixingPlaybackStream::onErrorAfterClose(oboe::AudioStream *audioStream, oboe::Result result) {
    mStream.reset();
}
