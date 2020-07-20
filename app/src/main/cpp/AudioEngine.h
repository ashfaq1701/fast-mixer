//
// Created by asalehin on 7/9/20.
//

#ifndef FAST_MIXER_AUDIOENGINE_H
#define FAST_MIXER_AUDIOENGINE_H

#ifndef MODULE_NAME
#define MODULE_NAME "AudioEngine"
#endif

#include <oboe/Definitions.h>
#include <oboe/AudioStream.h>
#include "logging_macros.h"
#include "RecordingCallback.h"
#include "SoundIO.h"
#include "LivePlaybackCallback.h"
#include "PlaybackCallback.h"

class AudioEngine {
public:
    AudioEngine(char* appDir, char* mRecordingSessionId);
    ~AudioEngine();

    RecordingCallback recordingCallback;
    LivePlaybackCallback livePlaybackCallback;
    PlaybackCallback playbackCallback;

    void startRecording();
    void stopRecording();
    void pauseRecording();

    void startLivePlayback();
    void stopLivePlayback();
    void pauseLivePlayback();

    void startPlayback();
    void stopPlayback();
    void pausePlayback();

private:
    const char* TAG = "Audio Engine:: %s";

    char* mRecordingSessionId = nullptr;
    char* mAppDir = nullptr;
    bool mPlayback = true;

    int32_t mRecordingDeviceId = oboe::Unprocessed;
    int32_t mPlaybackDeviceId = oboe::kUnspecified;
    oboe::AudioFormat mFormat = oboe::AudioFormat::I16;
    int32_t mSampleRate = oboe::kUnspecified;
    int32_t mFramesPerBurst{};

    int32_t mInputChannelCount = oboe::ChannelCount::Stereo;
    int32_t mOutputChannelCount = oboe::ChannelCount::Stereo;

    oboe::AudioApi mAudioApi = oboe::AudioApi::AAudio;
    oboe::AudioStream *mRecordingStream = nullptr;
    oboe::AudioStream *mLivePlaybackStream = nullptr;
    oboe::AudioStream *mPlaybackStream = nullptr;

    int32_t mRecordingFramesPerCallback = 24;
    int32_t mLivePlaybackFramesPerCallback = mRecordingFramesPerCallback;

    SoundIO mSoundIO;

    void openRecordingStream();
    void openLivePlaybackStream();
    void openPlaybackStream();

    void startStream(oboe::AudioStream *stream);
    void stopStream(oboe::AudioStream *stream);
    void closeStream(oboe::AudioStream *stream);

    oboe::AudioStreamBuilder* setupRecordingStreamParameters(oboe::AudioStreamBuilder* builder);
    oboe::AudioStreamBuilder* setupLivePlaybackStreamParameters(oboe::AudioStreamBuilder* builder, oboe::AudioApi audioApi,
            oboe::AudioFormat audioFormat, oboe::AudioStreamCallback *audioStreamCallback, int32_t deviceId,
            int32_t sampleRate, int channelCount);
    oboe::AudioStreamBuilder* setupPlaybackStreamParameters(oboe::AudioStreamBuilder* builder, oboe::AudioApi audioApi,
            oboe::AudioFormat audioFormat, oboe::AudioStreamCallback *audioStreamCallback, int32_t deviceId,
            int32_t sampleRate, int channelCount);
};


#endif //FAST_MIXER_AUDIOENGINE_H
