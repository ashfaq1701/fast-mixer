//
// Created by asalehin on 9/9/20.
//

#ifndef FAST_MIXER_MIXINGENGINE_H
#define FAST_MIXER_MIXINGENGINE_H


#include "MixingIO.h"
#include "map"

using namespace std;

class MixingEngine {
public:
    unique_ptr<buffer_data> readAllSamples(string filePath);
    int64_t getAudioFileTotalSamples(string uuid);
private:
    MixingIO mixingIO;
};


#endif //FAST_MIXER_MIXINGENGINE_H
