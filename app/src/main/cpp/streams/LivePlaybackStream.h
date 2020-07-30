//
// Created by asalehin on 7/30/20.
//

#ifndef FAST_MIXER_LIVEPLAYBACKSTREAM_H
#define FAST_MIXER_LIVEPLAYBACKSTREAM_H

#include "BaseStream.h"

class LivePlaybackStream: public BaseStream {
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

private:
    const char* TAG = "Live Playback Stream:: %s";
    RecordingIO* mRecordingIO;
};


#endif //FAST_MIXER_LIVEPLAYBACKSTREAM_H
