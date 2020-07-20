//
// Created by asalehin on 7/11/20.
//

#ifndef FAST_MIXER_RECORDINGCALLBACK_H
#define FAST_MIXER_RECORDINGCALLBACK_H

#include <oboe/Definitions.h>
#include <oboe/AudioStream.h>
#include "SoundIO.h"
#include "logging_macros.h"

#ifndef MODULE_NAME
#define MODULE_NAME  "RecordingCallback"
#endif

class RecordingCallback : public oboe::AudioStreamCallback {

private:
    const char* TAG = "RecordingCallback:: %s";
    SoundIO* mSoundIO = nullptr;

public:
    RecordingCallback() = default;

    explicit RecordingCallback(SoundIO* recording) {
        mSoundIO = recording;
    }

    oboe::DataCallbackResult
    onAudioReady(oboe::AudioStream *audioStream, void *audioData, int32_t numFrames);

    oboe::DataCallbackResult
    processRecordingFrames(oboe::AudioStream *audioStream, int16_t *audioData, int32_t numFrames);
};
#endif //FAST_MIXER_RECORDINGCALLBACK_H
