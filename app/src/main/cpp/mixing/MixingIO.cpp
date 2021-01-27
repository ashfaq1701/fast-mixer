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
            .channelCount = MixingStreamConstants::mChannelCount,
            .sampleRate = MixingStreamConstants::mSampleRate
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
    syncPlayHeads();
}

void MixingIO::addSourceMap(map<string, shared_ptr<DataSource>> playMap) {
    mPlayer->addSourceMap(playMap);
    syncPlayHeads();
}

void MixingIO::syncPlayHeads() {
    mPlayer->syncPlayHeads();
}

void MixingIO::read_playback(float *targetData, int32_t numSamples) {
    mPlayer->renderAudio(targetData, numSamples);
}

void MixingIO::setStopPlaybackCallback(function<void()> stopPlaybackCallback) {
    mPlayer->setPlaybackCallback(stopPlaybackCallback);
}

int MixingIO::getTotalSampleFrames() {
    if (mPlayer) {
        return mPlayer->getTotalSampleFrames();
    }
    return 0;
}

int MixingIO::getCurrentPlaybackProgress() {
    return mPlayer->getPlayHead();
}

void MixingIO::setPlayHead(int position) {
    mPlayer->setPlayHead(position);
}
