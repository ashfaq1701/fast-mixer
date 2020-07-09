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

void AudioEngine::openRecordingStream() {

}

void AudioEngine::openLivePlaybackStream() {

}

void AudioEngine::openPlaybackStream() {

}

void AudioEngine::startStream(oboe::AudioStream *stream) {

}

void AudioEngine::stopStream(oboe::AudioStream *stream) {

}

void AudioEngine::closeStream(oboe::AudioStream *stream) {

}

oboe::AudioStreamBuilder *
AudioEngine::setupRecordingStreamParameters(oboe::AudioStreamBuilder *builder) {
    return nullptr;
}

oboe::AudioStreamBuilder *
AudioEngine::setupLivePlaybackStreamParameters(oboe::AudioStreamBuilder *builder,
                                               oboe::AudioApi audioApi,
                                               oboe::AudioFormat audioFormat,
                                               oboe::AudioStreamCallback *audioStreamCallback,
                                               int32_t deviceId, int32_t sampleRate,
                                               int channelCount) {
    return nullptr;
}

oboe::AudioStreamBuilder *
AudioEngine::setupPlaybackStreamParameters(oboe::AudioStreamBuilder *builder,
                                           oboe::AudioApi audioApi, oboe::AudioFormat audioFormat,
                                           oboe::AudioStreamCallback *audioStreamCallback,
                                           int32_t deviceId, int32_t sampleRate, int channelCount) {
    return nullptr;
}
