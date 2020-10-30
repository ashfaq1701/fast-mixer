//
// Created by asalehin on 9/9/20.
//

#include "MixingIO.h"

FileDataSource* MixingIO::readFile(string filename) {
    AudioProperties targetProperties{
            .channelCount = StreamConstants::mChannelCount,
            .sampleRate = StreamConstants::mSampleRate
    };

    return FileDataSource::newFromCompressedFile(filename.c_str(), targetProperties);
}

int64_t MixingIO::getTotalSamples(string filename) {
    AudioProperties targetProperties{
            .channelCount = StreamConstants::mChannelCount,
            .sampleRate = StreamConstants::mSampleRate
    };

    return FileDataSource::getTotalSamples(filename.c_str(), targetProperties);
}
