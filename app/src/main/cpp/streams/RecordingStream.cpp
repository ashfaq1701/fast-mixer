//
// Created by asalehin on 7/30/20.
//

#include "RecordingStream.h"
#include "BaseStream.h"

RecordingStream::RecordingStream(RecordingIO* recordingIO): BaseStream(recordingIO) {
}

void RecordingStream::openRecordingStream() {
    LOGD(TAG, "openRecordingStream(): ");
    oboe::AudioStreamBuilder builder;
    setupRecordingStreamParameters(&builder);
    oboe::Result result = builder.openStream(&mRecordingStream);

    if (result == oboe::Result::OK && mRecordingStream) {
        assert(mRecordingStream->getChannelCount() == StreamConstants::mInputChannelCount);
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

oboe::AudioStreamBuilder *
RecordingStream::setupRecordingStreamParameters(oboe::AudioStreamBuilder *builder) {
    LOGD(TAG, "setUpRecordingStreamParameters(): ");
    builder->setAudioApi(StreamConstants::mAudioApi)
            ->setFormat(StreamConstants::mFormat)
            ->setSharingMode(oboe::SharingMode::Exclusive)
            ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
            ->setCallback(&recordingCallback)
            ->setDeviceId(StreamConstants::mRecordingDeviceId)
            ->setDirection(oboe::Direction::Input)
            ->setChannelCount(StreamConstants::mInputChannelCount)
            ->setFramesPerCallback(StreamConstants::mRecordingFramesPerCallback);

    return builder;
}
