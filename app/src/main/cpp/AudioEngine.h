//
// Created by asalehin on 7/9/20.
//

#ifndef FAST_MIXER_AUDIOENGINE_H
#define FAST_MIXER_AUDIOENGINE_H

#ifndef MODULE_NAME
#define MODULE_NAME "AudioEngine"
#endif

#include <oboe/Definitions.h>
#include <oboe/AudioStream.h>
#include "logging_macros.h"
#include "RecordingIO.h"
#include "streams/BaseStream.h"
#include "streams/RecordingStream.h"
#include "streams/LivePlaybackStream.h"
#include "streams/PlaybackStream.h"

class AudioEngine {
public:
    AudioEngine(char* appDir, char* mRecordingSessionId, bool recordingScreenViewModelPassed);
    ~AudioEngine();

    void startRecording();
    void stopRecording();
    void pauseRecording();

    void startLivePlayback();
    void stopLivePlayback();
    void pauseLivePlayback();

    void startPlayback();
    void stopAndResetPlayback();
    void pausePlayback();

    void flushWriteBuffer();
    void restartPlayback();

    int getCurrentMax();

    void resetCurrentMax();

    void togglePlayback();

private:
    const char* TAG = "Audio Engine:: %s";

    char* mRecordingSessionId = nullptr;
    char* mAppDir = nullptr;
    bool mPlayback = true;

    RecordingIO mRecordingIO;
    RecordingStream recordingStream = RecordingStream(&mRecordingIO);
    LivePlaybackStream livePlaybackStream = LivePlaybackStream(&mRecordingIO);
    PlaybackStream playbackStream = PlaybackStream(&mRecordingIO);
    bool mRecordingScreenViewModelPassed = false;

    void closePlaybackStream();

    void stopPlayback();
};


#endif //FAST_MIXER_AUDIOENGINE_H
