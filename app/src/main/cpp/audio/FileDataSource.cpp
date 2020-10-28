/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


#include "../logging_macros.h"
#include <oboe/Oboe.h>
#include <regex>
#include <string>
#include <sys/stat.h>
#include "FileDataSource.h"
#include "FFMpegExtractor.h"
#include <regex>

constexpr int kMaxCompressionRatio { 12 };

FileDataSource* FileDataSource::newFromCompressedFile(
        const char *filename,
        const AudioProperties targetProperties) {
    string filenameStr(filename);

    FILE* fl = fopen(filenameStr.c_str(), "r");
    if (!fl) {
        LOGE("Failed to open asset %s", filenameStr.c_str());
        fclose(fl);
        return nullptr;
    }
    fclose(fl);

    off_t assetSize = getSizeOfFile(filenameStr.c_str());

    // Allocate memory to store the decompressed audio. We don't know the exact
    // size of the decoded data until after decoding so we make an assumption about the
    // maximum compression ratio and the decoded sample format (float for FFmpeg, int16 for NDK).

    auto ffmpegExtractor = FFMpegExtractor(filenameStr, targetProperties);

    // TODO: Review, probably too much memory is allocated to buffer
    long maximumDataSizeInBytes = assetSize * AUDIO_CHANNEL_STEREO * (sizeof(float_t) / MIN_NUMBER_OF_BYTES_PER_SAMPLE);

    if (!strEndedWith(filenameStr, ".wav")) {
        maximumDataSizeInBytes = kMaxCompressionRatio * maximumDataSizeInBytes;
    }

    auto decodedData = new uint8_t[maximumDataSizeInBytes];

    int64_t bytesDecoded = ffmpegExtractor.decode(decodedData);

    auto numSamples = bytesDecoded / sizeof(float);

    // Now we know the exact number of samples we can create a float array to hold the audio data
    auto outputBuffer = make_unique<float[]>(numSamples);
    memcpy(outputBuffer.get(), decodedData, (size_t)bytesDecoded);

    delete[] decodedData;

    return new FileDataSource(move(outputBuffer),
                              numSamples,
                              targetProperties);
}

unique_ptr<buffer_data> FileDataSource::readData(size_t numSamples) {
    int channelCount = mProperties.channelCount;
    size_t samplesToHandle = 0;
    if (currentPtr + numSamples * channelCount > mBufferSize) {
        samplesToHandle = (mBufferSize - currentPtr) / channelCount;
    } else {
        samplesToHandle = numSamples;
    }
    auto selectedSamples = new float [samplesToHandle];
    for(int i = 0; i < samplesToHandle; i++) {
        float totalSample = 0;
        for (int j = 1; j <= channelCount; j++) {
            totalSample += mBuffer[++currentPtr];
        }
        selectedSamples[i] = totalSample / (float) channelCount;
    }
    buffer_data buff = {
            .ptr = selectedSamples,
            .numSamples = samplesToHandle
    };
    return make_unique<buffer_data>(buff);
}
