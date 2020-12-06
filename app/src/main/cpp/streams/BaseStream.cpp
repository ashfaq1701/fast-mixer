//
// Created by asalehin on 7/30/20.
//

#include <cassert>
#include "BaseStream.h"

BaseStream::BaseStream(RecordingIO* recordingIO, mutex& mtx): mLock(mtx) {
    mRecordingIO = recordingIO;
}

oboe::Result BaseStream::startStream() {
    lock_guard<mutex> lock(mLock);
    LOGD(TAG, "startStream(): ");

    if (!mStream) {
        oboe::Result openResult = openStream();

        if (openResult != oboe::Result::OK) {
            return openResult;
        }
    }
    oboe::Result result = mStream->start();

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

void BaseStream::resetStream() {
    if (mStream) {
        mStream.reset();
    }
}