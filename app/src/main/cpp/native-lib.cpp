#ifndef MODULE_NAME
#define MODULE_NAME  "native-lib"
#endif

#include <jni.h>
#include <string>
#include "AudioEngine.h"
#include "logging_macros.h"
#include "jni_env.h"
#include <android/asset_manager_jni.h>

const char *TAG = "native-lib: %s";
static AudioEngine *audioEngine = nullptr;

extern "C" {
    method_ids prepare_kotlin_method_ids(JNIEnv *env) {
        jclass recordingVMClass = env->FindClass("com/bluehub/fastmixer/screens/recording/RecordingScreenViewModel");
        auto recordingVmGlobal = env->NewGlobalRef(recordingVMClass);
        jmethodID togglePlay = env->GetStaticMethodID(static_cast<jclass>(recordingVmGlobal), "setStopPlay", "()V");

        method_ids kotlinMethodIds {
            .recordingScreenVM = static_cast<jclass>(env->NewGlobalRef(recordingVmGlobal)),
            .recordingScreenVMTogglePlay = togglePlay
        };
        return kotlinMethodIds;
    }

    void delete_kotlin_global_refs(JNIEnv *env, std::shared_ptr<method_ids> kotlinMethodIds) {
        if (kotlinMethodIds != nullptr && kotlinMethodIdsPtr->recordingScreenVM != nullptr) {
            env->DeleteGlobalRef(kotlinMethodIds->recordingScreenVM);
            kotlinMethodIds.reset();
        }
    }

    extern "C" JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
        java_machine = vm;
        return  JNI_VERSION_1_6;
    }

    JNIEXPORT jboolean JNICALL
    Java_com_bluehub_fastmixer_common_audio_AudioEngine_create(JNIEnv *env, jclass, jstring appDirStr, jstring recordingSessionIdStr, jboolean  recordingScreenViewModelPassed) {
        if (audioEngine == nullptr) {
            char* appDir = const_cast<char *>(env->GetStringUTFChars(appDirStr, NULL));
            char* recordingSessionId = const_cast<char *>(env->GetStringUTFChars(
                    recordingSessionIdStr, NULL));

            method_ids kotlinMethodIds = prepare_kotlin_method_ids(env);

            kotlinMethodIdsPtr = make_shared<method_ids>(kotlinMethodIds);

            audioEngine = new AudioEngine(appDir, recordingSessionId, recordingScreenViewModelPassed);
        }
        return (audioEngine != nullptr);
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_common_audio_AudioEngine_delete(JNIEnv *env, jclass) {
        delete_kotlin_global_refs(env, kotlinMethodIdsPtr);
        delete audioEngine;
        audioEngine = nullptr;
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_common_audio_AudioEngine_startRecording(JNIEnv *env, jclass) {
        if (audioEngine == nullptr) {
            LOGE("startRecording: audioEngine is null, you must call create() method before calling this method");
        }
        audioEngine->startRecording();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_common_audio_AudioEngine_stopRecording(JNIEnv *env, jclass) {
        if (audioEngine == nullptr) {
            LOGE("stopRecording: audioEngine is null, you must call create() method before calling this method");
            return;
        }
        audioEngine->stopRecording();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_common_audio_AudioEngine_pauseRecording(JNIEnv *env, jclass) {
        if (audioEngine == nullptr) {
            LOGE("pauseRecording: audioEngine is null, you must call create() method before calling this method");
            return;
        }
        audioEngine->pauseRecording();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_common_audio_AudioEngine_startPlayback(JNIEnv *env, jclass) {
        if (audioEngine == nullptr) {
            LOGE("startPlayback: audioEngine is null, you must call create() method before calling this method");
            return;
        }
        audioEngine->startPlayback();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_common_audio_AudioEngine_stopPlayback(JNIEnv *env, jclass) {
        if (audioEngine == nullptr) {
            LOGE("stopPlayback: audioEngine is null, you must call create() method before calling this method");
            return;
        }
        audioEngine->stopAndResetPlayback();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_common_audio_AudioEngine_pausePlayback(JNIEnv *env, jclass) {
        if (audioEngine == nullptr) {
            LOGE("pausePlayback: audioEngine is null, you must call create() method before calling this method");
            return;
        }
        audioEngine->pausePlayback();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_common_audio_AudioEngine_startLivePlayback(JNIEnv *env, jclass) {
        if (audioEngine == nullptr) {
            LOGE("startLivePlayback: audioEngine is null, you must call create() method before calling this method");
            return;
        }
        audioEngine->startLivePlayback();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_common_audio_AudioEngine_stopLivePlayback(JNIEnv *env, jclass) {
        if (audioEngine == nullptr) {
            LOGE("stopLivePlayback: audioEngine is null, you must call create() method before calling this method");
            return;
        }
        audioEngine->stopLivePlayback();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_common_audio_AudioEngine_pauseLivePlayback(JNIEnv *env, jclass) {
        if (audioEngine == nullptr) {
            LOGE("pauseLivePlayback: audioEngine is null, you must call create() method before calling this method");
            return;
        }
        audioEngine->pauseLivePlayback();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_common_audio_AudioEngine_flushWriteBuffer(JNIEnv *env, jclass) {
        if (audioEngine == nullptr) {
            LOGE("flushWriteBuffer: audioEngine is null, you must call create() method before calling this method");
            return;
        }
        audioEngine->flushWriteBuffer();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_common_audio_AudioEngine_restartPlayback(JNIEnv *env, jclass) {
        if (audioEngine == nullptr) {
            LOGE("restartPlayback: audioEngine is null, you must call create() method before calling this method");
            return;
        }
        audioEngine->restartPlayback();
    }

    JNIEXPORT jint JNICALL
    Java_com_bluehub_fastmixer_common_audio_AudioEngine_getCurrentMax(JNIEnv *env, jclass) {
        if (audioEngine == nullptr) {
            LOGE("getCurrentMax: audioEngine is null, you must call create() method before calling this method");
            return 0;
        }
        int currentMax = audioEngine->getCurrentMax();
        return currentMax;
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_common_audio_AudioEngine_resetCurrentMax(JNIEnv *env, jclass) {
        if (audioEngine == nullptr) {
            LOGE("resetCurrentMax: audioEngine is null, you must call create() method before calling this method");
            return;
        }
        audioEngine->resetCurrentMax();
    }

    JNIEXPORT jint JNICALL
    Java_com_bluehub_fastmixer_common_audio_AudioEngine_getTotalRecordedFrames(JNIEnv *env, jclass) {
        if (audioEngine == nullptr) {
            LOGE("getTotalRecordedFrames: audioEngine is null, you must call create() method before calling this method");
            return 0;
        }
        return audioEngine->getTotalRecordedFrames();
    }

    JNIEXPORT jint JNICALL
    Java_com_bluehub_fastmixer_common_audio_AudioEngine_getCurrentPlaybackProgress(JNIEnv *env, jclass) {
        if (audioEngine == nullptr) {
            LOGE("getCurrentPlaybackProgress: audioEngine is null, you must call create() method before calling this method");
            return 0;
        }
        return audioEngine->getCurrentPlaybackProgress();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_common_audio_AudioEngine_setPlayHead(JNIEnv *env, jclass, jint position) {
        if (audioEngine == nullptr) {
            LOGE("setPlayHead: audioEngine is null, you must call create() method before calling this method");
            return;
        }
        audioEngine->setPlayHead(position);
    }

    JNIEXPORT jint JNICALL
    Java_com_bluehub_fastmixer_common_audio_AudioEngine_getDurationInSeconds(JNIEnv *env, jclass) {
        if (audioEngine == nullptr) {
            LOGE("getDurationInSeconds: audioEngine is null, you must call create() method before calling this method");
            return 0;
        }
        return audioEngine->getDurationInSeconds();
    }
}