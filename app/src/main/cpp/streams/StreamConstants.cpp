//
// Created by asalehin on 7/30/20.
//

#include "StreamConstants.h"

int32_t StreamConstants::mSampleRate = oboe::DefaultStreamValues::SampleRate;
int32_t StreamConstants::mPlaybackSampleRate = StreamConstants::mSampleRate;
int32_t StreamConstants::mInputChannelCount = oboe::ChannelCount::Stereo;
int32_t StreamConstants::mOutputChannelCount = oboe::ChannelCount::Stereo;
oboe::AudioApi  StreamConstants::mAudioApi = oboe::AudioApi::AAudio;
oboe::AudioFormat StreamConstants::mFormat = oboe::AudioFormat::I16;
int32_t StreamConstants::mPlaybackDeviceId = oboe::kUnspecified;
int32_t StreamConstants::mFramesPerBurst{};
int32_t StreamConstants::mRecordingDeviceId = oboe::kUnspecified;
oboe::AudioFormat StreamConstants::mPlaybackFormat = oboe::AudioFormat::Float;
// 5ms of audio data at a sampling rate of 48kHz
int32_t StreamConstants::mRecordingFramesPerCallback = 240;
// Live playback will receive half sized data of the recording callback
int32_t StreamConstants::mLivePlaybackFramesPerCallback = StreamConstants::mRecordingFramesPerCallback / 2;