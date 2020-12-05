//
// Created by asalehin on 7/30/20.
//

#include <cassert>
#include "BaseStream.h"

BaseStream::BaseStream(RecordingIO* recordingIO) {
    mRecordingIO = recordingIO;
}

oboe::Result BaseStream::startStream() {
    lock_guard<mutex> lock(mLock);
    LOGD(TAG, "startStream(): ");

    oboe::Result result;

    if (!mStream) {
        result = openStream();

        if (result != oboe::Result::OK) {
            return result;
        }
    }
    result = mStream->start();

    if (result != oboe::Result::OK) {
        LOGE(TAG, "Error starting the stream: %s", oboe::convertToText(result));
    }

    return result;
}

void BaseStream::stopStream() {
    lock_guard<mutex> lock(mLock);
    LOGD(TAG, "stopStream(): ");
    if (mStream) {
        mStream->stop();
        mStream->close();
        mStream.reset();
    }
}