//
// Created by asalehin on 7/30/20.
//

#include <cassert>
#include "RecordingStream.h"

RecordingStream::RecordingStream(shared_ptr<RecordingIO> recordingIO): RecordingBaseStream(recordingIO) {}

oboe::Result RecordingStream::openStream() {
    oboe::AudioStreamBuilder builder;
    setupRecordingStreamParameters(&builder);
    oboe::Result result = builder.openStream(mStream);

    if (result == oboe::Result::OK && mStream) {
        assert(mStream->getChannelCount() == RecordingStreamConstants::mInputChannelCount);
        auto sampleRate = mStream->getSampleRate();
        auto format = mStream->getFormat();
        LOGV(TAG, "openRecordingStream(): mSampleRate = ");
        LOGV(TAG, to_string(sampleRate).c_str());

        LOGV(TAG, "openRecordingStream(): mFormat = ");
        LOGV(TAG, oboe::convertToText(format));
    } else {
        LOGE(TAG, "Failed to create recording stream. Error: %s", oboe::convertToText(result));
    }
    return result;
}

oboe::AudioStreamBuilder *
RecordingStream::setupRecordingStreamParameters(oboe::AudioStreamBuilder *builder) {
    builder->setAudioApi(RecordingStreamConstants::mAudioApi)
            ->setFormat(RecordingStreamConstants::mFormat)
            ->setSharingMode(oboe::SharingMode::Exclusive)
            ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
            ->setInputPreset(RecordingStreamConstants::mRecordingPreset)
            ->setDataCallback(this)
            ->setDeviceId(RecordingStreamConstants::mRecordingDeviceId)
            ->setDirection(oboe::Direction::Input)
            ->setChannelCount(RecordingStreamConstants::mInputChannelCount);

    return builder;
}

oboe::DataCallbackResult
RecordingStream::onAudioReady(oboe::AudioStream *audioStream, void *audioData, int32_t numFrames) {
    if (audioStream) {
        return processRecordingFrames(audioStream, static_cast<int16_t *>(audioData),
                                      numFrames * audioStream->getChannelCount());
    }

    return oboe::DataCallbackResult::Stop;
}

oboe::DataCallbackResult
RecordingStream::processRecordingFrames(oboe::AudioStream *audioStream, int16_t *audioData,
                                          int32_t numFrames) {
    if (audioData) {
        int32_t framesWritten = mRecordingIO->write(audioData, numFrames);
    }
    return oboe::DataCallbackResult::Continue;
}
