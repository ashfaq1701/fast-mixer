//
// Created by asalehin on 7/30/20.
//

#include <cassert>
#include "BaseStream.h"

BaseStream::BaseStream(RecordingIO* recordingIO) {
    mRecordingIO = recordingIO;
}

void BaseStream::startStream() {
    LOGD(TAG, "startStream(): ");
    if (stream) {
        oboe::Result result = stream->requestStart();
        if (result != oboe::Result::OK) {
            LOGE(TAG, "Error starting the stream: %s", oboe::convertToText(result));
        }
    }
}

void BaseStream::stopStream() {
    LOGD("stopStream(): ");
    if (stream != nullptr && stream->getState() != oboe::StreamState::Closed) {
        oboe::Result result = stream->stop(0L);

        oboe::StreamState inputState = oboe::StreamState::Stopping;
        oboe::StreamState nextState = oboe::StreamState::Uninitialized;
        int64_t millisecondsPerNanosecond = 1000000;
        int64_t timeoutNanos = 100 * millisecondsPerNanosecond;

        stream->waitForStateChange(inputState, &nextState, timeoutNanos);

        if (result != oboe::Result::OK) {
            LOGE(TAG, "Error stopping the stream: %s");
            LOGE(TAG, oboe::convertToText(result));
        }
        LOGW(TAG, "stopStream(): Total samples = ");
        LOGW(TAG, to_string(mRecordingIO->getTotalSamples()).c_str());
    }
}

void BaseStream::closeStream() {
    LOGD("closeStream(): ");

    oboe::StreamState inputState = oboe::StreamState::Closing;
    oboe::StreamState nextState = oboe::StreamState::Uninitialized;
    int64_t millisecondsPerNanosecond = 1000000;
    int64_t timeoutNanos = 100 * millisecondsPerNanosecond;
    if (stream) {
        stream->waitForStateChange(inputState, &nextState, timeoutNanos);
        if (nextState != oboe::StreamState::Closed) {
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
}
