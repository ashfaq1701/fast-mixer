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

    JNIEXPORT jobjectArray JNICALL
    Java_com_bluehub_fastmixer_screens_mixing_MixingEngine_readSamples(JNIEnv *env, jclass, jstring fileName) {
        if (mixingEngine == nullptr) {
            LOGE("readSamples: mixingEngine is null, you must call create() method before calling this method");
            return nullptr;
        }

        char* fileNameStr = const_cast<char *>(env->GetStringUTFChars(fileName, NULL));

        jclass floatCls = env->FindClass("java/lang/Float");
        jmethodID floatConstructor = env->GetMethodID(floatCls, "<init>", "(F)V");

        jobjectArray result;
        buffer_data* data = mixingEngine->readSamples(fileNameStr).release();

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

    JNIEXPORT jint JNICALL
    Java_com_bluehub_fastmixer_screens_mixing_MixingEngine_getTotalSamples(JNIEnv *env, jclass, jstring filePath) {
        if (mixingEngine == nullptr) {
            LOGE("deleteFile: mixingEngine is null, you must call create() method before calling this method");
            return 0;
        }
        char* filePathStr = const_cast<char *>(env->GetStringUTFChars(filePath, NULL));
        return mixingEngine->getAudioFileTotalSamples(filePathStr);
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

