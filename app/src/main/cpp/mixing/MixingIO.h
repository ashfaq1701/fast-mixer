//
// Created by asalehin on 9/9/20.
//

#ifndef FAST_MIXER_MIXINGIO_H
#define FAST_MIXER_MIXINGIO_H


#include <memory>
#include <FileDataSource.h>

using namespace std;

class MixingIO {
public:
    static shared_ptr<FileDataSource> readFile(string filename);
};


#endif //FAST_MIXER_MIXINGIO_H
