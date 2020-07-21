//
// Created by Ashfaq Salehin on 15/7/2020 AD.
//

#ifndef FAST_MIXER_PLAYBACKCALLBACK_H
#define FAST_MIXER_PLAYBACKCALLBACK_H

#include <oboe/Definitions.h>
#include <oboe/AudioStream.h>
#include "logging_macros.h"
#include "RecordingIO.h"

#ifndef MODULE_NAME
#define MODULE_NAME  "PlaybackCallback"
#endif


class PlaybackCallback : public oboe::AudioStreamCallback {
private:
    const char* TAG = "PlaybackCallback:: %s";
    RecordingIO* mRecordingIO = nullptr;

public:
    PlaybackCallback() = default;

    PlaybackCallback(RecordingIO* recording) {
        mRecordingIO = recording;
    }

    oboe::DataCallbackResult
    onAudioReady(oboe::AudioStream *audioStream, void *audioData, int32_t numFrames);

    oboe::DataCallbackResult
    processPlaybackFrame(oboe::AudioStream *audioStream, int16_t *audioData, int32_t numFrames);
};


#endif //FAST_MIXER_PLAYBACKCALLBACK_H