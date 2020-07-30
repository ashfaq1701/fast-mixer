//
// Created by asalehin on 7/30/20.
//

#ifndef FAST_MIXER_STREAMPROCESSOR_H
#define FAST_MIXER_STREAMPROCESSOR_H

#include <oboe/Definitions.h>
#include <oboe/Utilities.h>
#include <oboe/AudioStream.h>
#include "logging_macros.h"
#include "RecordingCallback.h"
#include "LivePlaybackCallback.h"
#include "PlaybackCallback.h"

class StreamProcessor {
public:
    StreamProcessor(RecordingIO* recordingIO);

    oboe::AudioStream *mRecordingStream = nullptr;
    oboe::AudioStream *mLivePlaybackStream = nullptr;
    oboe::AudioStream *mPlaybackStream = nullptr;

    static int32_t mSampleRate;
    static int32_t mPlaybackSampleRate;
    static int32_t mInputChannelCount;
    static int32_t mOutputChannelCount;

    RecordingCallback recordingCallback;
    LivePlaybackCallback livePlaybackCallback;
    PlaybackCallback playbackCallback;

    void openRecordingStream();
    void openLivePlaybackStream();
    void openPlaybackStream();
    void startStream(oboe::AudioStream *stream);
    void stopStream(oboe::AudioStream *stream);
    void closeStream(oboe::AudioStream *stream);
    oboe::AudioStreamBuilder* setupRecordingStreamParameters(oboe::AudioStreamBuilder *builder);
    oboe::AudioStreamBuilder* setupLivePlaybackStreamParameters(oboe::AudioStreamBuilder *builder,
                                                       oboe::AudioApi audioApi,
                                                       oboe::AudioFormat audioFormat,
                                                       oboe::AudioStreamCallback *audioStreamCallback,
                                                       int32_t deviceId, int32_t sampleRate,
                                                       int channelCount);
    oboe::AudioStreamBuilder* setupPlaybackStreamParameters(oboe::AudioStreamBuilder *builder,
                                                   oboe::AudioApi audioApi, oboe::AudioFormat audioFormat,
                                                   oboe::AudioStreamCallback *audioStreamCallback,
                                                   int32_t deviceId, int32_t sampleRate, int channelCount);

private:
    const char* TAG = "Stream Processor:: %s";

    oboe::AudioApi mAudioApi = oboe::AudioApi::AAudio;
    oboe::AudioFormat mFormat = oboe::AudioFormat::I16;

    int32_t mPlaybackDeviceId = oboe::kUnspecified;
    int32_t mFramesPerBurst{};

    int32_t mRecordingDeviceId = oboe::Unprocessed;
    oboe::AudioFormat mPlaybackFormat = oboe::AudioFormat::Float;

    int32_t mRecordingFramesPerCallback = 24;
    int32_t mLivePlaybackFramesPerCallback = mRecordingFramesPerCallback;

    RecordingIO* mRecordingIO;
};


#endif //FAST_MIXER_STREAMPROCESSOR_H
