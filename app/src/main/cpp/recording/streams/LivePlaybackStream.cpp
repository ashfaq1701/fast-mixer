//
// Created by asalehin on 7/30/20.
//

#include <cassert>
#include "LivePlaybackStream.h"

LivePlaybackStream::LivePlaybackStream(RecordingIO *recordingIO): BaseStream(recordingIO) {
}

void LivePlaybackStream::openLivePlaybackStream() {
    LOGD(TAG, "openLivePlaybackStream(): ");
    oboe::AudioStreamBuilder builder;
    setupLivePlaybackStreamParameters(&builder, StreamConstants::mAudioApi, StreamConstants::mFormat, this,
                                      StreamConstants::mPlaybackDeviceId, StreamConstants::mSampleRate, StreamConstants::mOutputChannelCount);
    oboe::Result result = builder.openStream(&mLivePlaybackStream);
    if (result == oboe::Result::OK && mLivePlaybackStream) {
        assert(mLivePlaybackStream->getChannelCount() == StreamConstants::mOutputChannelCount);
//        assert(mLivePlaybackStream->getSampleRate() == mSampleRate);
        assert(mLivePlaybackStream->getFormat() == StreamConstants::mFormat);

        auto sampleRate = mLivePlaybackStream->getSampleRate();
        LOGV(TAG, "openLivePlaybackStream(): mSampleRate = ");
        LOGV(TAG, to_string(sampleRate).c_str());

        int32_t mFramesPerBurst = mLivePlaybackStream->getFramesPerBurst();
        LOGV(TAG, "openLivePlaybackStream(): mFramesPerBurst = ");
        LOGV(TAG, to_string(mFramesPerBurst).c_str());

        mLivePlaybackStream->setBufferSizeInFrames(mFramesPerBurst);

    } else {
        LOGE(TAG, "openLivePlaybackStream(): Failed to create live playback stream. Error: %s",
             oboe::convertToText(result));
    }
}

oboe::AudioStreamBuilder *
LivePlaybackStream::setupLivePlaybackStreamParameters(oboe::AudioStreamBuilder *builder,
                                              oboe::AudioApi audioApi,
                                              oboe::AudioFormat audioFormat,
                                              oboe::AudioStreamDataCallback *audioStreamCallback,
                                              int32_t deviceId, int32_t sampleRate,
                                              int channelCount) {
    LOGD(TAG, "setupLivePlaybackStreamParameters()");
    builder->setAudioApi(audioApi)
            ->setFormat(audioFormat)
            ->setSharingMode(oboe::SharingMode::Exclusive)
            ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
            ->setDataCallback(audioStreamCallback)
            ->setDeviceId(deviceId)
            ->setDirection(oboe::Direction::Output)
            ->setSampleRate(sampleRate)
            ->setChannelCount(channelCount)
            ->setFramesPerDataCallback(StreamConstants::mLivePlaybackFramesPerCallback);
    return builder;
}

oboe::DataCallbackResult
LivePlaybackStream::onAudioReady(oboe::AudioStream *audioStream, void *audioData,
                                   int32_t numFrames) {
    return processLivePlaybackFrame(audioStream, static_cast<int16_t *>(audioData), numFrames * audioStream->getChannelCount());
}

oboe::DataCallbackResult
LivePlaybackStream::processLivePlaybackFrame(oboe::AudioStream *audioStream, int16_t *audioData,
                                               int32_t numFrames) {
    fillArrayWithZeros(audioData, numFrames);
    int64_t framesWritten = mRecordingIO->read_live_playback(audioData, numFrames);
    return oboe::DataCallbackResult::Continue;
}

void LivePlaybackStream::onErrorAfterClose(oboe::AudioStream *audioStream, oboe::Result result) {
    mLivePlaybackStream = nullptr;
}
