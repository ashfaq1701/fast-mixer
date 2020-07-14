//
// Created by asalehin on 7/11/20.
//

#include <cstdint>
#include <cstdio>
#include "SoundRecording.h"
#include "logging_macros.h"

int32_t SoundRecording::write(const int16_t *sourceData, int32_t numSamples, char* recordingFilePath) {
    auto *buffer = new int16_t[numSamples]{0};

    for (int i = 0; i < numSamples; i++) {
        buffer[i] = gain_factor * sourceData[i];
    }

    FILE *f = fopen(recordingFilePath, "ab");
    fwrite(buffer, sizeof(*buffer), numSamples, f);
    fclose(f);
    return numSamples;
}
