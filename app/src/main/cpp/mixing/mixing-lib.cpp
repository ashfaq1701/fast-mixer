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
#include <fcntl.h>

static unique_ptr<MixingEngine> mixingEngine {nullptr};

extern "C" {
    extern "C" JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
        java_machine = vm;
        return  JNI_VERSION_1_6;
    }

    void prepare_kotlin_mixing_method_ids(JNIEnv *env) {
        jclass recordingVMClass = env->FindClass("com/bluehub/mixi/screens/mixing/MixingScreenViewModel");
        auto recordingVmGlobal = env->NewGlobalRef(recordingVMClass);
        jmethodID setStopPlayback = env->GetStaticMethodID(static_cast<jclass>(recordingVmGlobal), "setStopPlay", "()V");

        mixing_method_ids kotlinMethodIds {
            .mixingScreenVM = static_cast<jclass>(env->NewGlobalRef(recordingVmGlobal)),
            .mixingScreenVMSetStopPlayback = setStopPlayback
        };
        kotlinMixingMethodIdsPtr = make_shared<mixing_method_ids>(kotlinMethodIds);
    }

    void delete_kotlin_mixing_global_refs(JNIEnv *env) {
        if (kotlinMixingMethodIdsPtr && kotlinMixingMethodIdsPtr->mixingScreenVM) {
            env->DeleteGlobalRef(kotlinMixingMethodIdsPtr->mixingScreenVM);
            kotlinMixingMethodIdsPtr.reset();
            kotlinMixingMethodIdsPtr = nullptr;
        }
    }

    JNIEXPORT jboolean JNICALL
    Java_com_bluehub_mixi_audio_MixingEngine_create(JNIEnv *env, jclass) {
        if (!mixingEngine) {
            prepare_kotlin_mixing_method_ids(env);

            mixingEngine = unique_ptr<MixingEngine> { new MixingEngine() };
        }
        return (mixingEngine != nullptr);
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_mixi_audio_MixingEngine_delete(JNIEnv *env, jclass) {
        if (!mixingEngine) {
            LOGE("delete: mixingEngine is null, you must call create() method before calling this method");
            return;
        }
        delete_kotlin_mixing_global_refs(env);

        mixingEngine.reset();
    }

    JNIEXPORT jboolean JNICALL
    Java_com_bluehub_mixi_audio_MixingEngine_addFile(JNIEnv *env, jclass, jstring fileId, jint fd) {
        if (!mixingEngine) {
            LOGE("addFile: mixingEngine is null, you must call create() method before calling this method");
            return false;
        }
        auto filePathStr = java_str_to_c_str(env, fileId);
        return mixingEngine->addFile(move(filePathStr), fd);
    }

    JNIEXPORT jobjectArray JNICALL
    Java_com_bluehub_mixi_audio_MixingEngine_readSamples(JNIEnv *env, jclass, jstring filePath, jint countPoints) {
        if (!mixingEngine) {
            LOGE("readSamples: mixingEngine is null, you must call create() method before calling this method");
            return nullptr;
        }

        auto filePathStr = java_str_to_c_str(env, filePath);

        jclass floatCls = env->FindClass("java/lang/Float");
        jmethodID floatConstructor = env->GetMethodID(floatCls, "<init>", "(F)V");

        jobjectArray result;
        auto data = mixingEngine->readSamples(filePathStr, countPoints);

        float* dataSamples = data->ptr;

        jobject initValue = env->NewObject(floatCls, floatConstructor, (jfloat) 0);
        result = env->NewObjectArray(data->countPoints, floatCls, initValue);
        for (int i = 0; i < data->countPoints; i++) {
            jobject sample = env->NewObject(floatCls, floatConstructor, (jfloat) dataSamples[i]);

            env->SetObjectArrayElement(result, i, sample);
        }

        env->DeleteLocalRef(floatCls);
        data.reset();

        return result;
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_mixi_audio_MixingEngine_deleteFile(JNIEnv *env, jclass, jstring filePath) {
        if (!mixingEngine) {
            LOGE("deleteFile: mixingEngine is null, you must call create() method before calling this method");
            return;
        }
        auto filePathStr = java_str_to_c_str(env, filePath);
        mixingEngine->deleteFile(move(filePathStr));
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_mixi_audio_MixingEngine_addSources(JNIEnv *env, jclass, jobjectArray filePaths) {
        if (!mixingEngine) {
            LOGE("addSources: mixingEngine is null, you must call create() method before calling this method");
            return;
        }
        jint numElements = env->GetArrayLength(filePaths);
        string strArr[numElements];

        for (int i = 0; i < numElements; i++) {
            jstring elem = (jstring) env->GetObjectArrayElement(filePaths, i);
            strArr[i] = java_str_to_c_str(env, elem);
        }

        string* strPtr = strArr;
        mixingEngine->addSourcesToPlayer(move(strPtr), numElements);
    }

    JNIEXPORT jint JNICALL
    Java_com_bluehub_mixi_audio_MixingEngine_getTotalSamples(JNIEnv *env, jclass, jstring filePath) {
        if (!mixingEngine) {
            LOGE("deleteFile: mixingEngine is null, you must call create() method before calling this method");
            return 0;
        }
        auto filePathStr = java_str_to_c_str(env, filePath);
        return mixingEngine->getAudioFileTotalSamples(move(filePathStr));
    }

    JNIEXPORT jint JNICALL
    Java_com_bluehub_mixi_audio_MixingEngine_getTotalSampleFrames(JNIEnv * env, jclass) {
        if (!mixingEngine) {
            LOGE("getTotalRecordedFrames: mixingEngine is null, you must call create() method before calling this method");
            return 0;
        }
        return mixingEngine->getTotalSampleFrames();
    }

    JNIEXPORT jint JNICALL
    Java_com_bluehub_mixi_audio_MixingEngine_getCurrentPlaybackProgress(JNIEnv *env, jclass) {
        if (!mixingEngine) {
            LOGE("getCurrentPlaybackProgress: mixingEngine is null, you must call create() method before calling this method");
            return 0;
        }
        return mixingEngine->getCurrentPlaybackProgress();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_mixi_audio_MixingEngine_clearPlayerSources(JNIEnv *env, jclass) {
        if (!mixingEngine) {
            LOGE("clearPlayerSources: mixingEngine is null, you must call create() method before calling this method");
            return;
        }
        mixingEngine->clearPlayerSources();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_mixi_audio_MixingEngine_setPlayerHead(JNIEnv *env, jclass, jint position) {
        if (!mixingEngine) {
            LOGE("setPlayerHead: mixingEngine is null, you must call create() method before calling this method");
            return;
        }
        mixingEngine->setPlayerHead(position);
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_mixi_audio_MixingEngine_setSourcePlayHead(JNIEnv *env, jclass, jstring filePath, jint position) {
        if (!mixingEngine) {
            LOGE("setSourcePlayHead: mixingEngine is null, you must call create() method before calling this method");
            return;
        }

        auto filePathStr = java_str_to_c_str(env, filePath);
        mixingEngine->setSourcePlayHead(move(filePathStr), position);
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_mixi_audio_MixingEngine_startPlayback(JNIEnv *env, jclass) {
        if (!mixingEngine) {
            LOGE("startPlayback: mixingEngine is null, you must call create() method before calling this method");
            return;
        }
        mixingEngine->startPlayback();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_mixi_audio_MixingEngine_pausePlayback(JNIEnv *env, jclass) {
        if (!mixingEngine) {
            LOGE("pausePlayback: mixingEngine is null, you must call create() method before calling this method");
            return;
        }
        mixingEngine->pausePlayback();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_mixi_audio_MixingEngine_gainSourceByDb(JNIEnv *env, jclass, jstring filePath, jfloat db) {
        if (!mixingEngine) {
            LOGE("setSourcePlayHead: mixingEngine is null, you must call create() method before calling this method");
            return;
        }

        auto filePathStr = java_str_to_c_str(env, filePath);
        mixingEngine->gainSourceByDb(move(filePathStr), db);
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_mixi_audio_MixingEngine_applySourceTransformation(JNIEnv *env, jclass, jstring filePath) {
        if (!mixingEngine) {
            LOGE("applySourceTransformation: mixingEngine is null, you must call create() method before calling this method");
            return;
        }

        auto filePathStr = java_str_to_c_str(env, filePath);
        mixingEngine->applySourceTransformation(move(filePathStr));
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_mixi_audio_MixingEngine_clearSourceTransformation(JNIEnv *env, jclass, jstring filePath) {
        if (!mixingEngine) {
            LOGE("clearSourceTransformation: mixingEngine is null, you must call create() method before calling this method");
            return;
        }

        auto filePathStr = java_str_to_c_str(env, filePath);
        mixingEngine->clearSourceTransformation(move(filePathStr));
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_mixi_audio_MixingEngine_setSourceBounds(JNIEnv *env, jclass, jstring filePath, jint start, jint end) {
        if (!mixingEngine) {
            LOGE("setSourceBounds: mixingEngine is null, you must call create() method before calling this method");
            return;
        }

        auto filePathStr = java_str_to_c_str(env, filePath);
        mixingEngine->setSourceBounds(filePathStr, start, end);
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_mixi_audio_MixingEngine_resetSourceBounds(JNIEnv *env, jclass, jstring filePath) {
        if (!mixingEngine) {
            LOGE("resetSourceBounds: mixingEngine is null, you must call create() method before calling this method");
            return;
        }

        auto filePathStr = java_str_to_c_str(env, filePath);
        mixingEngine->resetSourceBounds(filePathStr);
    }

    JNIEXPORT jint JNICALL
    Java_com_bluehub_mixi_audio_MixingEngine_shiftBySamples(JNIEnv *env, jclass, jstring filePath, jint position, jint numSamples) {
        if (!mixingEngine) {
            LOGE("shiftBySamples: mixingEngine is null, you must call create() method before calling this method");
            return -1;
        }

        auto filePathStr = java_str_to_c_str(env, filePath);
        return mixingEngine->shiftBySamples(filePathStr, position, numSamples);
    }

    JNIEXPORT jboolean JNICALL
    Java_com_bluehub_mixi_audio_MixingEngine_copyToClipboard(JNIEnv *env, jclass, jstring filePath, jint startPosition, jint endPosition) {
        if (!mixingEngine) {
            LOGE("copyToClipboard: mixingEngine is null, you must call create() method before calling this method");
            return false;
        }

        auto filePathStr = java_str_to_c_str(env, filePath);
        return mixingEngine->copyToClipboard(filePathStr, startPosition, endPosition);
    }

    JNIEXPORT jint JNICALL
    Java_com_bluehub_mixi_audio_MixingEngine_cutToClipboard(JNIEnv *env, jclass, jstring filePath, jint startPosition, jint endPosition) {
        if (!mixingEngine) {
            LOGE("cutToClipboard: mixingEngine is null, you must call create() method before calling this method");
            return -1;
        }

        auto filePathStr = java_str_to_c_str(env, filePath);
        return mixingEngine->cutToClipboard(filePathStr, startPosition, endPosition);
    }

    JNIEXPORT jboolean  JNICALL
    Java_com_bluehub_mixi_audio_MixingEngine_muteAndCopyToClipboard(JNIEnv *env, jclass, jstring filePath, jint startPosition, jint endPosition) {
        if (!mixingEngine) {
            LOGE("muteAndCopyToClipboard: mixingEngine is null, you must call create() method before calling this method");
            return false;
        }

        auto filePathStr = java_str_to_c_str(env, filePath);
        return mixingEngine->muteAndCopyToClipboard(filePathStr, startPosition, endPosition);
    }

    JNIEXPORT void  JNICALL
    Java_com_bluehub_mixi_audio_MixingEngine_pasteFromClipboard(JNIEnv *env, jclass, jstring filePath, jint position) {
        if (!mixingEngine) {
            LOGE("pasteFromClipboard: mixingEngine is null, you must call create() method before calling this method");
            return;
        }

        auto filePathStr = java_str_to_c_str(env, filePath);
        mixingEngine->pasteFromClipboard(filePathStr, position);
    }

    JNIEXPORT void  JNICALL
    Java_com_bluehub_mixi_audio_MixingEngine_pasteNewFromClipboard(JNIEnv *env, jclass, jstring filePath) {
        if (!mixingEngine) {
            LOGE("pasteNewFromClipboard: mixingEngine is null, you must call create() method before calling this method");
            return;
        }

        auto filePathStr = java_str_to_c_str(env, filePath);
        mixingEngine->pasteNewFromClipboard(filePathStr);
    }

    JNIEXPORT void  JNICALL
    Java_com_bluehub_mixi_audio_MixingEngine_setPlayerBoundStart(JNIEnv *env, jclass, jint playerBoundStart) {
        if (!mixingEngine) {
            LOGE("setPlayerBoundStart: mixingEngine is null, you must call create() method before calling this method");
            return;
        }

        mixingEngine->setPlayerBoundStart(playerBoundStart);
    }

    JNIEXPORT void  JNICALL
    Java_com_bluehub_mixi_audio_MixingEngine_setPlayerBoundEnd(JNIEnv *env, jclass, jint playerBoundEnd) {
        if (!mixingEngine) {
            LOGE("setPlayerBoundEnd: mixingEngine is null, you must call create() method before calling this method");
            return;
        }

        mixingEngine->setPlayerBoundEnd(playerBoundEnd);
    }

    JNIEXPORT void  JNICALL
    Java_com_bluehub_mixi_audio_MixingEngine_resetPlayerBoundStart(JNIEnv *env, jclass) {
        if (!mixingEngine) {
            LOGE("resetPlayerBoundStart: mixingEngine is null, you must call create() method before calling this method");
            return;
        }

        mixingEngine->resetPlayerBoundStart();
    }

    JNIEXPORT jboolean JNICALL
    Java_com_bluehub_mixi_audio_MixingEngine_writeToFile(JNIEnv *env, jclass, jobjectArray filePaths, jint fd) {
        if (!mixingEngine) {
            LOGE("writeToFile: mixingEngine is null, you must call create() method before calling this method");
            return false;
        }

        jint numElements = env->GetArrayLength(filePaths);
        string strArr[numElements];

        for (int i = 0; i < numElements; i++) {
            jstring elem = (jstring) env->GetObjectArrayElement(filePaths, i);
            strArr[i] = java_str_to_c_str(env, elem);
        }

        string* strPtr = strArr;

        return mixingEngine->writeToFile(strPtr, numElements, fd);
    }

    JNIEXPORT void  JNICALL
    Java_com_bluehub_mixi_audio_MixingEngine_resetPlayerBoundEnd(JNIEnv *env, jclass) {
        if (!mixingEngine) {
            LOGE("resetPlayerBoundEnd: mixingEngine is null, you must call create() method before calling this method");
            return;
        }

        mixingEngine->resetPlayerBoundEnd();
    }
}
