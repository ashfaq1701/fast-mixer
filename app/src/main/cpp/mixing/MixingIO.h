//
// Created by asalehin on 9/9/20.
//

#ifndef FAST_MIXER_MIXINGIO_H
#define FAST_MIXER_MIXINGIO_H


#include <memory>
#include <FileDataSource.h>
#include <BufferedDataSource.h>
#include "streams/MixingStreamConstants.h"
#include "../audio/Player.h"

using namespace std;

class MixingIO {
public:
    MixingIO();
    shared_ptr<FileDataSource> readFile(string filename, int fd);
    shared_ptr<BufferedDataSource> createClipboardDataSource(vector<float>& clipboard);
    void read_playback(float *targetData, int32_t numSamples);

    void setPlaying(bool setPlaying);

    void clearPlayerSources();

    void addSource(string key, shared_ptr<DataSource> source);

    void addSourceMap(map<string, shared_ptr<DataSource>> playMap);

    void syncPlayHeads();

    void setStopPlaybackCallback(function<void(void)> stopPlaybackCallback);

    int getTotalSampleFrames();

    int getCurrentPlaybackProgress();

    void setPlayHead(int position);

private:
    shared_ptr<Player> mPlayer {nullptr};
};


#endif //FAST_MIXER_MIXINGIO_H
