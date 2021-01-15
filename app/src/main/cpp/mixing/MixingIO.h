//
// Created by asalehin on 9/9/20.
//

#ifndef FAST_MIXER_MIXINGIO_H
#define FAST_MIXER_MIXINGIO_H


#include <memory>
#include <FileDataSource.h>
#include "streams/StreamConstants.h"
#include "../audio/Player.h"

using namespace std;

class MixingIO {
public:
    MixingIO();
    shared_ptr<FileDataSource> readFile(string filename);
    void read_playback(float *targetData, int32_t numSamples);

    void setPlaying(bool setPlaying);

    void clearPlayerSources();

    void addSource(string key, shared_ptr<DataSource> source);

    void setStopPlaybackCallback(function<void(void)> stopPlaybackCallback);

    int getCurrentPlaybackProgress();

private:
    shared_ptr<Player> mPlayer {nullptr};
};


#endif //FAST_MIXER_MIXINGIO_H
