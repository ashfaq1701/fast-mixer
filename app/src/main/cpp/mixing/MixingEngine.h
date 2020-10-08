//
// Created by asalehin on 9/9/20.
//

#ifndef FAST_MIXER_MIXINGENGINE_H
#define FAST_MIXER_MIXINGENGINE_H


#include "MixingIO.h"
#include "list"

using namespace std;

class MixingEngine {
public:
    void addFile(string filePath);
private:
    MixingIO mixingIO;
    list<shared_ptr<FileDataSource>> sourceList;
};


#endif //FAST_MIXER_MIXINGENGINE_H
