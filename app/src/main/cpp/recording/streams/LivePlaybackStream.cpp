//
// Created by asalehin on 7/30/20.
//

#include <cassert>
#include "LivePlaybackStream.h"

LivePlaybackStream::LivePlaybackStream(shared_ptr<RecordingIO> recordingIO): RecordingBaseStream(recordingIO) {}

oboe::Result LivePlaybackStream::openStream() {
    LOGD(TAG, "openLivePlaybackStream(): ");
    oboe::AudioStreamBuilder builder;
    setupLivePlaybackStreamParameters(&builder, RecordingStreamConstants::mAudioApi, RecordingStreamConstants::mFormat, this,
                                      RecordingStreamConstants::mPlaybackDeviceId, RecordingStreamConstants::mSampleRate, RecordingStreamConstants::mOutputChannelCount);
    oboe::Result result = builder.openStream(mStream);
    if (result == oboe::Result::OK) {
        assert(mStream->getChannelCount() == RecordingStreamConstants::mOutputChannelCount);
//        assert(mStream->getSampleRate() == mSampleRate);
        assert(mStream->getFormat() == RecordingStreamConstants::mFormat);

        auto sampleRate = mStream->getSampleRate();
        LOGV(TAG, "openLivePlaybackStream(): mSampleRate = ");
        LOGV(TAG, to_string(sampleRate).c_str());

        int32_t mFramesPerBurst = mStream->getFramesPerBurst();
        LOGV(TAG, "openLivePlaybackStream(): mFramesPerBurst = ");
        LOGV(TAG, to_string(mFramesPerBurst).c_str());

        mStream->setBufferSizeInFrames(mFramesPerBurst);

    } else {
        LOGE(TAG, "openLivePlaybackStream(): Failed to create live playback stream. Error: %s",
             oboe::convertToText(result));
    }
    return result;
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
            ->setSharingMode(oboe::SharingMode::Shared)
            ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
            ->setDataCallback(audioStreamCallback)
            ->setDeviceId(deviceId)
            ->setDirection(oboe::Direction::Output)
            ->setSampleRate(sampleRate)
            ->setChannelCount(channelCount);
    return builder;
}

oboe::DataCallbackResult
LivePlaybackStream::onAudioReady(oboe::AudioStream *audioStream, void *audioData,
                                   int32_t numFrames) {
    if (audioStream) {
        return processLivePlaybackFrame(audioStream, static_cast<int16_t *>(audioData),
                                        numFrames * audioStream->getChannelCount());
    }
    return oboe::DataCallbackResult::Stop;
}

oboe::DataCallbackResult
LivePlaybackStream::processLivePlaybackFrame(oboe::AudioStream *audioStream, int16_t *audioData,
                                               int32_t numFrames) {
    if (audioData) {
        fillArrayWithZeros(audioData, numFrames);
        int64_t framesWritten = mRecordingIO->read_live_playback(audioData, numFrames);
    }
    return oboe::DataCallbackResult::Continue;
}
