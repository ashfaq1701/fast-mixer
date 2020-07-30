//
// Created by asalehin on 7/30/20.
//

#ifndef FAST_MIXER_PLAYBACKSTREAM_H
#define FAST_MIXER_PLAYBACKSTREAM_H

#include "BaseStream.h"

class PlaybackStream: public BaseStream {
public:
    PlaybackStream(RecordingIO* recordingIO);

    oboe::AudioStream *mPlaybackStream = nullptr;

    void openPlaybackStream();

    oboe::AudioStreamBuilder* setupPlaybackStreamParameters(oboe::AudioStreamBuilder *builder,
                                                            oboe::AudioApi audioApi, oboe::AudioFormat audioFormat,
                                                            oboe::AudioStreamCallback *audioStreamCallback,
                                                            int32_t deviceId, int32_t sampleRate, int channelCount);
private:
    const char* TAG = "Playback Stream:: %s";
    RecordingIO* mRecordingIO;
};


#endif //FAST_MIXER_PLAYBACKSTREAM_H
