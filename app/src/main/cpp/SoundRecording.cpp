//
// Created by asalehin on 7/11/20.
//

#include <cstdint>
#include <cstdio>
#include <string>
#include "SoundRecording.h"
#include "logging_macros.h"
#include "Utils.h"

void SoundRecording::read_playback_runnable(int16_t *targetData, int32_t numSamples, SoundRecording* soundRecording) {
    LOGD(soundRecording->TAG, "readPlayback(): ");
    LOGD(soundRecording->TAG, std::to_string(numSamples).c_str());

    int32_t framesRead = 0;
    if (soundRecording->isPlaybackFpOpen) {
        framesRead = fread(targetData, sizeof(int16_t), numSamples, soundRecording->playbackFp);
        soundRecording->mTotalReadPlayback += framesRead;
    }
}

void SoundRecording::read_playback(int16_t *targetData, int32_t numSamples) {
    LOGD(TAG, "read(): ");
    LOGD(TAG, std::to_string(numSamples).c_str());
    // Live playback in main thread to prevent occasional lag that is being happened
    read_playback_runnable(targetData, numSamples, this);
}

void SoundRecording::flush_to_file(int16_t* buffer, int length, const std::string& recordingFilePath) {
    FILE* f = fopen(recordingFilePath.c_str(), "ab");
    fwrite(buffer, sizeof(*buffer), length, f);
    fclose(f);
}

int32_t SoundRecording::write(const int16_t *sourceData, int32_t numSamples) {
    LOGD(TAG, "write(): ");

    if (readyToFlush) {
        if (livePlaybackEnabled && mLivePlaybackReadIndex >= kMaxSamples) {
            toFlush = true;
        } else if (!livePlaybackEnabled) {
            toFlush = true;
        }
    }

    if (toFlush) {
        int16_t* oldBuffer = mData;
        taskQueue->enqueue(flush_to_file, oldBuffer, kMaxSamples * (mIteration - 1), mRecordingFilePath);

        auto * newData = new int16_t[kMaxSamples]{0};
        std::copy(mData + kMaxSamples * (mIteration - 1), mData + mWriteIndex, newData);
        mData = newData;
        mWriteIndex -= kMaxSamples;
        mLivePlaybackReadIndex -= kMaxSamples;
        readyToFlush = false;
        toFlush = false;
        mIteration = 1;
    }

    if (mWriteIndex + numSamples > mIteration * kMaxSamples) {
        LOGW(TAG, "write: mWriteIndex + numSamples > kMaxSamples");
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

void SoundRecording::flush_buffer() {
    if (mWriteIndex > 0) {
        int16_t* oldBuffer = mData;
        taskQueue->enqueue(flush_to_file, oldBuffer, static_cast<int>(mWriteIndex), mRecordingFilePath);

        mIteration = 1;
        mWriteIndex = 0;
        mLivePlaybackReadIndex = 0;
        mData = new int16_t[kMaxSamples]{0};
        readyToFlush = false;
        toFlush = false;
    }
}

int32_t SoundRecording::read_live_playback(int16_t *targetData, int32_t numSamples) {
    int32_t framesRead = 0;
    while (framesRead < numSamples && mLivePlaybackReadIndex < mTotalSamples) {
        targetData[framesRead++] = mData[mLivePlaybackReadIndex++];
    }
    return framesRead;
}

void SoundRecording::openPlaybackFp() {
    if (!isPlaybackFpOpen) {
        playbackFp = fopen(mRecordingFilePath.c_str(), "rb");
        isPlaybackFpOpen = true;
        fseek(playbackFp, mTotalReadPlayback * sizeof(int16_t), SEEK_SET);
    }
}

void SoundRecording::closePlaybackFp() {
    if (isPlaybackFpOpen) {
        fclose(playbackFp);
        isPlaybackFpOpen = false;
    }
}