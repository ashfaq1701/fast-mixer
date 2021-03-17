//
// Created by Ashfaq Salehin on 17/3/2021 AD.
//

#include <iostream>
#include <unistd.h>
#include "MixedAudioWriter.h"
#include "../mixing/streams/MixingStreamConstants.h"

bool MixedAudioWriter::writeToFile(int fd) {
    if (mSourceMap.size() == 0) return false;

    // Maximum addition of corresponding samples from all sources. If there are 3 tracks loaded a, b, c, then this will find max(a[i] + b[i] + c[i])
    updateAddedMax();

    // Find the maximum value among all samples in all sources
    float allMaxValue = getMaxValueAcrossSources();

    auto firstSource = mSourceMap.begin()->second;

    // All sources are assumed to have the same property
    auto properties = firstSource->getProperties();

    int bufferSize = 10 * properties.sampleRate * properties.channelCount;

    // Longest track number of samples
    int64_t maxTotalSourceFrames = getMaxTotalSourceFrames();

    // Multiply with channel count
    int64_t totalFramesTimeChannelCount = maxTotalSourceFrames * properties.channelCount;

    int format = SF_FORMAT_WAV | SF_FORMAT_FLOAT;
    SndfileHandle file = SndfileHandle(dup(fd), true,SFM_WRITE, format, MixingStreamConstants::mChannelCount, MixingStreamConstants::mSampleRate);

    // Each iteration will write 10 seconds of audio
    auto passes = (int) ceil((float) totalFramesTimeChannelCount / (float) bufferSize);

    for (int i = 0; i < passes; i++) {

        auto currentBufferSize = bufferSize;

        // If last pass, then the remaining number of samples can be less than 10 seconds
        if ((i + 1) * bufferSize > totalFramesTimeChannelCount) {
            currentBufferSize = totalFramesTimeChannelCount - i * bufferSize;
        }

        float* writeBuffer;

        try {
            writeBuffer = new float[currentBufferSize];
        } catch (const bad_alloc& e) {
            // If bad alloc, then return with failure
            return false;
        }

        for (int j = 0; j < currentBufferSize; j++) {

            float audioFrame = 0.0;

            // For each source
            for (auto it = mSourceMap.begin(); it != mSourceMap.end(); it++) {

                if (!it->second) {
                    continue;
                }

                const float *data = it->second->getData();

                // If the source is long enough to provide sample at this point of time, then add it with the buffered value
                if (i * bufferSize + j < it->second->getSize()) {
                    audioFrame += data[i * bufferSize + j];
                }
            }

            float scaledAudioFrame = audioFrame;

            // Scale frame accordingly. This will help to maintain proper gain
            if (addedMaxSampleValue != 0 && addedMaxSampleValue != FLT_MIN) {
                scaledAudioFrame = (scaledAudioFrame / addedMaxSampleValue) * allMaxValue;
            }

            // Add the scaled value to write buffer
            writeBuffer[j] = scaledAudioFrame;
        }

        // Write the buffer to file
        file.write(writeBuffer, currentBufferSize);
        // Delete the buffer, don't fill up the RAM :)
        delete[] writeBuffer;
    }

    return true;
}
