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
        if (kotlinMethodIdsPtr && kotlinMethodIdsPtr->recordingScreenVM) {
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
        if (!recordingEngine) {
            auto appDir = java_str_to_c_str(env, appDirStr);
            auto recordingSessionId = java_str_to_c_str(env, recordingSessionIdStr);

            prepare_kotlin_method_ids(env);

            recordingEngine = new RecordingEngine(appDir, recordingSessionId, recordingScreenViewModelPassed);
        }
        return (recordingEngine != nullptr);
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_screens_recording_RecordingEngine_delete(JNIEnv *env, jclass) {
        if (!recordingEngine) {
            return;
        }
        delete_kotlin_global_refs(env);
        delete recordingEngine;
        recordingEngine = nullptr;
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_screens_recording_RecordingEngine_startRecording(JNIEnv *env, jclass) {
        if (!recordingEngine) {
            LOGE("startRecording: recordingEngine is null, you must call create() method before calling this method");
        }
        recordingEngine->startRecording();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_screens_recording_RecordingEngine_stopRecording(JNIEnv *env, jclass) {
        if (!recordingEngine) {
            LOGE("stopRecording: recordingEngine is null, you must call create() method before calling this method");
            return;
        }
        recordingEngine->stopRecording();
    }

    JNIEXPORT jboolean JNICALL
    Java_com_bluehub_fastmixer_screens_recording_RecordingEngine_startPlayback(JNIEnv * env, jclass) {
        if (!recordingEngine) {
            LOGE("startPlayback: recordingEngine is null, you must call create() method before calling this method");
            return false;
        }
        return recordingEngine->startPlayback();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_screens_recording_RecordingEngine_stopPlayback(JNIEnv *env, jclass) {
        if (!recordingEngine) {
            LOGE("stopPlayback: recordingEngine is null, you must call create() method before calling this method");
            return;
        }
        recordingEngine->stopAndResetPlayback();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_screens_recording_RecordingEngine_pausePlayback(JNIEnv *env, jclass) {
        if (!recordingEngine) {
            LOGE("pausePlayback: recordingEngine is null, you must call create() method before calling this method");
            return;
        }
        recordingEngine->pausePlayback();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_screens_recording_RecordingEngine_startLivePlayback(JNIEnv *env, jclass) {
        if (!recordingEngine) {
            LOGE("startLivePlayback: recordingEngine is null, you must call create() method before calling this method");
            return;
        }
        recordingEngine->startLivePlayback();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_screens_recording_RecordingEngine_stopLivePlayback(JNIEnv *env, jclass) {
        if (!recordingEngine) {
            LOGE("stopLivePlayback: recordingEngine is null, you must call create() method before calling this method");
            return;
        }
        recordingEngine->stopLivePlayback();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_screens_recording_RecordingEngine_flushWriteBuffer(JNIEnv *env, jclass) {
        if (!recordingEngine) {
            LOGE("flushWriteBuffer: recordingEngine is null, you must call create() method before calling this method");
            return;
        }
        recordingEngine->flushWriteBuffer();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_screens_recording_RecordingEngine_restartPlayback(JNIEnv *env, jclass) {
        if (!recordingEngine) {
            LOGE("restartPlayback: recordingEngine is null, you must call create() method before calling this method");
            return;
        }
        recordingEngine->restartPlayback();
    }

    JNIEXPORT jint JNICALL
    Java_com_bluehub_fastmixer_screens_recording_RecordingEngine_getCurrentMax(JNIEnv * env, jclass) {
        if (!recordingEngine) {
            LOGE("getCurrentMax: recordingEngine is null, you must call create() method before calling this method");
            return 0;
        }
        int currentMax = recordingEngine->getCurrentMax();
        return currentMax;
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_screens_recording_RecordingEngine_resetCurrentMax(JNIEnv *env, jclass) {
        if (!recordingEngine) {
            LOGE("resetCurrentMax: recordingEngine is null, you must call create() method before calling this method");
            return;
        }
        recordingEngine->resetCurrentMax();
    }

    JNIEXPORT jint JNICALL
    Java_com_bluehub_fastmixer_screens_recording_RecordingEngine_getTotalRecordedFrames(JNIEnv * env, jclass) {
        if (!recordingEngine) {
            LOGE("getTotalRecordedFrames: recordingEngine is null, you must call create() method before calling this method");
            return 0;
        }
        return recordingEngine->getTotalRecordedFrames();
    }

    JNIEXPORT jint JNICALL
    Java_com_bluehub_fastmixer_screens_recording_RecordingEngine_getCurrentPlaybackProgress(JNIEnv * env, jclass) {
        if (!recordingEngine) {
            LOGE("getCurrentPlaybackProgress: recordingEngine is null, you must call create() method before calling this method");
            return 0;
        }
        return recordingEngine->getCurrentPlaybackProgress();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_screens_recording_RecordingEngine_setPlayHead(JNIEnv *env, jclass, jint position) {
        if (!recordingEngine) {
            LOGE("setPlayHead: recordingEngine is null, you must call create() method before calling this method");
            return;
        }
        recordingEngine->setPlayHead(position);
    }

    JNIEXPORT jint JNICALL
    Java_com_bluehub_fastmixer_screens_recording_RecordingEngine_getDurationInSeconds(JNIEnv * env, jclass) {
        if (!recordingEngine) {
            LOGE("getDurationInSeconds: recordingEngine is null, you must call create() method before calling this method");
            return 0;
        }
        return recordingEngine->getDurationInSeconds();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_screens_recording_RecordingEngine_resetRecordingEngine(JNIEnv *env, jclass) {
        if (!recordingEngine) {
            LOGE("resetAudioEngine: recordingEngine is null, you must call create() method before calling this method");
            return;
        }
        return recordingEngine->resetAudioEngine();
    }
}
