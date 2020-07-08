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

class AudioEngine {
public:
    AudioEngine();
    ~AudioEngine();


private:
    const char* TAG = "Audio Engine: %s";

    int32_t mInputChannelCount = oboe::ChannelCount::Stereo;
    int32_t mOutputChannelCount = oboe::ChannelCount::Stereo;
};


#endif //FAST_MIXER_AUDIOENGINE_H
