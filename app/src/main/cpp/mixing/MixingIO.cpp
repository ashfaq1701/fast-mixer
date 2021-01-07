//
// Created by asalehin on 9/9/20.
//

#include "MixingIO.h"

shared_ptr<FileDataSource> MixingIO::readFile(string filename) {
    AudioProperties targetProperties{
            .channelCount = StreamConstants::mChannelCount,
            .sampleRate = StreamConstants::mSampleRate
    };

    return shared_ptr<FileDataSource> {
        FileDataSource::newFromCompressedFile(filename.c_str(), targetProperties)
    };
}
