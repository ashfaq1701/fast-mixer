//
// Created by asalehin on 7/11/20.
//

#include "RecordingCallback.h"

oboe::DataCallbackResult
RecordingCallback::onAudioReady(oboe::AudioStream *audioStream, void *audioData, int32_t numFrames) {
    return processRecordingFrames(audioStream, static_cast<int16_t *>(audioData), numFrames * audioStream->getChannelCount());
}

oboe::DataCallbackResult
RecordingCallback::processRecordingFrames(oboe::AudioStream *audioStream, int16_t *audioData,
                                          int32_t numFrames) {
    int32_t framesWritten = mSoundIO->write(audioData, numFrames);
    return oboe::DataCallbackResult::Continue;
}
