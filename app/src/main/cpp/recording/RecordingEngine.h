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
#include "RecordingIO.h"
#include "../streams/BaseStream.h"
#include "streams/RecordingStream.h"
#include "streams/LivePlaybackStream.h"
#include "streams/PlaybackStream.h"

class RecordingEngine {
public:
    RecordingEngine(string appDir, string recordingSessionId, bool recordingScreenViewModelPassed);
    ~RecordingEngine();

    void startRecording();
    void stopRecording();

    void startLivePlayback();
    void stopLivePlayback();

    bool startPlayback();
    void stopAndResetPlayback();
    void pausePlayback();

    void flushWriteBuffer();
    void restartPlayback();

    int getCurrentMax();

    void resetCurrentMax();

    void setStopPlayback();

    int getTotalRecordedFrames();

    int getCurrentPlaybackProgress();

    void setPlayHead(int position);

    int getDurationInSeconds();

    void resetAudioEngine();

private:
    const char* TAG = "Audio Engine:: %s";

    string mRecordingSessionId = nullptr;
    string mAppDir = nullptr;
    bool mPlayback = true;

    RecordingIO mRecordingIO;

    mutex recordingStreamMtx;
    mutex livePlaybackStreamMtx;
    mutex playbackStreamMtx;

    RecordingStream recordingStream = RecordingStream(&mRecordingIO, recordingStreamMtx);
    LivePlaybackStream livePlaybackStream = LivePlaybackStream(&mRecordingIO, livePlaybackStreamMtx);
    PlaybackStream playbackStream = PlaybackStream(&mRecordingIO, playbackStreamMtx);
    bool mRecordingScreenViewModelPassed = false;

    void closePlaybackStream();

    void stopPlayback();
};


#endif //FAST_MIXER_RECORDINGENGINE_H
