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
    auto *buffer = new int16_t[numSamples]{0};

    for (int i = 0; i < numSamples; i++) {
        buffer[i] = gain_factor * sourceData[i];
    }

    fwrite(buffer, sizeof(*buffer), numSamples, recordingFp);
    fflush(recordingFp);

    mTotalSamples += numSamples;
    return numSamples;
}

int32_t SoundRecording::read(int16_t *targetData, int32_t numSamples) {
    LOGD(TAG, "read(): ");
    LOGD(TAG, std::to_string(numSamples).c_str());
    int32_t framesRead = 0;

    framesRead = fread(targetData, sizeof(int16_t), numSamples, livePlaybackFp);

    mTotalRead += framesRead;
    return framesRead;
}

void SoundRecording::openRecordingFp() {
    if (!isRecordingFpOpen) {
        recordingFp = fopen(mRecordingFilePath.c_str(), "ab");
        isRecordingFpOpen = true;
    }
}

void SoundRecording::closeRecordingFp() {
    if (isRecordingFpOpen) {
        fclose(recordingFp);
        isRecordingFpOpen = false;
    }
}

void SoundRecording::openLivePlaybackFp() {
    if (!isLiveFpOpen) {
        livePlaybackFp = fopen(mRecordingFilePath.c_str(), "rb");
        isLiveFpOpen = true;
        fseek(livePlaybackFp, mTotalRead * sizeof(int16_t), SEEK_SET);
    }
}

void SoundRecording::closeLivePlaybackFp() {
    if (isLiveFpOpen) {
        fclose(livePlaybackFp);
        isLiveFpOpen = false;
    }
}