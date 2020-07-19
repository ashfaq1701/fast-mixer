//
// Created by Ashfaq Salehin on 15/7/2020 AD.
//

#include "LivePlaybackCallback.h"
#include "Utils.h"

oboe::DataCallbackResult
LivePlaybackCallback::onAudioReady(oboe::AudioStream *audioStream, void *audioData,
                                   int32_t numFrames) {
    return processLivePlaybackFrame(audioStream, static_cast<int16_t *>(audioData), numFrames * audioStream->getChannelCount());
}

oboe::DataCallbackResult
LivePlaybackCallback::processLivePlaybackFrame(oboe::AudioStream *audioStream, int16_t *audioData,
                                             int32_t numFrames) {
    fillArrayWithZeros(audioData, numFrames);
    int64_t framesWritten = mSoundRecording->read(audioData, numFrames);
    return oboe::DataCallbackResult::Continue;
}
