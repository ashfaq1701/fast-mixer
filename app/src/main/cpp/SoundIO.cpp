//
// Created by asalehin on 7/11/20.
//

#include <cstdint>
#include <cstdio>
#include <string>
#include <unistd.h>
#include "SoundIO.h"
#include "logging_macros.h"
#include "Utils.h"
#include <mutex>
#include <condition_variable>
#include "Constants.h"

std::mutex SoundIO::mtx;
std::condition_variable SoundIO::reallocated;
bool SoundIO::is_reallocated = false;

bool SoundIO::check_if_reallocated() {
    return is_reallocated;
}

void SoundIO::read_playback_runnable(int16_t *targetData, int32_t numSamples, SoundIO* soundIO) {
    LOGD(soundIO->TAG, "readPlayback(): ");
    LOGD(soundIO->TAG, std::to_string(numSamples).c_str());

    int32_t framesRead = 0;
    if (soundIO->isPlaybackFpOpen) {
        framesRead = fread(targetData, sizeof(int16_t), numSamples, soundIO->playbackFp);
        soundIO->mTotalReadPlayback += framesRead;
    }
}

void SoundIO::read_playback(int16_t *targetData, int32_t numSamples) {
    LOGD(TAG, "read(): ");
    LOGD(TAG, std::to_string(numSamples).c_str());
    if (this->mTotalReadPlayback < mTotalSamples) {
        read_playback_runnable(targetData, numSamples, this);
    }
}

void SoundIO::flush_to_file(int16_t* buffer, int length, const std::string& recordingFilePath) {
    FILE* f = fopen(recordingFilePath.c_str(), "ab");
    fwrite(buffer, sizeof(*buffer), length, f);
    fclose(f);
    std::unique_lock<std::mutex> lck(mtx);
    reallocated.wait(lck, check_if_reallocated);
    delete[] buffer;
    is_reallocated = false;
}

void SoundIO::perform_flush(int flushIndex) {
    int16_t* oldBuffer = mData;
    is_reallocated = false;
    taskQueue->enqueue(flush_to_file, oldBuffer, flushIndex, mRecordingFilePath);

    auto * newData = new int16_t[kMaxSamples]{0};
    std::copy(mData + flushIndex, mData + mWriteIndex, newData);
    mData = newData;
    is_reallocated = true;
    mWriteIndex -= flushIndex;
    mLivePlaybackReadIndex -= flushIndex;
    readyToFlush = false;
    toFlush = false;
    mIteration = 1;
}

int32_t SoundIO::write(const int16_t *sourceData, int32_t numSamples) {
    LOGD(TAG, "write(): ");

    if (mWriteIndex + numSamples > kMaxSamples) {
        readyToFlush = true;
    }

    int flushIndex = 0;
    if (readyToFlush) {
        int upperBound  = 0;
        if (mWriteIndex < kMaxSamples) {
            upperBound = mWriteIndex;
        } else {
            upperBound = kMaxSamples;
        }
        if (livePlaybackEnabled && mLivePlaybackReadIndex >= upperBound) {
            flushIndex = upperBound;
            toFlush = true;
        } else if (!livePlaybackEnabled) {
            flushIndex = mWriteIndex;
            toFlush = true;
        }
    }

    if (toFlush) {
        perform_flush(flushIndex);
    }

    if (mWriteIndex + numSamples > mIteration * kMaxSamples) {
        readyToFlush = true;
        mIteration++;
        int32_t newSize = mIteration * kMaxSamples;
        auto * newData = new int16_t[newSize]{0};
        std::copy(mData, mData + mWriteIndex, newData);
        delete[] mData;
        mData = newData;
    }

    for(int i = 0; i < numSamples; i++) {
        mData[mWriteIndex++] = sourceData[i] * gain_factor;
    }
    mTotalSamples += numSamples;

    return numSamples;
}

void SoundIO::flush_buffer() {
    if (mWriteIndex > 0) {
        int16_t* oldBuffer = mData;
        is_reallocated = false;
        taskQueue->enqueue(flush_to_file, oldBuffer, static_cast<int>(mWriteIndex), mRecordingFilePath);

        mIteration = 1;
        mWriteIndex = 0;
        mLivePlaybackReadIndex = 0;
        mData = new int16_t[kMaxSamples]{0};
        is_reallocated = true;
        readyToFlush = false;
        toFlush = false;
    }
}

int32_t SoundIO::read_live_playback(int16_t *targetData, int32_t numSamples) {
    int32_t framesRead = 0;
    while (framesRead < numSamples && mLivePlaybackReadIndex < mTotalSamples) {
        targetData[framesRead++] = mData[mLivePlaybackReadIndex++];
    }
    return framesRead;
}

void SoundIO::openPlaybackFp() {
    if (!isPlaybackFpOpen) {
        playbackFp = fopen(mRecordingFilePath.c_str(), "rb");
        if (playbackFp != nullptr) {
            isPlaybackFpOpen = true;
            fseek(playbackFp, mTotalReadPlayback * sizeof(int16_t), SEEK_SET);
        }
    }
}

void SoundIO::closePlaybackFp() {
    if (isPlaybackFpOpen) {
        fclose(playbackFp);
        isPlaybackFpOpen = false;
    }
}