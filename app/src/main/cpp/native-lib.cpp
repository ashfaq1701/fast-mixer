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
    Java_com_bluehub_fastmixer_common_audio_AudioEngine_create(JNIEnv *env, jclass, jstring appDirStr, jstring recordingSessionIdStr) {
        LOGD(TAG, "create(): ");
        if (audioEngine == nullptr) {
            const char* appDir = env->GetStringUTFChars(appDirStr, NULL);
            const char* recordingSessionId = env->GetStringUTFChars(recordingSessionIdStr, NULL);
            audioEngine = new AudioEngine(appDir, recordingSessionId);
        }
        return (audioEngine != nullptr);
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_common_audio_AudioEngine_delete(JNIEnv *env, jclass) {
        LOGD(TAG, "delete(): ");
        delete audioEngine;
        audioEngine = nullptr;
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_common_audio_AudioEngine_startRecording(JNIEnv *env, jclass) {
        LOGD(TAG, "startRecording(): ");
        if (audioEngine == nullptr) {
            LOGE("audioEngine is null, you must call create() method before calling this method");
            return;
        }
        audioEngine->startRecording();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_common_audio_AudioEngine_stopRecording(JNIEnv *env, jclass) {
        LOGD(TAG, "stopRecording(): ");
        if (audioEngine == nullptr) {
            LOGE("audioEngine is null, you must call create() method before calling this method");
            return;
        }
        audioEngine->stopRecording();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_common_audio_AudioEngine_pauseRecording(JNIEnv *env, jclass) {
    LOGD(TAG, "pauseRecording(): ");
    if (audioEngine == nullptr) {
        LOGE("audioEngine is null, you must call create() method before calling this method");
        return;
    }
    audioEngine->pauseRecording();
}


}