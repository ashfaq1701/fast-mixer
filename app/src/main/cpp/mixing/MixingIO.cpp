//
// Created by asalehin on 9/9/20.
//

#include "MixingIO.h"

shared_ptr<FileDataSource> MixingIO::readFile(string filename) {
    AudioProperties targetProperties{
            .channelCount = StreamConstants::mChannelCount,
            .sampleRate = StreamConstants::mSampleRate
    };

    shared_ptr<FileDataSource> source {
        FileDataSource::newFromCompressedFile(filename.c_str(), targetProperties)
    };

    return move(source);
}
