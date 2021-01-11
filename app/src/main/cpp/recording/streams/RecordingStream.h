//
// Created by asalehin on 7/30/20.
//

#ifndef FAST_MIXER_RECORDINGSTREAM_H
#define FAST_MIXER_RECORDINGSTREAM_H

#include "RecordingBaseStream.h"

using namespace std;

class RecordingStream: public RecordingBaseStream, public oboe::AudioStreamDataCallback, public oboe::AudioStreamErrorCallback {
public:
    RecordingStream(RecordingIO* recordingIO);

    oboe::Result openStream();

    oboe::AudioStreamBuilder* setupRecordingStreamParameters(oboe::AudioStreamBuilder *builder);

    oboe::DataCallbackResult
    onAudioReady(oboe::AudioStream *audioStream, void *audioData, int32_t numFrames);

    oboe::DataCallbackResult
    processRecordingFrames(oboe::AudioStream *audioStream, int16_t *audioData, int32_t numFrames);

private:
    const char* TAG = "Recording Stream:: %s";

    void onErrorAfterClose(oboe::AudioStream *audioStream, oboe::Result result);
};


#endif //FAST_MIXER_RECORDINGSTREAM_H
