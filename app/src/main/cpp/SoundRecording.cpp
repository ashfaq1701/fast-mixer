//
// Created by asalehin on 7/11/20.
//

#include <cstdint>
#include <cstdio>
#include "SoundRecording.h"
#include "logging_macros.h"

int32_t SoundRecording::write(const int16_t *sourceData, int32_t numSamples) {
    auto *buffer = new int16_t[numSamples]{0};

    for (int i = 0; i < numSamples; i++) {
        buffer[i] = gain_factor * sourceData[i];
    }

    FILE *f = fopen(mRecordingFilePath, "ab");
    fwrite(buffer, sizeof(*buffer), numSamples, f);
    fclose(f);

    mTotalSamples += numSamples;
    return numSamples;
}

void SoundRecording::setRecordingFilePath(char* recordingFilePath) {
    mRecordingFilePath = recordingFilePath;
}
