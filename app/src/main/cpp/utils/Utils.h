
#ifndef FAST_MIXER_UTILS_H
#define FAST_MIXER_UTILS_H

#ifndef MODULE_NAME
#define MODULE_NAME  "RecordingIO"
#endif

#include "cstdint"
#include "cmath"
#include "sys/stat.h"
#include "cstring"
#include "string"
#include "sstream"
#include "ios"
#include "../logging_macros.h"

using namespace std;

template <typename K>
void fillArrayWithZeros(K *data, int32_t length) {
    size_t bufferSize = length * sizeof(K);
    memset(data, 0, bufferSize);
}

inline bool strEndedWith(string const &fullString, string const &ending) {
    if (fullString.length() >= ending.length()) {
        return (0 == fullString.compare (fullString.length() - ending.length(), ending.length(), ending));
    } else {
        return false;
    }
}

inline long getSizeOfFile(const char *fileName) {
    struct stat st;
    if(stat(fileName,&st)==0)
        return (static_cast<long>(st.st_size));
    else
        return -1;
}

inline string java_str_to_c_str(JNIEnv * env, jstring jStr) {
    const auto stringClass = env->FindClass("java/lang/String");
    const auto getBytes = env->GetMethodID(stringClass, "getBytes", "()[B");

    const auto stringJbytes = (jbyteArray) env->CallObjectMethod(jStr, getBytes);

    const auto length = env->GetArrayLength(stringJbytes);
    const auto pBytes = env->GetByteArrayElements(stringJbytes, nullptr);
    std::string str((char *)pBytes, length);
    env->ReleaseByteArrayElements(stringJbytes, pBytes, JNI_ABORT);

    return forward<string>(str);
}

#endif //FAST_MIXER_UTILS_H
