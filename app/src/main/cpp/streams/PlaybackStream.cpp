//
// Created by asalehin on 7/30/20.
//

#include "PlaybackStream.h"
#include "BaseStream.h"

PlaybackStream::PlaybackStream(RecordingIO* recordingIO): BaseStream(recordingIO) {
    mRecordingIO = recordingIO;
}

void PlaybackStream::openPlaybackStream() {
    LOGD(TAG, "openLivePlaybackStream(): ");
    oboe::AudioStreamBuilder builder;
    setupPlaybackStreamParameters(&builder, StreamConstants::mAudioApi, StreamConstants::mPlaybackFormat, &playbackCallback,
                                  StreamConstants::mPlaybackDeviceId, StreamConstants::mPlaybackSampleRate, StreamConstants::mOutputChannelCount);
    oboe::Result result = builder.openStream(&mPlaybackStream);
    if (result == oboe::Result::OK && mPlaybackStream) {
        assert(mPlaybackStream->getChannelCount() == StreamConstants::mOutputChannelCount);
//        assert(mPlaybackStream->getSampleRate() == mSampleRate);
        assert(mPlaybackStream->getFormat() == StreamConstants::mPlaybackFormat);

        auto sampleRate = mPlaybackStream->getSampleRate();
        LOGV(TAG, "openPlaybackStream(): mSampleRate = ");
        LOGV(TAG, std::to_string(sampleRate).c_str());

        int32_t mFramesPerBurst = mPlaybackStream->getFramesPerBurst();
        LOGV(TAG, "openPlaybackStream(): mFramesPerBurst = ");
        LOGV(TAG, std::to_string(mFramesPerBurst).c_str());

        // Set the buffer size to the burst size - this will give us the minimum possible latency
        mPlaybackStream->setBufferSizeInFrames(mFramesPerBurst);

    } else {
        LOGE(TAG, "openPlaybackStream(): Failed to create playback stream. Error: %s",
             oboe::convertToText(result));
    }

}

oboe::AudioStreamBuilder *
PlaybackStream::setupPlaybackStreamParameters(oboe::AudioStreamBuilder *builder,
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