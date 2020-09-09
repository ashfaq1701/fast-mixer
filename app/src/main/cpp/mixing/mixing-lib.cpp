//
// Created by asalehin on 9/9/20.
//

#ifndef MODULE_NAME
#define MODULE_NAME  "mixing-lib"
#endif

#include <memory>
#include <jni.h>
#include "MixingEngine.h"
#include "../logging_macros.h"
#include "../jvm_env.h"

const char *TAG = "mixing-lib: %s";
static MixingEngine *mixingEngine = nullptr;

extern "C" {
    extern "C" JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
        java_machine = vm;
        return  JNI_VERSION_1_6;
    }

    JNIEXPORT jboolean JNICALL
    Java_com_bluehub_fastmixer_screens_mixing_MixingEngine_create(JNIEnv *env, jclass) {
        if (mixingEngine == nullptr) {
            mixingEngine = new MixingEngine();
        }
        return (mixingEngine != nullptr);
    }
}

