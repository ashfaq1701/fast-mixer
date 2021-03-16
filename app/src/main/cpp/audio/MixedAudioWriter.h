//
// Created by Ashfaq Salehin on 17/3/2021 AD.
//

#ifndef FAST_MIXER_MIXEDAUDIOWRITER_H
#define FAST_MIXER_MIXEDAUDIOWRITER_H

#include "SourceStore.h"
#include <string>
#include "sndfile.hh"

using namespace std;

class MixedAudioWriter : public SourceStore {

public:

    MixedAudioWriter(map<string, shared_ptr<DataSource>> sourceMap) : SourceStore(sourceMap) {}

public:

    bool writeToFile(int fd);
};


#endif //FAST_MIXER_MIXEDAUDIOWRITER_H
