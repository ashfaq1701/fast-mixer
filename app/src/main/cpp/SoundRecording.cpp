//
// Created by asalehin on 7/11/20.
//

#include <cstdint>
#include <cstdio>
#include "SoundRecording.h"
#include "logging_macros.h"

int32_t SoundRecording::write(const int16_t *sourceData, int32_t numSamples, char* recordingFilePath) {
    FILE *f = fopen(recordingFilePath, "ab");
    fwrite(sourceData, sizeof(*sourceData), numSamples, f);
    fclose(f);
    return numSamples;
}
