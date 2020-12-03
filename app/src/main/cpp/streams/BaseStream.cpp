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

        oboe::StreamState inputState = oboe::StreamState::Starting;
        oboe::StreamState nextState = oboe::StreamState::Uninitialized;
        int64_t nanosecondsPerMillisecond = 1000000;
        int64_t timeoutNanos = 100 * nanosecondsPerMillisecond;

        stream->waitForStateChange(inputState, &nextState, timeoutNanos);

        if (result != oboe::Result::OK) {
            LOGE(TAG, "Error starting the stream: %s", oboe::convertToText(result));
        }
    }
}

void BaseStream::stopStream() {
    LOGD("stopStream(): ");
    if (stream &&
        stream->getState() != oboe::StreamState::Closing &&
        stream->getState() != oboe::StreamState::Closed) {
        oboe::Result result = stream->requestStop();

        oboe::StreamState inputState = oboe::StreamState::Stopping;
        oboe::StreamState nextState = oboe::StreamState::Uninitialized;
        int64_t nanosecondsPerMillisecond = 1000000;
        int64_t timeoutNanos = 100 * nanosecondsPerMillisecond;

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

    if (stream &&
        stream->getState() != oboe::StreamState::Closing &&
        stream->getState() != oboe::StreamState::Closed) {
        oboe::Result result = stream->close();

        oboe::StreamState inputState = oboe::StreamState::Closing;
        oboe::StreamState nextState = oboe::StreamState::Uninitialized;
        int64_t nanosecondsPerMillisecond = 1000000;
        int64_t timeoutNanos = 100 * nanosecondsPerMillisecond;

        if (!stream) return;

        stream->waitForStateChange(inputState, &nextState, timeoutNanos);

        if (result != oboe::Result::OK) {
            LOGE(TAG, "Error closing stream. %s", oboe::convertToText(result));
        } else {
            stream = nullptr;
        }

        LOGW(TAG, "closeStream(): mTotalSamples = ");
        LOGW(TAG, to_string(mRecordingIO->getTotalSamples()).c_str());
    }
}
