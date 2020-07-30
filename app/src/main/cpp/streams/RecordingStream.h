//
// Created by asalehin on 7/30/20.
//

#ifndef FAST_MIXER_RECORDINGSTREAM_H
#define FAST_MIXER_RECORDINGSTREAM_H

#include "BaseStream.h"

class RecordingStream: public BaseStream {
public:
    RecordingStream(RecordingIO* recordingIO);

    oboe::AudioStream *mRecordingStream = nullptr;

    void openRecordingStream();
    oboe::AudioStreamBuilder* setupRecordingStreamParameters(oboe::AudioStreamBuilder *builder);

private:
    const char* TAG = "Recording Stream:: %s";
};


#endif //FAST_MIXER_RECORDINGSTREAM_H
