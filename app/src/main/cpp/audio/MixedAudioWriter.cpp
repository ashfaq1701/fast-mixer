//
// Created by Ashfaq Salehin on 17/3/2021 AD.
//

#include <iostream>
#include "MixedAudioWriter.h"
#include "../mixing/streams/MixingStreamConstants.h"

bool MixedAudioWriter::writeToFile(int fd) {
    if (mSourceMap.size() == 0) return false;

    updateAddedMax();

    float allMaxValue = getMaxValueAcrossSources();

    auto firstSource = mSourceMap.begin()->second;

    auto properties = firstSource->getProperties();

    int bufferSize = 10 * properties.sampleRate * properties.channelCount;

    int64_t maxTotalSourceFrames = getMaxTotalSourceFrames();
    int64_t totalFramesTimeChannelCount = maxTotalSourceFrames * properties.channelCount;

    auto passes = (int) ceil((float) totalFramesTimeChannelCount / (float) bufferSize);

    for (int i = 0; i < passes; i++) {

        auto currentBufferSize = bufferSize;

        if ((i + 1) * bufferSize > totalFramesTimeChannelCount) {
            currentBufferSize = totalFramesTimeChannelCount - i * bufferSize;
        }

        float* writeBuffer;

        try {
            writeBuffer = new float[currentBufferSize];
        } catch (const bad_alloc& e) {
            return false;
        }

        for (int j = 0; j < currentBufferSize; j++) {

            float audioFrame = 0.0;

            for (auto it = mSourceMap.begin(); it != mSourceMap.end(); it++) {

                if (!it->second) {
                    continue;
                }

                const float *data = it->second->getData();

                if (i * bufferSize + j < it->second->getSize()) {
                    audioFrame += data[i * bufferSize + j];
                }
            }

            float scaledAudioFrame = audioFrame;

            if (addedMaxSampleValue != 0 && addedMaxSampleValue != FLT_MIN) {
                scaledAudioFrame = (scaledAudioFrame / addedMaxSampleValue) * allMaxValue;
            }

            writeBuffer[j] = scaledAudioFrame;
        }

        int format = SF_FORMAT_WAV | SF_FORMAT_FLOAT;
        SndfileHandle file = SndfileHandle(fd, false,SFM_WRITE, format, MixingStreamConstants::mChannelCount, MixingStreamConstants::mSampleRate);

        file.write(writeBuffer, currentBufferSize);
        delete[] writeBuffer;
    }

    return true;
}
