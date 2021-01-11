//
// Created by Ashfaq Salehin on 9/10/2020 AD.
//

#include "StreamConstants.h"

oboe::AudioApi StreamConstants::mAudioApi = oboe::AudioApi::AAudio;
oboe::AudioFormat StreamConstants::mFormat = oboe::AudioFormat::Float;
int32_t StreamConstants::mDeviceId = oboe::kUnspecified;
int32_t StreamConstants::mSampleRate = oboe::DefaultStreamValues::SampleRate;
int32_t StreamConstants::mChannelCount = oboe::ChannelCount::Stereo;
