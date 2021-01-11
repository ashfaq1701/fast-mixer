//
// Created by asalehin on 9/9/20.
//

#include "MixingIO.h"
#include "../utils/Utils.h"

MixingIO::MixingIO() {
    Player* player = new Player();
    mPlayer.reset(move(player));
}

shared_ptr<FileDataSource> MixingIO::readFile(string filename) {
    AudioProperties targetProperties{
            .channelCount = StreamConstants::mChannelCount,
            .sampleRate = StreamConstants::mSampleRate
    };

    return shared_ptr<FileDataSource> {
        FileDataSource::newFromCompressedFile(filename.c_str(), targetProperties)
    };
}

void MixingIO::read_playback(float *targetData, int32_t numSamples) {

}
