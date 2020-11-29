//
// Created by asalehin on 7/30/20.
//

#include <cassert>
#include "PlaybackStream.h"
#include "../../streams/BaseStream.h"

PlaybackStream::PlaybackStream(RecordingIO* recordingIO): BaseStream(recordingIO) {
    mRecordingIO = recordingIO;
}

void PlaybackStream::openPlaybackStream() {
    LOGD(TAG, "openLivePlaybackStream(): ");
    oboe::AudioStreamBuilder builder;
    setupPlaybackStreamParameters(&builder, StreamConstants::mAudioApi, StreamConstants::mPlaybackFormat, this,
                                  StreamConstants::mPlaybackDeviceId, StreamConstants::mPlaybackSampleRate, StreamConstants::mOutputChannelCount);
    oboe::Result result = builder.openStream(&mPlaybackStream);
    if (result == oboe::Result::OK && mPlaybackStream) {
        assert(mPlaybackStream->getChannelCount() == StreamConstants::mOutputChannelCount);
//        assert(mPlaybackStream->getSampleRate() == mSampleRate);
        assert(mPlaybackStream->getFormat() == StreamConstants::mPlaybackFormat);

        int32_t mFramesPerBurst = mPlaybackStream->getFramesPerBurst();

        mPlaybackStream->setBufferSizeInFrames(mFramesPerBurst);

    } else {
        LOGE(TAG, "openPlaybackStream(): Failed to create playback stream. Error: %s",
             oboe::convertToText(result));
    }
}

oboe::AudioStreamBuilder *
PlaybackStream::setupPlaybackStreamParameters(oboe::AudioStreamBuilder *builder,
                                          oboe::AudioApi audioApi, oboe::AudioFormat audioFormat,
                                          oboe::AudioStreamDataCallback *audioStreamCallback,
                                          int32_t deviceId, int32_t sampleRate, int channelCount) {
    LOGD(TAG, "setupPlaybackStreamParameters()");
    builder->setAudioApi(audioApi)
            ->setFormat(audioFormat)
            ->setSharingMode(oboe::SharingMode::Exclusive)
            ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
            ->setDataCallback(audioStreamCallback)
            ->setErrorCallback(reinterpret_cast<AudioStreamErrorCallback *>(audioStreamCallback))
            ->setDeviceId(deviceId)
            ->setDirection(oboe::Direction::Output)
            ->setSampleRate(sampleRate)
            ->setChannelCount(channelCount)
            ->setFramesPerDataCallback(StreamConstants::mPlaybackFramesPerCallback);
    return builder;
}

oboe::DataCallbackResult
PlaybackStream::onAudioReady(oboe::AudioStream *audioStream, void *audioData,
                               int32_t numFrames) {
    return processPlaybackFrame(audioStream, static_cast<float_t *>(audioData), numFrames, audioStream->getChannelCount());
}

oboe::DataCallbackResult
PlaybackStream::processPlaybackFrame(oboe::AudioStream *audioStream, float *audioData,
                                       int32_t numFrames, int32_t channelCount) {
    mRecordingIO->read_playback(audioData, numFrames, channelCount);
    return oboe::DataCallbackResult::Continue;
}

void PlaybackStream::onErrorAfterClose(oboe::AudioStream *audioStream, oboe::Result result) {
    mPlaybackStream = nullptr;
}

bool PlaybackStream::onError(oboe::AudioStream *stream, oboe::Result result) {
    return AudioStreamErrorCallback::onError(stream, result);
}
