//
// Created by asalehin on 7/30/20.
//

#ifndef FAST_MIXER_BASESTREAM_H
#define FAST_MIXER_BASESTREAM_H

#include "oboe/Definitions.h"
#include "oboe/Utilities.h"
#include "oboe/AudioStream.h"
#include "../recording/streams/StreamConstants.h"
#include "../logging_macros.h"
#include "../recording/RecordingIO.h"

using namespace std;

class BaseStream {
public:
    BaseStream(RecordingIO* recordingIO, mutex& mtx);

    RecordingIO* mRecordingIO;

    shared_ptr<oboe::AudioStream> mStream;

    mutex& mLock;

    virtual oboe::Result openStream() = 0;

    oboe::Result startStream();
    void stopStream();

    void resetStream();

private:
    const char* TAG = "Stream Processor:: %s";


};


#endif //FAST_MIXER_BASESTREAM_H
