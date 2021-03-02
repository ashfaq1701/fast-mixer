#ifndef MODULE_NAME
#define MODULE_NAME  "native-lib"
#endif

#include <jni.h>
#include <string>
#include "RecordingEngine.h"
#include "../logging_macros.h"
#include "../jvm_env.h"
#include <android/asset_manager_jni.h>

static RecordingEngine *recordingEngine = nullptr;

extern "C" {
    void prepare_kotlin_recording_method_ids(JNIEnv *env) {
        jclass recordingVMClass = env->FindClass("com/bluehub/fastmixer/screens/recording/RecordingScreenViewModel");
        auto recordingVmGlobal = env->NewGlobalRef(recordingVMClass);
        jmethodID togglePlay = env->GetStaticMethodID(static_cast<jclass>(recordingVmGlobal), "setStopPlay", "()V");

        recording_method_ids kotlinMethodIds {
            .recordingScreenVM = static_cast<jclass>(env->NewGlobalRef(recordingVmGlobal)),
            .recordingScreenVMTogglePlay = togglePlay
        };
        kotlinRecordingMethodIdsPtr = make_shared<recording_method_ids>(kotlinMethodIds);
    }

    void delete_kotlin_recording_global_refs(JNIEnv *env) {
        if (kotlinRecordingMethodIdsPtr && kotlinRecordingMethodIdsPtr->recordingScreenVM) {
            env->DeleteGlobalRef(kotlinRecordingMethodIdsPtr->recordingScreenVM);
            kotlinRecordingMethodIdsPtr.reset();
            kotlinRecordingMethodIdsPtr = nullptr;
        }
    }

    JNIEXPORT jboolean JNICALL
    Java_com_bluehub_fastmixer_audio_RecordingEngine_create(JNIEnv *env, jclass, jstring recordingFileDir, jboolean  recordingScreenViewModelPassed) {
        if (!recordingEngine) {
            auto recordingFileDirStr = java_str_to_c_str(env, recordingFileDir);

            prepare_kotlin_recording_method_ids(env);

            recordingEngine = new RecordingEngine(recordingFileDirStr, recordingScreenViewModelPassed);
        }
        return (recordingEngine != nullptr);
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_audio_RecordingEngine_delete(JNIEnv *env, jclass) {
        if (!recordingEngine) {
            return;
        }
        delete_kotlin_recording_global_refs(env);
        delete recordingEngine;
        recordingEngine = nullptr;
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_audio_RecordingEngine_setupAudioSource(JNIEnv *env, jclass, jint fd) {
        if (!recordingEngine) {
            LOGE("setupAudioSource: recordingEngine is null, you must call create() method before calling this method");
        }
        recordingEngine->setupAudioSource(fd);
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_audio_RecordingEngine_startRecording(JNIEnv *env, jclass) {
        if (!recordingEngine) {
            LOGE("startRecording: recordingEngine is null, you must call create() method before calling this method");
        }
        recordingEngine->startRecording();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_audio_RecordingEngine_stopRecording(JNIEnv *env, jclass) {
        if (!recordingEngine) {
            LOGE("stopRecording: recordingEngine is null, you must call create() method before calling this method");
            return;
        }
        recordingEngine->stopRecording();
    }

    JNIEXPORT jboolean JNICALL
    Java_com_bluehub_fastmixer_audio_RecordingEngine_startMixingTracksPlayback(JNIEnv * env, jclass) {
        if (!recordingEngine) {
            LOGE("startMixingTracksPlayback: recordingEngine is null, you must call create() method before calling this method");
            return false;
        }
        return recordingEngine->startMixingTracksPlayback();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_audio_RecordingEngine_stopMixingTracksPlayback(JNIEnv *env, jclass) {
        if (!recordingEngine) {
            LOGE("stopMixingTracksPlayback: recordingEngine is null, you must call create() method before calling this method");
            return;
        }
        recordingEngine->stopMixingTracksPlayback();
    }

    JNIEXPORT jboolean JNICALL
    Java_com_bluehub_fastmixer_audio_RecordingEngine_startPlayback(JNIEnv * env, jclass) {
        if (!recordingEngine) {
            LOGE("startPlayback: recordingEngine is null, you must call create() method before calling this method");
            return false;
        }
        return recordingEngine->startPlayback();
    }

    JNIEXPORT jboolean JNICALL
    Java_com_bluehub_fastmixer_audio_RecordingEngine_startPlaybackWithMixingTracks(JNIEnv * env, jclass) {
        if (!recordingEngine) {
            LOGE("startPlaybackWithMixingTracks: recordingEngine is null, you must call create() method before calling this method");
            return false;
        }
        return recordingEngine->startPlaybackWithMixingTracks();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_audio_RecordingEngine_startPlayingWithMixingTracksWithoutSetup(JNIEnv * env, jclass) {
        if (!recordingEngine) {
            LOGE("startPlayingWithMixingTracksWithoutSetup: recordingEngine is null, you must call create() method before calling this method");
            return;
        }
        recordingEngine->startPlayingWithMixingTracksWithoutSetup();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_audio_RecordingEngine_stopPlayback(JNIEnv *env, jclass) {
        if (!recordingEngine) {
            LOGE("stopPlayback: recordingEngine is null, you must call create() method before calling this method");
            return;
        }
        recordingEngine->stopAndResetPlayback();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_audio_RecordingEngine_pausePlayback(JNIEnv *env, jclass) {
        if (!recordingEngine) {
            LOGE("pausePlayback: recordingEngine is null, you must call create() method before calling this method");
            return;
        }
        recordingEngine->pausePlayback();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_audio_RecordingEngine_startLivePlayback(JNIEnv *env, jclass) {
        if (!recordingEngine) {
            LOGE("startLivePlayback: recordingEngine is null, you must call create() method before calling this method");
            return;
        }
        recordingEngine->startLivePlayback();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_audio_RecordingEngine_stopLivePlayback(JNIEnv *env, jclass) {
        if (!recordingEngine) {
            LOGE("stopLivePlayback: recordingEngine is null, you must call create() method before calling this method");
            return;
        }
        recordingEngine->stopLivePlayback();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_audio_RecordingEngine_flushWriteBuffer(JNIEnv *env, jclass) {
        if (!recordingEngine) {
            LOGE("flushWriteBuffer: recordingEngine is null, you must call create() method before calling this method");
            return;
        }
        recordingEngine->flushWriteBuffer();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_audio_RecordingEngine_restartPlayback(JNIEnv *env, jclass) {
        if (!recordingEngine) {
            LOGE("restartPlayback: recordingEngine is null, you must call create() method before calling this method");
            return;
        }
        recordingEngine->restartPlayback();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_audio_RecordingEngine_restartPlaybackWithMixingTracks(JNIEnv *env, jclass) {
        if (!recordingEngine) {
            LOGE("restartPlaybackWithMixingTracks: recordingEngine is null, you must call create() method before calling this method");
            return;
        }
        recordingEngine->restartPlaybackWithMixingTracks();
    }

    JNIEXPORT jint JNICALL
    Java_com_bluehub_fastmixer_audio_RecordingEngine_getCurrentMax(JNIEnv * env, jclass) {
        if (!recordingEngine) {
            LOGE("getCurrentMax: recordingEngine is null, you must call create() method before calling this method");
            return 0;
        }
        int currentMax = recordingEngine->getCurrentMax();
        return currentMax;
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_audio_RecordingEngine_resetCurrentMax(JNIEnv *env, jclass) {
        if (!recordingEngine) {
            LOGE("resetCurrentMax: recordingEngine is null, you must call create() method before calling this method");
            return;
        }
        recordingEngine->resetCurrentMax();
    }

    JNIEXPORT jint JNICALL
    Java_com_bluehub_fastmixer_audio_RecordingEngine_getTotalSampleFrames(JNIEnv * env, jclass) {
        if (!recordingEngine) {
            LOGE("getTotalRecordedFrames: recordingEngine is null, you must call create() method before calling this method");
            return 0;
        }
        return recordingEngine->getTotalSampleFrames();
    }

    JNIEXPORT jint JNICALL
    Java_com_bluehub_fastmixer_audio_RecordingEngine_getCurrentPlaybackProgress(JNIEnv * env, jclass) {
        if (!recordingEngine) {
            LOGE("getCurrentPlaybackProgress: recordingEngine is null, you must call create() method before calling this method");
            return 0;
        }
        return recordingEngine->getCurrentPlaybackProgress();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_audio_RecordingEngine_setPlayHead(JNIEnv *env, jclass, jint position) {
        if (!recordingEngine) {
            LOGE("setPlayHead: recordingEngine is null, you must call create() method before calling this method");
            return;
        }
        recordingEngine->setPlayHead(position);
    }

    JNIEXPORT jint JNICALL
    Java_com_bluehub_fastmixer_audio_RecordingEngine_getDurationInSeconds(JNIEnv * env, jclass) {
        if (!recordingEngine) {
            LOGE("getDurationInSeconds: recordingEngine is null, you must call create() method before calling this method");
            return 0;
        }
        return recordingEngine->getDurationInSeconds();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_audio_RecordingEngine_addSources(JNIEnv *env, jclass, jobjectArray filePaths) {
        if (!recordingEngine) {
            LOGE("addSources: recordingEngine is null, you must call create() method before calling this method");
            return;
        }
        jint numElements = env->GetArrayLength(filePaths);
        string strArr[numElements];

        for (int i = 0; i < numElements; i++) {
            jstring elem = (jstring) env->GetObjectArrayElement(filePaths, i);
            strArr[i] = java_str_to_c_str(env, elem);
        }

        string* strPtr = strArr;
        recordingEngine->addSourcesToPlayer(move(strPtr), numElements);
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_audio_RecordingEngine_resetRecordingEngine(JNIEnv *env, jclass) {
        if (!recordingEngine) {
            LOGE("resetAudioEngine: recordingEngine is null, you must call create() method before calling this method");
            return;
        }
        return recordingEngine->resetAudioEngine();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_audio_RecordingEngine_closeFd(JNIEnv *env, jclass, jint fd) {
        close(fd);
    }
}
