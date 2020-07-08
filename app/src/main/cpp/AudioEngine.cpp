//
// Created by asalehin on 7/9/20.
//

#include <oboe/Oboe.h>
#include "AudioEngine.h"
#include "logging_macros.h"

AudioEngine::AudioEngine() {
    assert(mInputChannelCount == mOutputChannelCount);
}

AudioEngine::~AudioEngine() {

}