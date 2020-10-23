//
// Created by asalehin on 9/9/20.
//

#include "MixingIO.h"

unique_ptr<FileDataSource> MixingIO::readFile(string filename) {
    AudioProperties targetProperties{
            .channelCount = StreamConstants::mChannelCount,
            .sampleRate = StreamConstants::mSampleRate
    };

    FileDataSource* source = FileDataSource::newFromCompressedFile(filename.c_str(), targetProperties);

    return unique_ptr<FileDataSource> {
        source
    };
}
