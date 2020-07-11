#ifndef MODULE_NAME
#define MODULE_NAME  "native-lib"
#endif

#include <jni.h>
#include <string>
#include "AudioEngine.h"
#include "logging_macros.h"

const char *TAG = "native-lib: %s";
static AudioEngine *audioEngine = nullptr;

extern "C" {
    JNIEXPORT jboolean JNICALL
    Java_com_bluehub_fastmixer_common_audio_AudioEngine_create(JNIEnv *env, jclass) {
        LOGD(TAG, "create(): ");
        if (audioEngine == nullptr) {
            audioEngine = new AudioEngine();
        }
        return (audioEngine != nullptr);
    }
}