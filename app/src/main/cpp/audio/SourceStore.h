//
// Created by Ashfaq Salehin on 16/3/2021 AD.
//

#ifndef FAST_MIXER_SOURCESTORE_H
#define FAST_MIXER_SOURCESTORE_H

#include "map"
#include "DataSource.h"

using namespace std;

class SourceStore {

public:

    SourceStore(map<string, shared_ptr<DataSource>> sourceMap)
        : mSourceMap { sourceMap } {};

    SourceStore() : SourceStore { map<string, shared_ptr<DataSource>>() } {};

    int64_t getMaxTotalSourceFrames();

    int64_t getTotalSampleFrames();

protected:

    map<string, shared_ptr<DataSource>> mSourceMap;

    float addedMaxSampleValue = FLT_MIN;

    void updateAddedMax();

    float getMaxValueAcrossSources();
};


#endif //FAST_MIXER_SOURCESTORE_H
