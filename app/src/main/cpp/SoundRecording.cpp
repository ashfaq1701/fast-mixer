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

    FILE* recordingFp = fopen(mRecordingFilePath.c_str(), "ab");
    fwrite(buffer, sizeof(*buffer), numSamples, recordingFp);
    fclose(recordingFp);

    mTotalSamples += numSamples;
    return numSamples;
}

int32_t SoundRecording::read(int16_t *targetData, int32_t numSamples) {
    LOGD(TAG, "read(): ");
    LOGD(TAG, std::to_string(numSamples).c_str());
    int32_t framesRead = 0;

    FILE* livePlaybackFp = fopen(mRecordingFilePath.c_str(), "rb");
    fseek(livePlaybackFp, mTotalRead * sizeof(int16_t), SEEK_SET);
    framesRead = fread(targetData, sizeof(int16_t), numSamples, livePlaybackFp);

    mTotalRead += framesRead;

    fclose(livePlaybackFp);
    return framesRead;
}