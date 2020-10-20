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
    ~MixingEngine();
    void addFile(string filePath, string uuid);
    unique_ptr<buffer_data> readSamples(string uuid, size_t numSamples);
    void deleteFile(string uuid);
    int64_t getAudioFileTotalSamples(string uuid);
private:
    MixingIO mixingIO;
    map<string, shared_ptr<FileDataSource>> sourceMap;
};


#endif //FAST_MIXER_MIXINGENGINE_H
