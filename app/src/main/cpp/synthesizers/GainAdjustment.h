//
// Created by Ashfaq Salehin on 30/1/2021 AD.
//

#ifndef FAST_MIXER_GAINADJUSTMENT_H
#define FAST_MIXER_GAINADJUSTMENT_H

#include "BaseSynthesizer.h"


using namespace std;

class GainAdjustment : public BaseSynthesizer {

public:
    GainAdjustment(float gainFactorLog);
    void synthesize(shared_ptr<FileDataSource> source);

private:
    float mGainFactorLog = 0.0;
};


#endif //FAST_MIXER_GAINADJUSTMENT_H
