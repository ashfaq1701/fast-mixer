//
// Created by asalehin on 9/9/20.
//

#ifndef FAST_MIXER_MIXINGENGINE_H
#define FAST_MIXER_MIXINGENGINE_H


#include "MixingIO.h"
#include "vector"

using namespace std;

class MixingEngine {
public:
    void addFile(string filePath);
    unique_ptr<buffer_data> readSamples(int index, size_t numSamples);
    void deleteFile(int idx);
private:
    MixingIO mixingIO;
    vector<shared_ptr<FileDataSource>> sourceList;
};


#endif //FAST_MIXER_MIXINGENGINE_H
