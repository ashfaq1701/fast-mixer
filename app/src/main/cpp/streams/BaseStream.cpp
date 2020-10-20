//
// Created by asalehin on 7/30/20.
//

#include "BaseStream.h"

BaseStream::BaseStream(RecordingIO* recordingIO) {
    mRecordingIO = recordingIO;
}

void BaseStream::startStream(oboe::AudioStream *stream) {
    LOGD(TAG, "startStream(): ");
    assert(stream);
    if (stream) {
        oboe::Result result = stream->requestStart();
        if (result != oboe::Result::OK) {
            LOGE(TAG, "Error starting the stream: %s", oboe::convertToText(result));
        }
    }
}

void BaseStream::stopStream(oboe::AudioStream *stream) {
    LOGD("stopStream(): ");
    if (stream && stream->getState() != oboe::StreamState::Closed) {
        oboe::Result result = stream->stop(0L);
        if (result != oboe::Result::OK) {
            LOGE(TAG, "Error stopping the stream: %s");
            LOGE(TAG, oboe::convertToText(result));
        }
        LOGW(TAG, "stopStream(): Total samples = ");
        LOGW(TAG, to_string(mRecordingIO->getTotalSamples()).c_str());
    }
}

void BaseStream::closeStream(oboe::AudioStream *stream) {
    LOGD("closeStream(): ");

    if (stream && stream->getState() != oboe::StreamState::Closing && stream->getState() != oboe::StreamState::Closed) {
        oboe::Result result = stream->close();
        if (result != oboe::Result::OK) {
            LOGE(TAG, "Error closing stream. %s", oboe::convertToText(result));
        } else {
            stream = nullptr;
        }

        LOGW(TAG, "closeStream(): mTotalSamples = ");
        LOGW(TAG, to_string(mRecordingIO->getTotalSamples()).c_str());
    }
}
