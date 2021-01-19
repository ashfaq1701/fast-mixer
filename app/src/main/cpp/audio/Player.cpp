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

#include "Player.h"
#include "../logging_macros.h"

void Player::addSource(string key, shared_ptr<DataSource> source) {
    mSourceMap.insert(pair<string, shared_ptr<DataSource>>(key, source));
}

void Player::addSourceMap(map<string, shared_ptr<DataSource>> playMap) {
    for (auto it = playMap.begin(); it != playMap.end(); it++) {
        mSourceMap.insert(pair<string, shared_ptr<DataSource>>(it->first, it->second));
    }

    if (mSourceMap.size() > 1) {
        updateAddedMax();
    } else {
        addedMaxSampleValue = FLT_MIN;
    }
}

void Player::setPlaybackCallback(function<void ()> stopPlaybackCallback) {
    mStopPlaybackCallback = stopPlaybackCallback;
}

void Player::renderAudio(float *targetData, int32_t numFrames) {
    if (mSourceMap.size() == 0) return;

    // All the sources should have the same properties
    const AudioProperties properties = mSourceMap.begin()->second->getProperties();

    if (mIsPlaying) {

        float allMaxValue = getMaxValueAcrossSources();
        int64_t maxTotalSourceFrames = getMaxTotalSourceFrames();

        int64_t framesToRenderFromData = numFrames;

        if (!mIsLooping && mReadFrameIndex + numFrames >= maxTotalSourceFrames) {
            framesToRenderFromData = maxTotalSourceFrames - mReadFrameIndex;
            mIsPlaying = false;
            if (mStopPlaybackCallback) {
                mStopPlaybackCallback();
            }
        }

        for (int i = 0; i < framesToRenderFromData; ++i) {

            for (int j = 0; j < properties.channelCount; ++j) {

                float audioFrame = 0.0;

                for (auto it = mSourceMap.begin(); it != mSourceMap.end(); it++) {

                    const float *data = it->second->getData();

                    if ((mReadFrameIndex * properties.channelCount + j) < it->second->getSize()) {
                        audioFrame += data[mReadFrameIndex * properties.channelCount + j];
                    }
                }

                float scaledAudioFrame = audioFrame;

                if (addedMaxSampleValue != 0 && addedMaxSampleValue != FLT_MIN) {
                    scaledAudioFrame = (scaledAudioFrame / addedMaxSampleValue) * allMaxValue;
                }

                targetData[i * properties.channelCount + j] = scaledAudioFrame;
            }

            if (++mReadFrameIndex >= maxTotalSourceFrames) {
                mReadFrameIndex = 0;
            }
        }

        if (framesToRenderFromData < numFrames) {
            // fill the rest of the buffer with silence
            renderSilence(&targetData[framesToRenderFromData], numFrames * properties.channelCount);
        }

        if (mSourceMap.size() == 1) {
            mSourceMap.begin()->second->setPlayHead(mReadFrameIndex);
        }

    } else {
        renderSilence(targetData, numFrames * properties.channelCount);
    }
}

void Player::renderSilence(float *start, int32_t numSamples){
    for (int i = 0; i < numSamples; ++i) {
        start[i] = 0;
    }
}

int64_t Player::getMaxTotalSourceFrames() {
    int64_t maxTotalSourceFrames = INT64_MIN;

    for (auto it = mSourceMap.begin(); it != mSourceMap.end(); it++) {
        int64_t totalSourceFrames = it->second->getSize() / it->second->getProperties().channelCount;

        if (totalSourceFrames > maxTotalSourceFrames) {
            maxTotalSourceFrames = totalSourceFrames;
        }
    }

    return maxTotalSourceFrames;
}

float Player::getMaxValueAcrossSources() {
    float allMaxValue = 0.0;
    for (auto it = mSourceMap.begin(); it != mSourceMap.end(); it++) {
        if (it->second->getMaxSampleValue() < allMaxValue) {
            allMaxValue = it->second->getMaxSampleValue();
        }
    }
    return allMaxValue;
}

void Player::setPlayHead(int32_t playHead) {
    int64_t maxTotalSourceFrames = getMaxTotalSourceFrames();
    if (playHead < maxTotalSourceFrames) {
        mReadFrameIndex = playHead;
    }
}

int64_t Player::getTotalSampleFrames() {
    return getMaxTotalSourceFrames();
}

void Player::clearSources() {
    mSourceMap.clear();
}

void Player::syncPlayHeads() {
    if (mSourceMap.size() == 1) {
        setPlayHead(mSourceMap.begin()->second->getPlayHead());
    }
}

void Player::updateAddedMax() {
    addedMaxSampleValue = FLT_MIN;

    int64_t maxSize = INT64_MIN;
    for (auto it = mSourceMap.begin(); it != mSourceMap.end(); it++) {
        if (it->second->getSize() > maxSize) {
            maxSize = it->second->getSize();
        }
    }

    for (int i = 0; i < maxSize; i++) {
        float totalSampleValue = 0.0;

        for (auto it = mSourceMap.begin(); it != mSourceMap.end(); it++) {
            if (i < it->second->getSize()) {
                totalSampleValue += it->second->getData()[i];
            }
        }

        if (abs(totalSampleValue) > addedMaxSampleValue) {
            addedMaxSampleValue = abs(totalSampleValue);
        }
    }
}
