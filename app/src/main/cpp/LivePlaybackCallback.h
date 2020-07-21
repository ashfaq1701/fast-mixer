//
// Created by Ashfaq Salehin on 15/7/2020 AD.
//

#ifndef FAST_MIXER_LIVEPLAYBACKCALLBACK_H
#define FAST_MIXER_LIVEPLAYBACKCALLBACK_H

#include <oboe/Definitions.h>
#include <oboe/AudioStream.h>
#include "logging_macros.h"
#include "RecordingIO.h"

#ifndef MODULE_NAME
#define MODULE_NAME  "LivePlaybackCallback"
#endif


class LivePlaybackCallback : public oboe::AudioStreamCallback {
private:
    const char* TAG = "LivePlaybackCallback:: %s";
    RecordingIO* mRecordingIO = nullptr;

public:
    LivePlaybackCallback() = default;

    LivePlaybackCallback(RecordingIO* recording) {
        mRecordingIO = recording;
    }

    oboe::DataCallbackResult
    onAudioReady(oboe::AudioStream *audioStream, void *audioData, int32_t numFrames);

    oboe::DataCallbackResult
    processLivePlaybackFrame(oboe::AudioStream *audioStream, int16_t *audioData, int32_t numFrames);
};


#endif //FAST_MIXER_LIVEPLAYBACKCALLBACK_H
