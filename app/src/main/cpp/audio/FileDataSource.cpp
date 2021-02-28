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
#include <vector>
#include <regex>
#include <string>
#include <sys/stat.h>
#include "FileDataSource.h"
#include "FFMpegExtractor.h"
#include <regex>

constexpr int kMaxCompressionRatio { 12 };

FileDataSource::FileDataSource (
        unique_ptr<float[]> data,
        size_t size,
        const AudioProperties properties) :
        mBuffer(move(data)), mBufferSize(size), mProperties(properties) {

    calculateProperties();
}

void FileDataSource::calculateProperties() {
    auto data = getData();
    auto bufferSize = getSize();

    for (int i = 0; i < bufferSize; i++) {
        if (abs(data[i]) > mMaxAbsSampleValue) {
            mMaxAbsSampleValue = abs(data[i]);
        }

        if (data[i] < mMinSampleValue) {
            mMinSampleValue = mBuffer[i];
        }

        if (data[i] > mMaxSampleValue) {
            mMaxSampleValue = data[i];
        }
    }
}

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

    long initialSize = assetSize * sizeof(float);

    if (strEndedWith(filenameStr, ".mp3")) {
        initialSize = initialSize * kMaxCompressionRatio;
    }

    vector<uint8_t> decodedData;
    decodedData.resize(initialSize);

    int64_t bytesDecoded = ffmpegExtractor.decode(decodedData);

    if (bytesDecoded <= 0) {
        return nullptr;
    }

    auto numSamples = bytesDecoded / sizeof(float);

    // Now we know the exact number of samples we can create a float array to hold the audio data
    auto outputBuffer = make_unique<float[]>(numSamples);
    memcpy(outputBuffer.get(), decodedData.data(), (size_t)bytesDecoded);

    vector <uint8_t> v;
    decodedData.swap(v);

    return new FileDataSource(move(outputBuffer),
                              numSamples,
                              move(targetProperties));
}

unique_ptr<buffer_data> FileDataSource::readData(size_t countPoints) {

    float * dataPtr = mBuffer.get();
    int channelCount = mProperties.channelCount;

    size_t samplesToHandle;

    if (countPoints * channelCount > mBufferSize) {
        samplesToHandle = floor((float) mBufferSize / (float) channelCount);
    } else {
        samplesToHandle = countPoints;
    }

    int ptsDistance = (int) ((float) mBufferSize / (float) samplesToHandle);

    auto selectedSamples = new float [samplesToHandle];
    for (int i = 0; i < samplesToHandle; i++) {
        float maxValue = FLT_MIN;
        for (int j = i * ptsDistance; j < (i + 1) * ptsDistance; j += channelCount) {
            float sample = 0.0;

            float maxAmpValue = 0.0;

            for (int k = j; k < j + channelCount; k++) {
                if (abs(dataPtr[k]) > maxValue) {
                    maxAmpValue = dataPtr[k];
                }

                sample += abs(dataPtr[k]);
            }

            float avgSample = sample / channelCount;

            if (avgSample > abs(maxValue)) {

                maxValue = maxAmpValue < 0 ? 0 - avgSample : avgSample;
            }
        }

        selectedSamples[i] = maxValue;
    }

    buffer_data buff = {
            .ptr = selectedSamples,
            .countPoints = samplesToHandle
    };
    return make_unique<buffer_data>(buff);
}

const float* FileDataSource::getMainBufferData() {
    return mBuffer.get();
}

int64_t FileDataSource::getMainBufferSize() {
    return mBufferSize;
}

const float* FileDataSource::getData() const {
    if (mBackupBuffer) {
        return mBackupBuffer.get();
    }
    return mBuffer.get();
}

float FileDataSource::getAbsMaxSampleValue() const {
    return mMaxAbsSampleValue;
}

float FileDataSource::getMaxSampleValue() const {
    return mMaxSampleValue;
}

float FileDataSource::getMinSampleValue() const {
    return mMinSampleValue;
}

void FileDataSource::setPlayHead(int64_t playHead) {
    mPlayHead = playHead;
}

int64_t FileDataSource::getPlayHead() {
    return mPlayHead;
}

void FileDataSource::setSelectionStart(int64_t selectionStart) {
    mSelectionStart = selectionStart;
}

void FileDataSource::setSelectionEnd(int64_t selectionEnd) {
    mSelectionEnd = selectionEnd;
}

void FileDataSource::resetSelectionStart() {
    mSelectionStart = INT64_MIN;
}

void FileDataSource::resetSelectionEnd() {
    mSelectionEnd = INT64_MIN;
}

int64_t FileDataSource::getSelectionStart() {
    return mSelectionStart;
}

int64_t FileDataSource::getSelectionEnd() {
    return mSelectionEnd;
}

void FileDataSource::setBackupBufferData(float* &&data, int64_t numSamples) {
    mBackupBuffer = unique_ptr<float[]>(move(data));
    mBackupBufferSize = numSamples;
}

int64_t FileDataSource::getSize() const {
    if (mBackupBuffer) {
        return mBackupBufferSize;
    }
    return mBufferSize;
}

int64_t FileDataSource::getSampleSize() {
    if (mBackupBuffer) {
        return mBackupBufferSize / mProperties.channelCount;
    }

    return mBufferSize / mProperties.channelCount;
}

void FileDataSource::resetBackupBufferData() {
    if (mBackupBuffer) {
        mBackupBuffer = unique_ptr<float[]>{nullptr};
    }
}

