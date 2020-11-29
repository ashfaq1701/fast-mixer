//
// Created by asalehin on 7/30/20.
//

#ifndef FAST_MIXER_STREAMCONSTANTS_H
#define FAST_MIXER_STREAMCONSTANTS_H

#include "oboe/Definitions.h"

class StreamConstants {
public:
    static int32_t mSampleRate;
    static int32_t mPlaybackSampleRate;
    static int32_t mInputChannelCount;
    static int32_t mOutputChannelCount;
    static oboe::AudioApi mAudioApi;
    static oboe::AudioFormat mFormat;

    static int32_t mPlaybackDeviceId;
    static int32_t mFramesPerBurst;

    static int32_t mRecordingDeviceId;
    static oboe::AudioFormat mPlaybackFormat;

    static int32_t mRecordingFramesPerCallback;
    static int32_t mLivePlaybackFramesPerCallback;
    static int32_t mPlaybackFramesPerCallback;
};


#endif //FAST_MIXER_STREAMCONSTANTS_H
