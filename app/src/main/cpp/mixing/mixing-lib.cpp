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

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_screens_mixing_MixingEngine_addFile(JNIEnv *env, jclass, jstring filePathStr, jstring uuid) {
        if (mixingEngine == nullptr) {
            LOGE("addFile: mixingEngine is null, you must call create() method before calling this method");
            return;
        }
        char* filePath = const_cast<char *>(env->GetStringUTFChars(filePathStr, NULL));
        char* uuidStr = const_cast<char *>(env->GetStringUTFChars(uuid, NULL));
        mixingEngine->addFile(filePath, uuidStr);
    }

    JNIEXPORT jobjectArray JNICALL
    Java_com_bluehub_fastmixer_screens_mixing_MixingEngine_readSamples(JNIEnv *env, jclass, jstring uuid, jint numSamples) {
        if (mixingEngine == nullptr) {
            LOGE("readSamples: mixingEngine is null, you must call create() method before calling this method");
            return nullptr;
        }

        char* uuidStr = const_cast<char *>(env->GetStringUTFChars(uuid, NULL));

        jclass floatCls = env->FindClass("java/lang/Float");
        jmethodID floatConstructor = env->GetMethodID(floatCls, "<init>", "(F)V");

        jobjectArray result;
        buffer_data* data = mixingEngine->readSamples(uuidStr, numSamples).release();

        float* dataSamples = data->ptr;

        jobject initValue = env->NewObject(floatCls, floatConstructor, (jfloat) 0);
        result = env->NewObjectArray(data->numSamples, floatCls, initValue);
        for (int i = 0; i < data->numSamples; i++) {
            jobject sample = env->NewObject(floatCls, floatConstructor, (jfloat) dataSamples[i]);

            env->SetObjectArrayElement(result, i, sample);
        }

        env->DeleteLocalRef(floatCls);
        delete(data);

        return result;
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_screens_mixing_MixingEngine_deleteFile(JNIEnv *env, jclass, jstring uuid) {
        if (mixingEngine == nullptr) {
            LOGE("deleteFile: mixingEngine is null, you must call create() method before calling this method");
            return;
        }
        char* uuidStr = const_cast<char *>(env->GetStringUTFChars(uuid, NULL));
        mixingEngine->deleteFile(uuidStr);
    }

    JNIEXPORT jint JNICALL
    Java_com_bluehub_fastmixer_screens_mixing_MixingEngine_getTotalSamples(JNIEnv *env, jclass, jstring uuid) {
        if (mixingEngine == nullptr) {
            LOGE("deleteFile: mixingEngine is null, you must call create() method before calling this method");
            return 0;
        }
        char* uuidStr = const_cast<char *>(env->GetStringUTFChars(uuid, NULL));
        return mixingEngine->getAudioFileTotalSamples(uuidStr);
    }

    JNIEXPORT void JNICALL
    Java_com_bluehub_fastmixer_screens_mixing_MixingEngine_delete(JNIEnv *env, jclass) {
        if (mixingEngine != nullptr) {
            LOGE("delete: mixingEngine is null, you must call create() method before calling this method");
            return;
        }
        delete mixingEngine;
        mixingEngine = nullptr;
    }
}

