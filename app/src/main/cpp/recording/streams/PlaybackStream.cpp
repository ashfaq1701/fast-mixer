//
// Created by asalehin on 7/30/20.
//

#include <cassert>
#include "PlaybackStream.h"
#include "../../streams/BaseStream.h"

PlaybackStream::PlaybackStream(RecordingIO* recordingIO): BaseStream(recordingIO) {
    mRecordingIO = recordingIO;
}

void PlaybackStream::startStream() {
    if (!stream) {
        openPlaybackStream();
    }
    BaseStream::startStream();
}

void PlaybackStream::openPlaybackStream() {
    LOGD(TAG, "openLivePlaybackStream(): ");
    oboe::AudioStreamBuilder builder;
    setupPlaybackStreamParameters(&builder, StreamConstants::mAudioApi, StreamConstants::mPlaybackFormat, this,
                                  StreamConstants::mPlaybackDeviceId, StreamConstants::mPlaybackSampleRate, StreamConstants::mOutputChannelCount);
    oboe::Result result = builder.openStream(&stream);
    if (result == oboe::Result::OK && stream) {
        assert(stream->getChannelCount() == StreamConstants::mOutputChannelCount);
//        assert(stream->getSampleRate() == mSampleRate);
        assert(stream->getFormat() == StreamConstants::mPlaybackFormat);

        int32_t mFramesPerBurst = stream->getFramesPerBurst();

        stream->setBufferSizeInFrames(mFramesPerBurst);

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
            ->setChannelCount(channelCount);
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
    stream = nullptr;
}

bool PlaybackStream::onError(oboe::AudioStream *stream, oboe::Result result) {
    return AudioStreamErrorCallback::onError(stream, result);
}
