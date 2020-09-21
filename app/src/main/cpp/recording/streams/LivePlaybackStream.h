//
// Created by asalehin on 7/30/20.
//

#ifndef FAST_MIXER_LIVEPLAYBACKSTREAM_H
#define FAST_MIXER_LIVEPLAYBACKSTREAM_H

#include "../../streams/BaseStream.h"

using namespace std;

class LivePlaybackStream: public BaseStream, public oboe::AudioStreamCallback {
public:
    LivePlaybackStream(RecordingIO* recordingIO);

    oboe::AudioStream *mLivePlaybackStream = nullptr;

    void openLivePlaybackStream();

    oboe::AudioStreamBuilder* setupLivePlaybackStreamParameters(oboe::AudioStreamBuilder *builder,
                                                                oboe::AudioApi audioApi,
                                                                oboe::AudioFormat audioFormat,
                                                                oboe::AudioStreamCallback *audioStreamCallback,
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
