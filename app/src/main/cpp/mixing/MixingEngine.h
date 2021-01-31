//
// Created by asalehin on 9/9/20.
//

#ifndef FAST_MIXER_MIXINGENGINE_H
#define FAST_MIXER_MIXINGENGINE_H

#include <oboe/Definitions.h>
#include <oboe/AudioStream.h>
#include "../SourceMapStore.h"
#include "../logging_macros.h"
#include "streams/MixingPlaybackStream.h"
#include "MixingIO.h"
#include "map"

using namespace std;

class MixingEngine {

public:
    MixingEngine(SourceMapStore* sourceMapStore);
    void addFile(string filePath);
    unique_ptr<buffer_data> readSamples(string filePath, size_t countPoints);
    void deleteFile(string filePath);
    int64_t getAudioFileTotalSamples(string filePath);

    bool startPlayback();
    void pausePlayback();

    void gainSourceByDb(string filePath, float db);

    void applySourceTransformation(string filePath);

    void clearSourceTransformation(string filePath);

    void addSourcesToPlayer(string* strArr, int count);

    void clearPlayerSources();

    int getTotalSampleFrames();

    int getCurrentPlaybackProgress();

    void setPlayerHead(int playHead);

    void setSourcePlayHead(string filePath, int playHead);

private:

    const char* TAG = "Mixing Engine:: %s";

    MixingIO mMixingIO;

    SourceMapStore* mSourceMapStore;

    mutex playbackStreamMtx;

    MixingPlaybackStream playbackStream = MixingPlaybackStream(&mMixingIO);

    bool startPlaybackCallable();

    void stopPlaybackCallable();

    void closePlaybackStream();

    void setStopPlayback();
};


#endif //FAST_MIXER_MIXINGENGINE_H
