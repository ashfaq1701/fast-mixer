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
#include <vector>

using namespace std;

class MixingEngine {

public:

    MixingEngine();

    ~MixingEngine();

    bool addFile(string filePath, int fd);
    shared_ptr<buffer_data> readSamples(string filePath, size_t countPoints);
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

    void setSourceBounds(string filePath, int64_t start, int64_t end);

    void resetSourceBounds(string filePath);

    int64_t shiftBySamples(string filePath, int64_t position, int64_t numSamples);

    int64_t cutToClipboard(string filePath, int64_t startPosition, int64_t endPosition);

    bool copyToClipboard(string filePath, int64_t startPosition, int64_t endPosition);

    bool muteAndCopyToClipboard(string filePath, int64_t startPosition, int64_t endPosition);

    void pasteFromClipboard(string filePath, int64_t position);

    void pasteNewFromClipboard(string fileId);

    void setPlayerBoundStart(int64_t boundStart);

    void setPlayerBoundEnd(int64_t boundEnd);

    void resetPlayerBoundStart();

    void resetPlayerBoundEnd();

    bool writeToFile(string* filePaths, int count, int fd);

private:

    const char* TAG = "Mixing Engine:: %s";

    shared_ptr<MixingIO> mMixingIO {
        new MixingIO()
    };

    shared_ptr<SourceMapStore> mSourceMapStore;

    vector<float> clipboard;

    mutex playbackStreamMtx;

    unique_ptr<MixingPlaybackStream> playbackStream {
        new MixingPlaybackStream(mMixingIO)
    };

    bool startPlaybackCallable();

    void stopPlaybackCallable();

    void closePlaybackStream();

    void setStopPlayback();
};


#endif //FAST_MIXER_MIXINGENGINE_H
