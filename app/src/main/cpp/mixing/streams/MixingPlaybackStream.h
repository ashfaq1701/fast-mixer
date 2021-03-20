//
// Created by Ashfaq Salehin on 11/1/2021 AD.
//

#ifndef FAST_MIXER_MixingPlaybackStream_H
#define FAST_MIXER_MixingPlaybackStream_H

#include "MixingBaseStream.h"

using namespace std;

class MixingPlaybackStream : public MixingBaseStream, public oboe::AudioStreamDataCallback {

public:

    MixingPlaybackStream(shared_ptr<MixingIO> mixingIO);

    oboe::Result openStream();

    oboe::AudioStreamBuilder* setupMixingPlaybackStreamParameters(oboe::AudioStreamBuilder *builder,
                                                            oboe::AudioApi audioApi, oboe::AudioFormat audioFormat,
                                                            oboe::AudioStreamDataCallback *audioStreamCallback,
                                                            int32_t deviceId, int32_t sampleRate, int channelCount);

    oboe::DataCallbackResult
    onAudioReady(oboe::AudioStream *audioStream, void *audioData, int32_t numFrames);

    oboe::DataCallbackResult
    processPlaybackFrame(oboe::AudioStream *audioStream, float *audioData, int32_t numFrames, int32_t channelCount);

private:
    const char* TAG = "Playback Stream:: %s";
};


#endif //FAST_MIXER_MixingPlaybackStreamStreamConstants_H
