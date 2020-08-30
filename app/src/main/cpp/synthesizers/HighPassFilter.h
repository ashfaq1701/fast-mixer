//
// Created by asalehin on 8/30/20.
//

#ifndef FAST_MIXER_HIGHPASSFILTER_H
#define FAST_MIXER_HIGHPASSFILTER_H


#include "BaseSynthesizer.h"

class HighPassFilter: BaseSynthesizer {
public:
    void synthesize(int16_t data, int numSamples);
};


#endif //FAST_MIXER_HIGHPASSFILTER_H
