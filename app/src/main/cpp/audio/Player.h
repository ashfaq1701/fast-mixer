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
#include "vector"

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

    Player(vector<shared_ptr<DataSource>> sourceList, function<void(void)> stopPlaybackCallback)
        : mSourceList{ sourceList } {
        mStopPlaybackCallback = stopPlaybackCallback;
    };

    Player(function<void(void)> stopPlaybackCallback)
        : Player { vector<shared_ptr<DataSource>>(), stopPlaybackCallback } {};

    void addSource(shared_ptr<DataSource> source);
    void renderAudio(float *targetData, int32_t numFrames);
    void resetPlayHead() { mReadFrameIndex = 0; };
    int32_t getPlayHead() { return mReadFrameIndex; }
    void setPlayHead(int32_t playHead);
    void setPlaying(bool isPlaying) { mIsPlaying = isPlaying; };
    void setLooping(bool isLooping) { mIsLooping = isLooping; };
    int64_t getTotalSampleFrames();

private:
    int32_t mReadFrameIndex = 0;
    atomic<bool> mIsPlaying { false };
    atomic<bool> mIsLooping { false };
    function<void(void)> mStopPlaybackCallback = nullptr;
    vector<shared_ptr<DataSource>> mSourceList;

    void renderSilence(float*, int32_t);
};

#endif //RHYTHMGAME_SOUNDRECORDING_H
