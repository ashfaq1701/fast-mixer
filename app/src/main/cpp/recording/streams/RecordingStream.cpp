//
// Created by asalehin on 7/30/20.
//

#include <cassert>
#include "RecordingStream.h"
#include "../../streams/BaseStream.h"

RecordingStream::RecordingStream(RecordingIO* recordingIO): BaseStream(recordingIO) {
}

void RecordingStream::openRecordingStream() {
    oboe::AudioStreamBuilder builder;
    setupRecordingStreamParameters(&builder);
    oboe::Result result = builder.openStream(&mRecordingStream);

    if (result == oboe::Result::OK && mRecordingStream) {
        assert(mRecordingStream->getChannelCount() == StreamConstants::mInputChannelCount);
        auto sampleRate = mRecordingStream->getSampleRate();
        auto format = mRecordingStream->getFormat();
        LOGV(TAG, "openRecordingStream(): mSampleRate = ");
        LOGV(TAG, to_string(sampleRate).c_str());

        LOGV(TAG, "openRecordingStream(): mFormat = ");
        LOGV(TAG, oboe::convertToText(format));
    } else {
        LOGE(TAG, "Failed to create recording stream. Error: %s", oboe::convertToText(result));
    }
}

oboe::AudioStreamBuilder *
RecordingStream::setupRecordingStreamParameters(oboe::AudioStreamBuilder *builder) {
    builder->setAudioApi(StreamConstants::mAudioApi)
            ->setFormat(StreamConstants::mFormat)
            ->setSharingMode(oboe::SharingMode::Exclusive)
            ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
            ->setDataCallback(this)
            ->setDeviceId(StreamConstants::mRecordingDeviceId)
            ->setDirection(oboe::Direction::Input)
            ->setChannelCount(StreamConstants::mInputChannelCount)
            ->setFramesPerDataCallback(StreamConstants::mRecordingFramesPerCallback);

    return builder;
}

oboe::DataCallbackResult
RecordingStream::onAudioReady(oboe::AudioStream *audioStream, void *audioData, int32_t numFrames) {
    return processRecordingFrames(audioStream, static_cast<int16_t *>(audioData), numFrames * audioStream->getChannelCount());
}

oboe::DataCallbackResult
RecordingStream::processRecordingFrames(oboe::AudioStream *audioStream, int16_t *audioData,
                                          int32_t numFrames) {
    int32_t framesWritten = mRecordingIO->write(audioData, numFrames);
    return oboe::DataCallbackResult::Continue;
}

void RecordingStream::onErrorAfterClose(oboe::AudioStream *audioStream, oboe::Result result) {
    mRecordingStream = nullptr;
}
