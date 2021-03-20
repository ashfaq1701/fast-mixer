//
// Created by asalehin on 9/9/20.
//

#include "MixingIO.h"
#include "../utils/Utils.h"

MixingIO::MixingIO() {
    Player* player = new Player();
    mPlayer.reset(move(player));
}

shared_ptr<FileDataSource> MixingIO::readFile(string filename, int fd) {
    AudioProperties targetProperties{
            .channelCount = MixingStreamConstants::mChannelCount,
            .sampleRate = MixingStreamConstants::mSampleRate
    };

    return shared_ptr<FileDataSource> {
        FileDataSource::newFromCompressedFile(filename.c_str(), fd, targetProperties),
        [](FileDataSource *source) {
            delete source;
        }
    };
}

shared_ptr<BufferedDataSource> MixingIO::createClipboardDataSource(vector<float>& clipboard) {
    AudioProperties targetProperties{
            .channelCount = MixingStreamConstants::mChannelCount,
            .sampleRate = MixingStreamConstants::mSampleRate
    };

    return shared_ptr<BufferedDataSource> {
        BufferedDataSource::newFromClipboard(clipboard, targetProperties),
        [](BufferedDataSource *source) {
            delete source;
        }
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

bool MixingIO::writeSourcesToFile(map<string, shared_ptr<DataSource>> playMap, int fd) {
    MixedAudioWriter mixedAudioWriter(playMap);
    return mixedAudioWriter.writeToFile(fd);
}

void MixingIO::syncPlayHeads() {
    mPlayer->syncPlayHeads();
}

void MixingIO::read_playback(float *targetData, int32_t numSamples) {
    mPlayer->renderAudio(targetData, numSamples);
}

void MixingIO::setStopPlaybackCallback(function<void()> stopPlaybackCallback) {
    mStopPlaybackCallback = stopPlaybackCallback;
    mPlayer->setPlaybackCallback(stopPlaybackCallback);
}

void MixingIO::runStopPlaybackCallback() {
    if (mStopPlaybackCallback) {
        mStopPlaybackCallback();
    }
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

void MixingIO::setPlayerBoundStart(int64_t boundStart) {
    mPlayer->setPlayerBoundStart(boundStart);
}

void MixingIO::setPlayerBoundEnd(int64_t boundEnd) {
    mPlayer->setPlayerBoundEnd(boundEnd);
}

void MixingIO::resetPlayerBoundStart() {
    mPlayer->resetPlayerBoundStart();
}

void MixingIO::resetPlayerBoundEnd() {
    mPlayer->resetPlayerBoundEnd();
}
