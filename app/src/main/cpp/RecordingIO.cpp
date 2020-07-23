//
// Created by asalehin on 7/11/20.
//

#include <cstdint>
#include <cstdio>
#include <string>
#include <unistd.h>
#include "RecordingIO.h"
#include "logging_macros.h"
#include "Utils.h"
#include <mutex>
#include <condition_variable>
#include "Constants.h"

std::mutex RecordingIO::mtx;
std::condition_variable RecordingIO::reallocated;
bool RecordingIO::is_reallocated = false;

bool RecordingIO::check_if_reallocated() {
    return is_reallocated;
}

void RecordingIO::setup_audio_source() {
    if (!validate_audio_file()) {
        return;
    }

    if (mChannelCount == 0 || mSampleRate == 0) {
        return;
    }

    AudioProperties targetProperties {
            .channelCount = mChannelCount,
            .sampleRate = mSampleRate
    };

    std::shared_ptr<AAssetDataSource> audioSource {
            AAssetDataSource::newFromCompressedAsset(mAssetManager, mRecordingFilePath.c_str(), targetProperties)
    };

    mRecordedTrack = std::make_unique<Player>(audioSource);
    mRecordedTrack->setPlaying(true);
}

void RecordingIO::pause_audio_source() {
    if (mRecordedTrack == nullptr) {
        return;
    }
    mRecordedTrack->setPlaying(false);
}

void RecordingIO::stop_audio_source() {
    pause_audio_source();
    mRecordedTrack = nullptr;
}

bool RecordingIO::validate_audio_file() {
    return !(mRecordingFilePath.empty() || (access(mRecordingFilePath.c_str(), F_OK) == -1));
}

void RecordingIO::read_playback(float *targetData, int32_t numSamples) {
    LOGD(TAG, "read(): ");
    LOGD(TAG, std::to_string(numSamples).c_str());

    if (!validate_audio_file()) {
        return;
    }

    if (mRecordedTrack == nullptr) {
        return;
    }

    if (this->mTotalReadPlayback < mTotalSamples) {
        mRecordedTrack->renderAudio(targetData, numSamples);
    }
}

void RecordingIO::flush_to_file(int16_t* buffer, int length, const std::string& recordingFilePath) {
    FILE* f = fopen(recordingFilePath.c_str(), "ab");
    fwrite(buffer, sizeof(*buffer), length, f);
    fclose(f);
    std::unique_lock<std::mutex> lck(mtx);
    reallocated.wait(lck, check_if_reallocated);
    delete[] buffer;
    is_reallocated = false;
}

void RecordingIO::perform_flush(int flushIndex) {
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

int32_t RecordingIO::write(const int16_t *sourceData, int32_t numSamples) {
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

void RecordingIO::flush_buffer() {
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

int32_t RecordingIO::read_live_playback(int16_t *targetData, int32_t numSamples) {
    int32_t framesRead = 0;
    while (framesRead < numSamples && mLivePlaybackReadIndex < mTotalSamples) {
        targetData[framesRead++] = mData[mLivePlaybackReadIndex++];
    }
    return framesRead;
}