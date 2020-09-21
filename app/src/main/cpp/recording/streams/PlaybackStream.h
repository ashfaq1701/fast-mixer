//
// Created by asalehin on 7/30/20.
//

#ifndef FAST_MIXER_PLAYBACKSTREAM_H
#define FAST_MIXER_PLAYBACKSTREAM_H

#include "../../streams/BaseStream.h"

using namespace std;

class PlaybackStream: public BaseStream, public oboe::AudioStreamCallback {
public:
    PlaybackStream(RecordingIO* recordingIO);

    oboe::AudioStream *mPlaybackStream = nullptr;

    void openPlaybackStream();

    oboe::AudioStreamBuilder* setupPlaybackStreamParameters(oboe::AudioStreamBuilder *builder,
                                                            oboe::AudioApi audioApi, oboe::AudioFormat audioFormat,
                                                            oboe::AudioStreamCallback *audioStreamCallback,
                                                            int32_t deviceId, int32_t sampleRate, int channelCount);

    oboe::DataCallbackResult
    onAudioReady(oboe::AudioStream *audioStream, void *audioData, int32_t numFrames);

    oboe::DataCallbackResult
    processPlaybackFrame(oboe::AudioStream *audioStream, float *audioData, int32_t numFrames, int32_t channelCount);
private:
    const char* TAG = "Playback Stream:: %s";

    void onErrorAfterClose(oboe::AudioStream* audioStream, oboe::Result result);
};


#endif //FAST_MIXER_PLAYBACKSTREAM_H
