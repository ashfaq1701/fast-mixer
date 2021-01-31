//
// Created by asalehin on 8/29/20.
//

#ifndef FAST_MIXER_BASESYNTHESIZER_H
#define FAST_MIXER_BASESYNTHESIZER_H

#include "oboe/Definitions.h"
#include "../audio/FileDataSource.h"
#include <memory>

using namespace std;

class BaseSynthesizer {

public:

    virtual void synthesize(shared_ptr<FileDataSource> source) = 0;
};


#endif //FAST_MIXER_BASESYNTHESIZER_H
