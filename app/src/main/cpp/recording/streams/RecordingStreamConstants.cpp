//
// Created by asalehin on 7/30/20.
//

#include "RecordingStreamConstants.h"

int32_t RecordingStreamConstants::mSampleRate = oboe::DefaultStreamValues::SampleRate;
int32_t RecordingStreamConstants::mPlaybackSampleRate = RecordingStreamConstants::mSampleRate;
int32_t RecordingStreamConstants::mInputChannelCount = oboe::ChannelCount::Stereo;
int32_t RecordingStreamConstants::mOutputChannelCount = oboe::ChannelCount::Stereo;
oboe::AudioApi RecordingStreamConstants::mAudioApi = oboe::AudioApi::AAudio;
oboe::AudioFormat RecordingStreamConstants::mFormat = oboe::AudioFormat::I16;
int32_t RecordingStreamConstants::mPlaybackDeviceId = oboe::kUnspecified;
int32_t RecordingStreamConstants::mFramesPerBurst{};
int32_t RecordingStreamConstants::mRecordingDeviceId = oboe::kUnspecified;
oboe::AudioFormat RecordingStreamConstants::mPlaybackFormat = oboe::AudioFormat::Float;
oboe::InputPreset RecordingStreamConstants::mRecordingPreset = oboe::InputPreset::VoicePerformance;
