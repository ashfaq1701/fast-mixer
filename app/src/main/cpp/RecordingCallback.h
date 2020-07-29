//
// Created by asalehin on 7/11/20.
//

#ifndef FAST_MIXER_RECORDINGCALLBACK_H
#define FAST_MIXER_RECORDINGCALLBACK_H

#include <oboe/Definitions.h>
#include <oboe/AudioStream.h>
#include "RecordingIO.h"
#include "logging_macros.h"

#ifndef MODULE_NAME
#define MODULE_NAME  "RecordingCallback"
#endif

class RecordingCallback : public oboe::AudioStreamCallback {

private:
    const char* TAG = "RecordingCallback:: %s";
    RecordingIO* mRecordingIO = nullptr;

public:
    RecordingCallback() = default;

    explicit RecordingCallback(RecordingIO* recording) {
        mRecordingIO = recording;
    }

    oboe::DataCallbackResult
    onAudioReady(oboe::AudioStream *audioStream, void *audioData, int32_t numFrames);

    oboe::DataCallbackResult
    processRecordingFrames(oboe::AudioStream *audioStream, int16_t *audioData, int32_t numFrames);
};
#endif //FAST_MIXER_RECORDINGCALLBACK_H
