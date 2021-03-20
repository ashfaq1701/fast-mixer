//
// Created by Ashfaq Salehin on 11/1/2021 AD.
//

#include "MixingBaseStream.h"

MixingBaseStream::MixingBaseStream(shared_ptr<MixingIO> mixingIO) : BaseStream() {
    mMixingIO = mixingIO;
}
