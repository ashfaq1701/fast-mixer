//
// Created by Ashfaq Salehin on 9/10/2020 AD.
//

#ifndef FAST_MIXER_STREAMCONSTANTS_H
#define FAST_MIXER_STREAMCONSTANTS_H

#include "oboe/Definitions.h"

class MixingStreamConstants {
public:
    static oboe::AudioApi mAudioApi;
    static oboe::AudioFormat mFormat;
    static int32_t mDeviceId;
    static int32_t mSampleRate;
    static int32_t mChannelCount;
};


#endif //FAST_MIXER_STREAMCONSTANTS_H
