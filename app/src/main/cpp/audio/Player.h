/*
 * Copyright 2018 The Android Open Source Project
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

#ifndef FAST_MIXER_SOUNDRECORDING_H
#define FAST_MIXER_SOUNDRECORDING_H

#include <cstdint>
#include <array>
#include "map"

#include <chrono>
#include <memory>
#include <atomic>

#include <android/asset_manager.h>

#include "IRenderableAudio.h"
#include "DataSource.h"

using namespace std;

class Player : public IRenderableAudio{

public:
    /**
     * Construct a new Player from the given DataSource. Players can share the same data source.
     * For example, you could play two identical sounds concurrently by creating 2 Players with the
     * same data source.
     *
     * @param source
     */

    Player(map<string, shared_ptr<DataSource>> sourceMap, function<void(void)> stopPlaybackCallback)
        : mSourceMap{ sourceMap } {
        mStopPlaybackCallback = stopPlaybackCallback;

        if (mSourceMap.size() > 1) {
            updateAddedMax();
        }
    };

    Player()
        : Player { map<string, shared_ptr<DataSource>>(), nullptr } {};

    void addSource(string key, shared_ptr<DataSource> source);
    bool checkPlayerSources(map<string, shared_ptr<DataSource>> playMap);
    void addSourceMap(map<string, shared_ptr<DataSource>> playMap);
    void setPlaybackCallback(function<void(void)> stopPlaybackCallback);
    void renderAudio(float *targetData, int32_t numFrames);
    void resetPlayHead() { mReadFrameIndex = 0; };
    int32_t getPlayHead() { return mReadFrameIndex; }
    int64_t getMaxTotalSourceFrames();

    void setPlayHead(int32_t playHead);
    void setPlaying(bool isPlaying) { mIsPlaying = isPlaying; };
    int64_t getTotalSampleFrames();

    void clearSources();
    void syncPlayHeads();

    void setPlayerBoundStart(int64_t boundStart);
    void setPlayerBoundEnd(int64_t boundEnd);
    void resetPlayerBoundStart();
    void resetPlayerBoundEnd();

private:
    int32_t mReadFrameIndex = 0;
    float addedMaxSampleValue = FLT_MIN;
    atomic<bool> mIsPlaying { false };
    function<void(void)> mStopPlaybackCallback = nullptr;
    map<string, shared_ptr<DataSource>> mSourceMap;

    int64_t mSourceBoundStart = -1;
    int64_t mSourceBoundEnd = -1;

    void updateAddedMax();
    void renderSilence(float*, int32_t);
    float getMaxValueAcrossSources();
};

#endif //RHYTHMGAME_SOUNDRECORDING_H
