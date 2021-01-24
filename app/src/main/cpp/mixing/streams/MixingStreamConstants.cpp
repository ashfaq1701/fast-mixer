//
// Created by Ashfaq Salehin on 9/10/2020 AD.
//

#include "MixingStreamConstants.h"

oboe::AudioApi MixingStreamConstants::mAudioApi = oboe::AudioApi::AAudio;
oboe::AudioFormat MixingStreamConstants::mFormat = oboe::AudioFormat::Float;
int32_t MixingStreamConstants::mDeviceId = oboe::kUnspecified;
int32_t MixingStreamConstants::mSampleRate = oboe::DefaultStreamValues::SampleRate;
int32_t MixingStreamConstants::mChannelCount = oboe::ChannelCount::Stereo;
