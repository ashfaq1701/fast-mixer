
#ifndef FAST_MIXER_UTILS_H
#define FAST_MIXER_UTILS_H

#ifndef MODULE_NAME
#define MODULE_NAME  "Utils"
#endif

#include <cstdint>
#include <cmath>
#include <cstring>
#include <string>
#include <sstream>
#include <ios>
#include "logging_macros.h"

template <typename K>
void fillArrayWithZeros(K *data, int32_t length) {

    size_t bufferSize = length * sizeof(K);
    memset(data, 0, bufferSize);
}

#endif //FAST_MIXER_UTILS_H
