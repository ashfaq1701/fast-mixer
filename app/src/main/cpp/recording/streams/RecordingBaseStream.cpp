//
// Created by Ashfaq Salehin on 11/1/2021 AD.
//

#include "RecordingBaseStream.h"

RecordingBaseStream::RecordingBaseStream(shared_ptr<RecordingIO> recordingIO) : BaseStream() {
    mRecordingIO = recordingIO;
}
