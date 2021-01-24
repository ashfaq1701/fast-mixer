//
// Created by Ashfaq Salehin on 12/1/2021 AD.
//

#ifndef FAST_MIXER_STRUCTS_H
#define FAST_MIXER_STRUCTS_H

#include <jni.h>

struct mixing_method_ids {
    jclass mixingScreenVM;
    jmethodID mixingScreenVMSetStopPlayback;
};

struct recording_method_ids {
    jclass recordingScreenVM;
    jmethodID recordingScreenVMTogglePlay;
};

#endif //FAST_MIXER_STRUCTS_H
