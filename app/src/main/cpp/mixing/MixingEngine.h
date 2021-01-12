//
// Created by asalehin on 9/9/20.
//

#ifndef FAST_MIXER_MIXINGENGINE_H
#define FAST_MIXER_MIXINGENGINE_H

#include <oboe/Definitions.h>
#include <oboe/AudioStream.h>
#include "../logging_macros.h"
#include "streams/PlaybackStream.h"
#include "MixingIO.h"
#include "map"

using namespace std;

class MixingEngine {

public:
    ~MixingEngine();
    void addFile(string filePath);
    unique_ptr<buffer_data> readSamples(string filePath, size_t countPoints);
    void deleteFile(string filePath);
    int64_t getAudioFileTotalSamples(string filePath);

    bool startPlayback();
    void pausePlayback();

    void addSourcesToPlayer(string* strArr, int count);

    void clearPlayerSources();

private:

    const char* TAG = "Mixing Engine:: %s";

    MixingIO mMixingIO;
    map<string, shared_ptr<FileDataSource>> sourceMap;

    mutex playbackStreamMtx;

    PlaybackStream playbackStream = PlaybackStream(&mMixingIO);

    bool startPlaybackCallable();

    void stopPlaybackCallable();

    void closePlaybackStream();
};


#endif //FAST_MIXER_MIXINGENGINE_H
