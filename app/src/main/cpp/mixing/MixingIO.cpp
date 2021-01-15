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

void MixingIO::setPlaying(bool isPlaying) {
    mPlayer->setPlaying(isPlaying);
}

void MixingIO::clearPlayerSources() {
    mPlayer->resetPlayHead();
    mPlayer->clearSources();
}

void MixingIO::addSource(string key, shared_ptr<DataSource> source) {
    mPlayer->addSource(key, source);
    if (source->getPlayHead() > 0) {
        mPlayer->setPlayHead(source->getPlayHead());
    }
}

void MixingIO::read_playback(float *targetData, int32_t numSamples) {
    mPlayer->renderAudio(targetData, numSamples);
}

void MixingIO::setStopPlaybackCallback(function<void()> stopPlaybackCallback) {
    mPlayer->setPlaybackCallback(stopPlaybackCallback);
}

int MixingIO::getCurrentPlaybackProgress() {
    return mPlayer->getPlayHead();
}
