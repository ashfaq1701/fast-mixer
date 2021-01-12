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
#include "mixing_jvm_env.h"

const char *TAG = "mixing-lib: %s";
static MixingEngine *mixingEngine = nullptr;

extern "C" {
    void prepare_kotlin_method_ids(JNIEnv *env) {
        jclass recordingVMClass = env->FindClass("com/bluehub/fastmixer/screens/mixing/MixingScreenViewModel");
        auto recordingVmGlobal = env->NewGlobalRef(recordingVMClass);
        jmethodID setStopPlayback = env->GetStaticMethodID(static_cast<jclass>(recordingVmGlobal), "setStopPlay", "()V");

        method_ids kotlinMethodIds {
            .mixingScreenVM = static_cast<jclass>(env->NewGlobalRef(recordingVmGlobal)),
            .mixingScreenVMSetStopPlayback = setStopPlayback
        };
        kotlinMethodIdsPtr = make_shared<method_ids>(kotlinMethodIds);
    }

    void delete_kotlin_global_refs(JNIEnv *env) {
        if (kotlinMethodIdsPtr && kotlinMethodIdsPtr->mixingScreenVM) {
            env->DeleteGlobalRef(kotlinMethodIdsPtr->mixingScreenVM);
            kotlinMethodIdsPtr.reset();
            kotlinMethodIdsPtr = nullptr;
        }
    }

    extern "C" JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
        java_mixing_machine = vm;
        return  JNI_VERSION_1_6;
    }

    JNIEXPORT jboolean JNICALL
    Java_com_bluehub_fastmixer_screens_mixing_MixingEngine_create(JNIEnv *env, jclass) {
        if (!mixingEngine) {
            prepare_kotlin_method_ids(env);
            mixingEngine = new MixingEngine();
        }
        return (mixingEngine != nullptr);
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_screens_mixing_MixingEngine_delete(JNIEnv *env, jclass) {
        if (!mixingEngine) {
            LOGE("delete: mixingEngine is null, you must call create() method before calling this method");
            return;
        }
        delete_kotlin_global_refs(env);
        mixingEngine = nullptr;
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_screens_mixing_MixingEngine_addFile(JNIEnv *env, jclass, jstring filePath) {
        if (!mixingEngine) {
            LOGE("addFile: mixingEngine is null, you must call create() method before calling this method");
            return;
        }
        auto filePathStr = java_str_to_c_str(env, filePath);
        mixingEngine->addFile(move(filePathStr));
    }

    JNIEXPORT jobjectArray JNICALL
    Java_com_bluehub_fastmixer_screens_mixing_MixingEngine_readSamples(JNIEnv *env, jclass, jstring filePath, jint countPoints) {
        if (!mixingEngine) {
            LOGE("readSamples: mixingEngine is null, you must call create() method before calling this method");
            return nullptr;
        }

        auto filePathStr = java_str_to_c_str(env, filePath);

        jclass floatCls = env->FindClass("java/lang/Float");
        jmethodID floatConstructor = env->GetMethodID(floatCls, "<init>", "(F)V");

        jobjectArray result;
        buffer_data* data = mixingEngine->readSamples(filePathStr, countPoints).release();

        float* dataSamples = data->ptr;

        jobject initValue = env->NewObject(floatCls, floatConstructor, (jfloat) 0);
        result = env->NewObjectArray(data->countPoints, floatCls, initValue);
        for (int i = 0; i < data->countPoints; i++) {
            jobject sample = env->NewObject(floatCls, floatConstructor, (jfloat) dataSamples[i]);

            env->SetObjectArrayElement(result, i, sample);
        }

        env->DeleteLocalRef(floatCls);
        delete(data);

        return result;
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_screens_mixing_MixingEngine_deleteFile(JNIEnv *env, jclass, jstring filePath) {
        if (!mixingEngine) {
            LOGE("deleteFile: mixingEngine is null, you must call create() method before calling this method");
            return;
        }
        auto filePathStr = java_str_to_c_str(env, filePath);
        mixingEngine->deleteFile(move(filePathStr));
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_screens_mixing_MixingEngine_addSources(JNIEnv *env, jclass, jobjectArray filePaths) {
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
    Java_com_bluehub_fastmixer_screens_mixing_MixingEngine_getTotalSamples(JNIEnv *env, jclass, jstring filePath) {
        if (!mixingEngine) {
            LOGE("deleteFile: mixingEngine is null, you must call create() method before calling this method");
            return 0;
        }
        auto filePathStr = java_str_to_c_str(env, filePath);
        return mixingEngine->getAudioFileTotalSamples(move(filePathStr));
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_screens_mixing_MixingEngine_clearPlayerSources(JNIEnv *env, jclass) {
        if (!mixingEngine) {
            LOGE("clearPlayerSources: mixingEngine is null, you must call create() method before calling this method");
            return;
        }
        mixingEngine->clearPlayerSources();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_screens_mixing_MixingEngine_startPlayback(JNIEnv *env, jclass) {
        if (!mixingEngine) {
            LOGE("startPlayback: mixingEngine is null, you must call create() method before calling this method");
            return;
        }
        mixingEngine->startPlayback();
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_screens_mixing_MixingEngine_pausePlayback(JNIEnv *env, jclass) {
        if (!mixingEngine) {
            LOGE("pausePlayback: mixingEngine is null, you must call create() method before calling this method");
            return;
        }
        mixingEngine->pausePlayback();
    }
}
