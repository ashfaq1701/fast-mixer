//
// Created by asalehin on 7/21/20.
//

#ifndef FAST_MIXER_CONSTANTS_H
#define FAST_MIXER_CONSTANTS_H

#include <jni.h>

struct AudioProperties {
    int32_t channelCount;
    int32_t sampleRate;
};

struct buffer_data {
    float *ptr;
    size_t numSamples; ///< size left in the buffer
};

struct method_ids {
    jclass recordingScreenVM;
    jmethodID recordingScreenVMTogglePlay;
};

#endif //FAST_MIXER_CONSTANTS_H
