//
// Created by asalehin on 9/9/20.
//

#ifndef FAST_MIXER_MIXINGIO_H
#define FAST_MIXER_MIXINGIO_H


#include <memory>
#include <FileDataSource.h>
#include "conf/StreamConstants.h"

using namespace std;

class MixingIO {
public:
    int64_t getTotalSamples(string filename);
    FileDataSource* readFile(string filename);
};


#endif //FAST_MIXER_MIXINGIO_H
