//
// Created by asalehin on 7/9/20.
//

#ifndef FAST_MIXER_RECORDINGENGINE_H
#define FAST_MIXER_RECORDINGENGINE_H

#ifndef MODULE_NAME
#define MODULE_NAME "AudioEngine"
#endif

#include <oboe/Definitions.h>
#include <oboe/AudioStream.h>
#include "../logging_macros.h"
#include "../SourceMapStore.h"
#include "RecordingIO.h"
#include "streams/RecordingStream.h"
#include "streams/LivePlaybackStream.h"
#include "streams/PlaybackStream.h"

class RecordingEngine {

public:

    RecordingEngine(string recordingFileDir, bool recordingScreenViewModelPassed);
    ~RecordingEngine();

    void startRecording();
    void stopRecording();

    void setupAudioSource(int fd);

    void startLivePlayback();
    void stopLivePlayback();

    bool startPlayback();
    bool startPlaybackWithMixingTracks();
    void startPlayingWithMixingTracksWithoutSetup();
    bool startMixingTracksPlayback();
    void stopMixingTracksPlayback();
    void stopAndResetPlayback();
    void pausePlayback();

    void flushWriteBuffer();
    void restartPlayback();
    void restartPlaybackWithMixingTracks();

    int getCurrentMax();

    void resetCurrentMax();

    void setStopPlayback();

    int getTotalSampleFrames();

    int getCurrentPlaybackProgress();

    void setPlayHead(int position);

    int getDurationInSeconds();

    void resetAudioEngine();

    void closePlaybackStream();

    void stopPlaybackCallable();

    bool startPlaybackCallable();

    bool startPlaybackWithMixingTracksCallable();

    bool startMixingTracksPlaybackCallable();

    void addSourcesToPlayer(string* strArr, int count);

private:

    const char* TAG = "Recording Engine:: %s";

    string mRecordingFileDir = nullptr;
    bool mPlayback = true;
    bool isDirty = false;

    int bakPlayHead = 0;

    mutex recordingStreamMtx;
    mutex livePlaybackStreamMtx;
    mutex playbackStreamMtx;

    shared_ptr<SourceMapStore> mSourceMapStore;
    shared_ptr<RecordingIO> mRecordingIO {
        new RecordingIO()
    };

    shared_ptr<FileDataSource> mDataSource {nullptr};

    unique_ptr<RecordingStream> recordingStream {
        new RecordingStream(mRecordingIO)
    };
    unique_ptr<LivePlaybackStream> livePlaybackStream{
        new LivePlaybackStream(mRecordingIO)
    };
    unique_ptr<PlaybackStream> playbackStream {
        new PlaybackStream(mRecordingIO)
    };
    bool mRecordingScreenViewModelPassed = false;

    void stopPlayback();
};


#endif //FAST_MIXER_RECORDINGENGINE_H
