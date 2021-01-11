//
// Created by asalehin on 7/30/20.
//

#ifndef FAST_MIXER_LIVEPLAYBACKSTREAM_H
#define FAST_MIXER_LIVEPLAYBACKSTREAM_H

#include "RecordingBaseStream.h"

using namespace std;

class LivePlaybackStream: public RecordingBaseStream, public oboe::AudioStreamDataCallback, public oboe::AudioStreamErrorCallback {
public:
    LivePlaybackStream(RecordingIO* recordingIO);

    oboe::Result openStream();

    oboe::AudioStreamBuilder* setupLivePlaybackStreamParameters(oboe::AudioStreamBuilder *builder,
                                                                oboe::AudioApi audioApi,
                                                                oboe::AudioFormat audioFormat,
                                                                oboe::AudioStreamDataCallback *audioStreamCallback,
                                                                int32_t deviceId, int32_t sampleRate,
                                                                int channelCount);

    oboe::DataCallbackResult
    onAudioReady(oboe::AudioStream *audioStream, void *audioData, int32_t numFrames);

    oboe::DataCallbackResult
    processLivePlaybackFrame(oboe::AudioStream *audioStream, int16_t *audioData, int32_t numFrames);

private:
    const char* TAG = "Live Playback Stream:: %s";

    void onErrorAfterClose(oboe::AudioStream* audioStream, oboe::Result result);
};


#endif //FAST_MIXER_LIVEPLAYBACKSTREAM_H
