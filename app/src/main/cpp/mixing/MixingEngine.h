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
    void addFile(string filePath);
    unique_ptr<buffer_data> readSamples(string filePath, size_t countPoints);
    void deleteFile(string filePath);
    int64_t getAudioFileTotalSamples(string filePath);
private:
    MixingIO mixingIO;
    map<string, FileDataSource*> sourceMap;
};


#endif //FAST_MIXER_MIXINGENGINE_H
