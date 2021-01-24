//
// Created by Ashfaq Salehin on 11/1/2021 AD.
//

#ifndef FAST_MIXER_MIXINGBASESTREAM_H
#define FAST_MIXER_MIXINGBASESTREAM_H

#include "../../streams/BaseStream.h"
#include "../MixingIO.h"
#include "MixingStreamConstants.h"

class MixingBaseStream : public BaseStream {
public:
    MixingBaseStream(MixingIO* mixingIO);
    MixingIO* mMixingIO;
};


#endif //FAST_MIXER_MIXINGBASESTREAM_H
