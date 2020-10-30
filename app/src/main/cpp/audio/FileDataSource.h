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
    int64_t getSize() const override { return mBufferSize; }
    AudioProperties getProperties() const override { return mProperties; }
    const float* getData() const override { return mBuffer.get(); }

    static FileDataSource* newFromCompressedFile(
            const char *filename,
            const AudioProperties targetProperties);

    unique_ptr<buffer_data> readAllData();

    static int64_t getTotalSamples(const char *filename, const AudioProperties targetProperties);

private:

    FileDataSource(unique_ptr<float[]> data, size_t size,
                   const AudioProperties properties)
            : mBuffer(move(data))
            , mBufferSize(size)
            , mProperties(properties) {
    }

    const unique_ptr<float[]> mBuffer;
    const int64_t mBufferSize;
    const AudioProperties mProperties;
};
#endif //RHYTHMGAME_AASSETDATASOURCE_H
