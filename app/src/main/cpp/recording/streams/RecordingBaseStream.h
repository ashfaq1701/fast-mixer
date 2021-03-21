//
// Created by Ashfaq Salehin on 11/1/2021 AD.
//

#ifndef FAST_MIXER_RECORDINGBASESTREAM_H
#define FAST_MIXER_RECORDINGBASESTREAM_H

#include "../../streams/BaseStream.h"
#include "../RecordingIO.h"
#include "RecordingStreamConstants.h"

class RecordingBaseStream : public BaseStream {
public:
    RecordingBaseStream(shared_ptr<RecordingIO> recordingIO);
    shared_ptr<RecordingIO> mRecordingIO;
};


#endif //FAST_MIXER_RECORDINGBASESTREAM_H
