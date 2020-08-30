//
// Created by asalehin on 8/29/20.
//

#ifndef FAST_MIXER_BASESYNTHESIZER_H
#define FAST_MIXER_BASESYNTHESIZER_H

#include "oboe/Definitions.h"


class BaseSynthesizer {
public:
    virtual void synthesize(int16_t data, int numSamples) = 0;
};


#endif //FAST_MIXER_BASESYNTHESIZER_H
