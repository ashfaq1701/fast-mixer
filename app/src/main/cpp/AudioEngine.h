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
#include "RecordingCallback.h"
#include "RecordingIO.h"
#include "LivePlaybackCallback.h"
#include "PlaybackCallback.h"
#include "streams/BaseStream.h"
#include "streams/RecordingStream.h"
#include "streams/LivePlaybackStream.h"
#include "streams/PlaybackStream.h"

class AudioEngine {
public:
    AudioEngine(char* appDir, char* mRecordingSessionId);
    ~AudioEngine();

    void startRecording();
    void stopRecording();
    void pauseRecording();

    void startLivePlayback();
    void stopLivePlayback();
    void pauseLivePlayback();

    void startPlayback();
    void stopPlayback();
    void pausePlayback();

private:
    const char* TAG = "Audio Engine:: %s";

    char* mRecordingSessionId = nullptr;
    char* mAppDir = nullptr;
    bool mPlayback = true;

    RecordingIO mRecordingIO;
    BaseStream streamProcessor = BaseStream(&mRecordingIO);
    RecordingStream recordingStream = RecordingStream(&mRecordingIO);
    LivePlaybackStream livePlaybackStream = LivePlaybackStream(&mRecordingIO);
    PlaybackStream playbackStream = PlaybackStream(&mRecordingIO);
};


#endif //FAST_MIXER_AUDIOENGINE_H
