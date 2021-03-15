//
// Created by Ashfaq Salehin on 22/1/2021 AD.
//

#ifndef FAST_MIXER_SOURCEMAPSTORE_H
#define FAST_MIXER_SOURCEMAPSTORE_H

#include "audio/FileDataSource.h"
#include "map"

class SourceMapStore {

public:

    static shared_ptr<SourceMapStore> mInstance;
    static shared_ptr<SourceMapStore> getInstance();

    map<string, shared_ptr<FileDataSource>> sourceMap;

    static void reset();

    ~SourceMapStore();

private:

    SourceMapStore();
};


#endif //FAST_MIXER_SOURCEMAPSTORE_H
