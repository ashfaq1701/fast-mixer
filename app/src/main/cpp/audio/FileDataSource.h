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

#ifndef FAST_MIXER_FILEDATASOURCE_H
#define FAST_MIXER_FILEDATASOURCE_H

#include <android/asset_manager.h>
#include "../Constants.h"
#include "DataSource.h"
#include "../utils/Utils.h"

using namespace std;

#define AUDIO_CHANNEL_STEREO 2
#define MIN_NUMBER_OF_BYTES_PER_SAMPLE 1

class FileDataSource : public DataSource {

public:
    int64_t getMainBufferSize();
    int64_t getSize() const override;

    AudioProperties getProperties() const override { return mProperties; }

    const float* getMainBufferData();
    const float* getData() const override;

    void setBackupBufferData(float* &&data, int64_t numSamples);

    int64_t getSampleSize();

    void setPlayHead(int64_t playHead);
    int64_t getPlayHead() override;

    float getAbsMaxSampleValue() const override;

    float getMaxSampleValue() const override;

    float getMinSampleValue() const override;

    void setSelectionStart(int64_t selectionStart);
    void setSelectionEnd(int64_t selectionEnd);

    void resetSelectionStart();
    void resetSelectionEnd();

    int64_t getSelectionStart() override;
    int64_t getSelectionEnd() override;

    static FileDataSource* newFromCompressedFile(
            const char *filename,
            const AudioProperties targetProperties);

    unique_ptr<buffer_data> readData(size_t numSamples);

    void applyBackupBufferData();
    void resetBackupBufferData();

private:

    FileDataSource(unique_ptr<float[]> data, size_t size,
                   const AudioProperties properties);

    void calculateProperties();

    unique_ptr<float[]> mBuffer;
    unique_ptr<float[]> mBackupBuffer {nullptr};

    const int64_t mBufferSize;
    int64_t mBackupBufferSize = 0;

    const AudioProperties mProperties;
    int64_t currentPtr = 0;
    int64_t mPlayHead = 0;

    float mMaxAbsSampleValue = FLT_MIN;
    float mMaxSampleValue = FLT_MIN;
    float mMinSampleValue = FLT_MAX;

    int64_t mSelectionStart = INT64_MIN;
    int64_t mSelectionEnd = INT64_MIN;
};
#endif //RHYTHMGAME_AASSETDATASOURCE_H
