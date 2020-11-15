//
// Created by asalehin on 9/2/20.
//

#ifndef FAST_MIXER_JVM_ENV_H
#define FAST_MIXER_JVM_ENV_H

#include <jni.h>
#include "../logging_macros.h"
#include "../Constants.h"

using namespace std;

inline JavaVM *java_mixing_machine;
inline shared_ptr<method_ids> kotlinMethodIdsPtr {nullptr};

inline int get_mixing_env(JNIEnv **g_env) {
    int getEnvStat = java_mixing_machine->GetEnv((void **) g_env, JNI_VERSION_1_6);
    if (getEnvStat == JNI_EDETACHED) {
        if (java_mixing_machine->AttachCurrentThread(g_env, nullptr) != 0) {
            LOGE("FAILED ATTACH THREAD");
            return 2; //Failed to attach
        }
        return 1; //Attached. Need detach
    }
    return 0;//Already attached
}

class jvm_mixing_env {
public:
    jvm_mixing_env() {
        auto resCode = get_mixing_env(&mEnv);
        if (resCode == 2)
            throw runtime_error("Cannot retrieve JNI environment");
        needDetach = (resCode == 1);
    }

    ~jvm_mixing_env() {
        if (needDetach && java_mixing_machine) {
            java_mixing_machine->DetachCurrentThread();
        }
        java_mixing_machine = nullptr;
    }

    JNIEnv *env() const noexcept {
        return mEnv;
    }

private:
    JNIEnv *mEnv;
    bool needDetach;
};

template<typename Callable>
auto call_in_attached_thread(Callable func) {
    jvm_mixing_env env;
    return func(env.env());
}

#endif //FAST_MIXER_JVM_ENV_H
