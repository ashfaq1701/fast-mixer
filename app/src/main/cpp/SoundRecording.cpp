//
// Created by asalehin on 7/11/20.
//

#include <cstdint>
#include <cstdio>
#include <string>
#include "SoundRecording.h"
#include "logging_macros.h"
#include "Utils.h"

int32_t SoundRecording::write(const int16_t *sourceData, int32_t numSamples) {
    LOGD(TAG, "write(): ");
    if (mWriteIndex + numSamples > mIteration * kMaxSamples) {
        LOGW(TAG, "write: mWriteIndex + numSamples > kMaxSamples");
        mIteration++;
        int32_t newSize = mIteration * kMaxSamples;
        auto * newData = new int16_t[newSize]{0};
        std::copy(mData, mData + mTotalSamples, newData);
        delete[] mData;
        mData = newData;
    }

    for(int i = 0; i < numSamples; i++) {
        mData[mWriteIndex++] = sourceData[i] * gain_factor;
    }
    mTotalSamples += numSamples;
    return numSamples;
}

int32_t SoundRecording::read(int16_t *targetData, int32_t numSamples) {
    LOGD(TAG, "read(): ");
    int32_t framesRead = 0;
    while (framesRead < numSamples && mReadIndex < mTotalSamples) {
        targetData[framesRead++] = mData[mReadIndex++];
    }
    return framesRead;
}