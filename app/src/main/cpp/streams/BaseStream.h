//
// Created by asalehin on 7/30/20.
//

#ifndef FAST_MIXER_BASESTREAM_H
#define FAST_MIXER_BASESTREAM_H

#include "oboe/Definitions.h"
#include "oboe/Utilities.h"
#include "oboe/AudioStream.h"
#include "StreamConstants.h"
#include "../logging_macros.h"
#include "../RecordingIO.h"

class BaseStream {
public:
    BaseStream(RecordingIO* recordingIO);

    void startStream(oboe::AudioStream *stream);
    void stopStream(oboe::AudioStream *stream);
    void closeStream(oboe::AudioStream *stream);

private:
    const char* TAG = "Stream Processor:: %s";
    RecordingIO* mRecordingIO;
};


#endif //FAST_MIXER_BASESTREAM_H
