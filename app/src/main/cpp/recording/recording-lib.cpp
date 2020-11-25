#ifndef MODULE_NAME
#define MODULE_NAME  "native-lib"
#endif

#include <jni.h>
#include <string>
#include "RecordingEngine.h"
#include "../logging_macros.h"
#include "recording_jvm_env.h"
#include <android/asset_manager_jni.h>

const char *TAG = "native-lib: %s";
static RecordingEngine *recordingEngine = nullptr;

extern "C" {
    void prepare_kotlin_method_ids(JNIEnv *env) {
        jclass recordingVMClass = env->FindClass("com/bluehub/fastmixer/screens/recording/RecordingScreenViewModel");
        auto recordingVmGlobal = env->NewGlobalRef(recordingVMClass);
        jmethodID togglePlay = env->GetStaticMethodID(static_cast<jclass>(recordingVmGlobal), "setStopPlay", "()V");

        method_ids kotlinMethodIds {
            .recordingScreenVM = static_cast<jclass>(env->NewGlobalRef(recordingVmGlobal)),
            .recordingScreenVMTogglePlay = togglePlay
        };
        kotlinMethodIdsPtr = make_shared<method_ids>(kotlinMethodIds);
    }

    void delete_kotlin_global_refs(JNIEnv *env) {
        if (kotlinMethodIdsPtr != nullptr && kotlinMethodIdsPtr->recordingScreenVM != nullptr) {
            env->DeleteGlobalRef(kotlinMethodIdsPtr->recordingScreenVM);
            kotlinMethodIdsPtr.reset();
            kotlinMethodIdsPtr = nullptr;
        }
    }

    extern "C" JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
        java_recording_machine = vm;
        return  JNI_VERSION_1_6;
    }

    JNIEXPORT jboolean JNICALL
    Java_com_bluehub_fastmixer_screens_recording_RecordingEngine_create(JNIEnv *env, jclass, jstring appDirStr, jstring recordingSessionIdStr, jboolean  recordingScreenViewModelPassed) {
        if (recordingEngine == nullptr) {
            auto appDir = java_str_to_c_str(env, appDirStr);
            auto recordingSessionId = java_str_to_c_str(env, recordingSessionIdStr);

            prepare_kotlin_method_ids(env);

            recordingEngine = new RecordingEngine(appDir, recordingSessionId, recordingScreenViewModelPassed);
        }
        return (recordingEngine != nullptr);
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_screens_recording_RecordingEngine_delete(JNIEnv *env, jclass) {
        delete_kotlin_global_refs(env);
        if (recordingEngine == nullptr) {
            return;
        }
        delete recordingEngine;
        recordingEngine = nullptr;
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_screens_recording_RecordingEngine_startRecording(JNIEnv *env, jclass) {
        if (recordingEngine == nullptr) {
            LOGE("startRecording: recordingEngine is null, you must call create() method before calling this method");
        }
        recordingEngine->startRecording();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_screens_recording_RecordingEngine_stopRecording(JNIEnv *env, jclass) {
        if (recordingEngine == nullptr) {
            LOGE("stopRecording: recordingEngine is null, you must call create() method before calling this method");
            return;
        }
        recordingEngine->stopRecording();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_screens_recording_RecordingEngine_pauseRecording(JNIEnv *env, jclass) {
        if (recordingEngine == nullptr) {
            LOGE("pauseRecording: recordingEngine is null, you must call create() method before calling this method");
            return;
        }
        recordingEngine->pauseRecording();
    }

    JNIEXPORT jboolean JNICALL
    Java_com_bluehub_fastmixer_screens_recording_RecordingEngine_startPlayback(JNIEnv * env, jclass) {
        if (recordingEngine == nullptr) {
            LOGE("startPlayback: recordingEngine is null, you must call create() method before calling this method");
            return false;
        }
        return recordingEngine->startPlayback();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_screens_recording_RecordingEngine_stopPlayback(JNIEnv *env, jclass) {
        if (recordingEngine == nullptr) {
            LOGE("stopPlayback: recordingEngine is null, you must call create() method before calling this method");
            return;
        }
        recordingEngine->stopAndResetPlayback();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_screens_recording_RecordingEngine_pausePlayback(JNIEnv *env, jclass) {
        if (recordingEngine == nullptr) {
            LOGE("pausePlayback: recordingEngine is null, you must call create() method before calling this method");
            return;
        }
        recordingEngine->pausePlayback();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_screens_recording_RecordingEngine_startLivePlayback(JNIEnv *env, jclass) {
        if (recordingEngine == nullptr) {
            LOGE("startLivePlayback: recordingEngine is null, you must call create() method before calling this method");
            return;
        }
        recordingEngine->startLivePlayback();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_screens_recording_RecordingEngine_stopLivePlayback(JNIEnv *env, jclass) {
        if (recordingEngine == nullptr) {
            LOGE("stopLivePlayback: recordingEngine is null, you must call create() method before calling this method");
            return;
        }
        recordingEngine->stopLivePlayback();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_screens_recording_RecordingEngine_pauseLivePlayback(JNIEnv *env, jclass) {
        if (recordingEngine == nullptr) {
            LOGE("pauseLivePlayback: recordingEngine is null, you must call create() method before calling this method");
            return;
        }
        recordingEngine->pauseLivePlayback();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_screens_recording_RecordingEngine_flushWriteBuffer(JNIEnv *env, jclass) {
        if (recordingEngine == nullptr) {
            LOGE("flushWriteBuffer: recordingEngine is null, you must call create() method before calling this method");
            return;
        }
        recordingEngine->flushWriteBuffer();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_screens_recording_RecordingEngine_restartPlayback(JNIEnv *env, jclass) {
        if (recordingEngine == nullptr) {
            LOGE("restartPlayback: recordingEngine is null, you must call create() method before calling this method");
            return;
        }
        recordingEngine->restartPlayback();
    }

    JNIEXPORT jint JNICALL
    Java_com_bluehub_fastmixer_screens_recording_RecordingEngine_getCurrentMax(JNIEnv * env, jclass) {
        if (recordingEngine == nullptr) {
            LOGE("getCurrentMax: recordingEngine is null, you must call create() method before calling this method");
            return 0;
        }
        int currentMax = recordingEngine->getCurrentMax();
        return currentMax;
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_screens_recording_RecordingEngine_resetCurrentMax(JNIEnv *env, jclass) {
        if (recordingEngine == nullptr) {
            LOGE("resetCurrentMax: recordingEngine is null, you must call create() method before calling this method");
            return;
        }
        recordingEngine->resetCurrentMax();
    }

    JNIEXPORT jint JNICALL
    Java_com_bluehub_fastmixer_screens_recording_RecordingEngine_getTotalRecordedFrames(JNIEnv * env, jclass) {
        if (recordingEngine == nullptr) {
            LOGE("getTotalRecordedFrames: recordingEngine is null, you must call create() method before calling this method");
            return 0;
        }
        return recordingEngine->getTotalRecordedFrames();
    }

    JNIEXPORT jint JNICALL
    Java_com_bluehub_fastmixer_screens_recording_RecordingEngine_getCurrentPlaybackProgress(JNIEnv * env, jclass) {
        if (recordingEngine == nullptr) {
            LOGE("getCurrentPlaybackProgress: recordingEngine is null, you must call create() method before calling this method");
            return 0;
        }
        return recordingEngine->getCurrentPlaybackProgress();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_screens_recording_RecordingEngine_setPlayHead(JNIEnv *env, jclass, jint position) {
        if (recordingEngine == nullptr) {
            LOGE("setPlayHead: recordingEngine is null, you must call create() method before calling this method");
            return;
        }
        recordingEngine->setPlayHead(position);
    }

    JNIEXPORT jint JNICALL
    Java_com_bluehub_fastmixer_screens_recording_RecordingEngine_getDurationInSeconds(JNIEnv * env, jclass) {
        if (recordingEngine == nullptr) {
            LOGE("getDurationInSeconds: recordingEngine is null, you must call create() method before calling this method");
            return 0;
        }
        return recordingEngine->getDurationInSeconds();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_screens_recording_RecordingEngine_resetRecordingEngine(JNIEnv *env, jclass) {
        if (recordingEngine == nullptr) {
            LOGE("resetAudioEngine: recordingEngine is null, you must call create() method before calling this method");
            return;
        }
        return recordingEngine->resetAudioEngine();
    }
}