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
    std::string filenameStr(filename);
    //filenameStr = std::regex_replace(filenameStr, std::regex("recording.wav"), "example.mp3");

    FILE* fl = fopen(filenameStr.c_str(), "r");
    if (!fl) {
        LOGE("Failed to open asset %s", filenameStr.c_str());
        return nullptr;
    }

    off_t assetSize = getFileSize(filenameStr.c_str());
    LOGD("Opened %s, size %ld", filenameStr.c_str(), assetSize);

    // Allocate memory to store the decompressed audio. We don't know the exact
    // size of the decoded data until after decoding so we make an assumption about the
    // maximum compression ratio and the decoded sample format (float for FFmpeg, int16 for NDK).

    auto ffmpegExtractor = FFMpegExtractor(filenameStr, targetProperties);
    ffmpegExtractor.decode();

    int numBytesInSample = 0;
    switch (ffmpegExtractor.mAudioFormat) {
        case AV_SAMPLE_FMT_U8:
        case AV_SAMPLE_FMT_U8P:
            numBytesInSample = 1;
            break;
        case AV_SAMPLE_FMT_S16:
        case AV_SAMPLE_FMT_S16P:
            numBytesInSample = 2;
            break;
        case AV_SAMPLE_FMT_S32:
        case AV_SAMPLE_FMT_S32P:
        case AV_SAMPLE_FMT_FLT:
        case AV_SAMPLE_FMT_FLTP:
            numBytesInSample = 4;
            break;
        case AV_SAMPLE_FMT_DBL:
        case AV_SAMPLE_FMT_DBLP:
        case AV_SAMPLE_FMT_S64:
        case AV_SAMPLE_FMT_S64P:
            numBytesInSample = 8;
            break;
        default:
            numBytesInSample = 8;
    }

    long maximumDataSizeInBytes = 0;
    maximumDataSizeInBytes = assetSize * ffmpegExtractor.mChannelCount * (sizeof(float_t) / numBytesInSample);
    if (!strEndedWith(filenameStr, ".wav")) {
        maximumDataSizeInBytes = kMaxCompressionRatio * maximumDataSizeInBytes;
    }

    LOGD("Asset Size: %ld", assetSize);
    LOGD("Maximum Data Size: %ld", maximumDataSizeInBytes);

    auto decodedData = new uint8_t[maximumDataSizeInBytes];

    int64_t bytesDecoded = ffmpegExtractor.readData(decodedData);
    auto numSamples = bytesDecoded / sizeof(float);

    // Now we know the exact number of samples we can create a float array to hold the audio data
    auto outputBuffer = std::make_unique<float[]>(numSamples);
    memcpy(outputBuffer.get(), decodedData, (size_t)bytesDecoded);

    delete[] decodedData;
    fclose(fl);

    return new FileDataSource(std::move(outputBuffer),
                              numSamples,
                              targetProperties);
}

long FileDataSource::getFileSize(const char *fileName) {
    return getSizeOfFile(fileName);
}
