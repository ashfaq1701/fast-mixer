//
// Created by asalehin on 8/29/20.
//

#ifndef FAST_MIXER_LOWPASSFILTER_H
#define FAST_MIXER_LOWPASSFILTER_H


#include "BaseSynthesizer.h"

class LowPassFilter: BaseSynthesizer {
public:
    void synthesize(int16_t data, int numSamples);
};


#endif //FAST_MIXER_LOWPASSFILTER_H
