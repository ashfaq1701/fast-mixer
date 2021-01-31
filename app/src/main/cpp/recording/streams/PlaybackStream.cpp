//
// Created by asalehin on 7/30/20.
//

#include <cassert>
#include "PlaybackStream.h"

PlaybackStream::PlaybackStream(RecordingIO* recordingIO): RecordingBaseStream(recordingIO) {}

oboe::Result PlaybackStream::openStream() {
    LOGD(TAG, "openPlaybackStream(): ");
    oboe::AudioStreamBuilder builder;
    setupPlaybackStreamParameters(
            &builder,
            RecordingStreamConstants::mAudioApi,
            RecordingStreamConstants::mPlaybackFormat,
            this,
            RecordingStreamConstants::mPlaybackDeviceId,
            RecordingStreamConstants::mPlaybackSampleRate,
            RecordingStreamConstants::mOutputChannelCount
            );
    oboe::Result result = builder.openStream(mStream);
    if (result == oboe::Result::OK && mStream) {
        assert(mStream->getChannelCount() == RecordingStreamConstants::mOutputChannelCount);
//        assert(mStream->getSampleRate() == mSampleRate);
        assert(mStream->getFormat() == RecordingStreamConstants::mPlaybackFormat);

        int32_t mFramesPerBurst = mStream->getFramesPerBurst();

        mStream->setBufferSizeInFrames(mFramesPerBurst);

    } else {
        LOGE(TAG, "openPlaybackStream(): Failed to create playback stream. Error: %s",
             oboe::convertToText(result));
    }

    return result;
}

oboe::AudioStreamBuilder *
PlaybackStream::setupPlaybackStreamParameters(oboe::AudioStreamBuilder *builder,
                                          oboe::AudioApi audioApi, oboe::AudioFormat audioFormat,
                                          oboe::AudioStreamDataCallback *audioStreamCallback,
                                          int32_t deviceId, int32_t sampleRate, int channelCount) {
    LOGD(TAG, "setupPlaybackStreamParameters()");
    builder->setAudioApi(audioApi)
            ->setFormat(audioFormat)
            ->setSharingMode(oboe::SharingMode::Shared)
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
PlaybackStream::onAudioReady(oboe::AudioStream *audioStream, void *audioData,
                               int32_t numFrames) {
    if (audioStream && audioStream->getState() != oboe::StreamState::Closed) {
        return processPlaybackFrame(audioStream, static_cast<float_t *>(audioData), numFrames,
                                    audioStream->getChannelCount());
    }
    return oboe::DataCallbackResult::Stop;
}

oboe::DataCallbackResult
PlaybackStream::processPlaybackFrame(oboe::AudioStream *audioStream, float *audioData,
                                       int32_t numFrames, int32_t channelCount) {
    if (mStream->getState() != oboe::StreamState::Started) {
        return oboe::DataCallbackResult::Stop;
    }

    fillArrayWithZeros(audioData, numFrames);
    mRecordingIO->read_playback(audioData, numFrames);
    return oboe::DataCallbackResult::Continue;
}

void PlaybackStream::onErrorAfterClose(oboe::AudioStream *audioStream, oboe::Result result) {
    mStream.reset();
}
