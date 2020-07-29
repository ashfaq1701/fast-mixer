//
// Created by Ashfaq Salehin on 15/7/2020 AD.
//

#include "PlaybackCallback.h"
#include "Utils.h"

oboe::DataCallbackResult
PlaybackCallback::onAudioReady(oboe::AudioStream *audioStream, void *audioData,
                               int32_t numFrames) {
    return processPlaybackFrame(audioStream, static_cast<float_t *>(audioData), numFrames, audioStream->getChannelCount());
}

oboe::DataCallbackResult
PlaybackCallback::processPlaybackFrame(oboe::AudioStream *audioStream, float *audioData,
                                       int32_t numFrames, int32_t channelCount) {
    mRecordingIO->read_playback(audioData, numFrames, channelCount);
    return oboe::DataCallbackResult::Continue;
}