void FileDataSource::applyBackupBufferData() {
    if (mBackupBuffer) {
        mBuffer.swap(mBackupBuffer);
    }
    resetBackupBufferData();
    calculateProperties();
}

int64_t FileDataSource::shiftBySamples(int64_t position, int64_t numSamples) {

    auto channelCount = mProperties.channelCount;

    int64_t numSamplesWithChannelCount = numSamples * channelCount;
    int64_t positionWithChannelCount = position * channelCount;

    int64_t newBufferSize = mBufferSize + numSamplesWithChannelCount;

    float* oldBufferData = mBuffer.get();
    float* newBuffer = new float[newBufferSize];

    int64_t fillStartPosition = positionWithChannelCount;

    if (positionWithChannelCount - channelCount + 1 > 0) {
        fillStartPosition = positionWithChannelCount - channelCount + 1;
    }

    if (fillStartPosition >= 0) {
        copy(oldBufferData, oldBufferData + fillStartPosition, newBuffer);
    }

    int64_t fillEndPosition = fillStartPosition + numSamplesWithChannelCount;

    fill(newBuffer + fillStartPosition, newBuffer + fillEndPosition, 0);

    copy(oldBufferData + fillStartPosition, oldBufferData + mBufferSize, newBuffer + fillEndPosition);

    mBuffer.reset(move(newBuffer));
    mBufferSize = newBufferSize;

    return position + numSamples;
}

int64_t FileDataSource::cutToClipboard(int64_t startPosition, int64_t endPosition, vector<float>& clipboard) {
    auto channelCount = mProperties.channelCount;

    if (endPosition > getSampleSize() - 1) {
        endPosition = getSampleSize() - 1;
    }

    auto startIndexWithChannels = startPosition * channelCount;
    auto endIndexWithChannels = (endPosition + 1) * channelCount;

    if (endIndexWithChannels > mBufferSize) {
        endIndexWithChannels = mBufferSize;
    }

    auto numElements = (endPosition - startPosition + 1) * channelCount;

    float* oldBufferData = mBuffer.get();
    float* newBuffer = new float[mBufferSize - numElements];

    copy(oldBufferData + startIndexWithChannels, oldBufferData + endIndexWithChannels, clipboard.begin());

    copy(oldBufferData, oldBufferData + startIndexWithChannels, newBuffer);
    copy(oldBufferData + endIndexWithChannels, oldBufferData + mBufferSize, newBuffer + startIndexWithChannels);

    mBuffer.reset(move(newBuffer));
    mBufferSize = mBufferSize - numElements;

    if (mPlayHead >= startPosition && mPlayHead <= endPosition) {
        if (startPosition > 0) {
            mPlayHead = startPosition - 1;
        } else {
            mPlayHead = startPosition;
        }
    } else if (mPlayHead > endPosition) {
       auto newPlayHead = mPlayHead - (endPosition - startPosition + 1);
       if (newPlayHead < 0) {
           newPlayHead = 0;
       }

       mPlayHead = newPlayHead;
    }

    return mPlayHead;
}

void FileDataSource::copyToClipboard(int64_t startPosition, int64_t endPosition, vector<float>& clipboard) {
    auto channelCount = mProperties.channelCount;

    if (endPosition > getSampleSize() - 1) {
        endPosition = getSampleSize() - 1;
    }

    auto startIndexWithChannels = startPosition * channelCount;
    auto endIndexWithChannels = (endPosition + 1) * channelCount;

    if (endIndexWithChannels > mBufferSize) {
        endIndexWithChannels = mBufferSize;
    }

    float* oldBufferData = mBuffer.get();

    copy(oldBufferData + startIndexWithChannels, oldBufferData + endIndexWithChannels, clipboard.begin());
}

void FileDataSource::muteAndCopyToClipboard(int64_t startPosition, int64_t endPosition, vector<float>& clipboard) {
    auto channelCount = mProperties.channelCount;

    if (endPosition > getSampleSize() - 1) {
        endPosition = getSampleSize() - 1;
    }

    auto startIndexWithChannels = startPosition * channelCount;
    auto endIndexWithChannels = (endPosition + 1) * channelCount;

    float* oldBufferData = mBuffer.get();

    copy(oldBufferData + startIndexWithChannels, oldBufferData + endIndexWithChannels, clipboard.begin());

    fill(oldBufferData + startIndexWithChannels, oldBufferData + endIndexWithChannels, 0);
}

void FileDataSource::pasteFromClipboard(int64_t position, vector<float>& clipboard) {
    auto channelCount = mProperties.channelCount;

    if (position > getSampleSize() - 1) {
        position = getSampleSize() - 1;
    }

    auto pasteIndex = position * channelCount;

    if (position == getSampleSize() - 1) {
        pasteIndex = (position + 1) * channelCount;
    }

    float* oldBufferData = mBuffer.get();
    float* newBuffer = new float[mBufferSize + clipboard.size()];

    copy(oldBufferData, oldBufferData + pasteIndex, newBuffer);
    copy(clipboard.begin(), clipboard.end(), newBuffer + pasteIndex);

    if (pasteIndex < mBufferSize) {
        copy(oldBufferData + pasteIndex, oldBufferData + mBufferSize,
                        newBuffer + pasteIndex + clipboard.size());
    }

    mBuffer.reset(move(newBuffer));
    mBufferSize += clipboard.size();
}
