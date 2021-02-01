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
#include "thread"
#include "vector"

mutex addedMaxMtx;

void Player::addSource(string key, shared_ptr<DataSource> source) {
    mSourceMap.insert(pair<string, shared_ptr<DataSource>>(key, source));
}

bool Player::checkPlayerSources(map<string, shared_ptr<DataSource>> playMap) {
    if (playMap.size() != mSourceMap.size()) return false;

    bool isSame = true;

    for (auto i = playMap.begin(), j = mSourceMap.begin();
        i != playMap.end(); i++, j++) {

        isSame = isSame && (strcmp(i->first.c_str(), j->first.c_str()) == 0);
    }

    return isSame;
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
        if (mReadFrameIndex + numFrames >= maxTotalSourceFrames) {
            framesToRenderFromData = maxTotalSourceFrames - mReadFrameIndex;
            mIsPlaying = false;
            if (mStopPlaybackCallback) {
                mStopPlaybackCallback();
            }
        }

        if (framesToRenderFromData <= 0) return;

        for (int i = 0; i < framesToRenderFromData; ++i) {

            for (int j = 0; j < properties.channelCount; ++j) {

                float audioFrame = 0.0;

                for (auto it = mSourceMap.begin(); it != mSourceMap.end(); it++) {

                    if (!it->second) {
                        continue;
                    }

                    const float *data = it->second->getData();

                    if (data && (mReadFrameIndex * properties.channelCount + j) < it->second->getSize()) {
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
                break;
            }
        }

        if (mSourceMap.size() == 1 && mSourceMap.begin()->second) {
            mSourceMap.begin()->second->setPlayHead(mReadFrameIndex);
        }
    }
}

int64_t Player::getMaxTotalSourceFrames() {
    int64_t maxTotalSourceFrames = INT64_MIN;

    for (auto it = mSourceMap.begin(); it != mSourceMap.end(); it++) {
        if (!it->second) {
            continue;
        }
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
        if (!it->second) {
            continue;
        }
        if (it->second->getAbsMaxSampleValue() > allMaxValue) {
            allMaxValue = it->second->getAbsMaxSampleValue();
        }
    }
    return allMaxValue;
}

void Player::setPlayHead(int32_t playHead) {
    int64_t maxTotalSourceFrames = getMaxTotalSourceFrames();
    if (playHead < maxTotalSourceFrames || playHead == 0) {
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

    if (mSourceMap.size() == 0) return;

    // All the sources should have the same properties
    const AudioProperties properties = mSourceMap.begin()->second->getProperties();

    // 30 seconds of audio
    int64_t samplesToHandlePerThread = properties.sampleRate * properties.channelCount * 30;

    // Get maximum width of all loaded sources
    int64_t maxSize = INT64_MIN;
    for (auto it = mSourceMap.begin(); it != mSourceMap.end(); it++) {
        if (it->second->getSize() > maxSize) {
            maxSize = it->second->getSize();
        }
    }

    // Number of threads, while each will handle 30 seconds audio at max
    auto numThreads = (int) ceil((float) maxSize / (float) samplesToHandlePerThread);

    vector<thread> workers;

    for (int t = 0; t < numThreads; t++) {
        auto start = t * samplesToHandlePerThread;
        auto end = (t + 1) * samplesToHandlePerThread - 1;

        if (end >= maxSize) {
            end = maxSize - 1;
        }

        // Lambda to calculate max from segments
        auto addedMaxCaller = [start,
                               end,
                               &mSourceMap = mSourceMap,
                               &addedMaxSampleValue = addedMaxSampleValue]() {
            float localMaxValue = FLT_MIN;

            for (int i = start; i <= end; i++) {
                float totalSampleValue = 0.0;

                for (auto it = mSourceMap.begin(); it != mSourceMap.end(); it++) {
                    if (it->second && i < it->second->getSize()) {
                        totalSampleValue += it->second->getData()[i];
                    }
                }

                if (abs(totalSampleValue) > localMaxValue) {
                    localMaxValue = abs(totalSampleValue);
                }

            }

            // Only allow a single thread to perform this operation
            addedMaxMtx.lock();
            if (localMaxValue > addedMaxSampleValue) {
                addedMaxSampleValue = localMaxValue;
            }
            addedMaxMtx.unlock();
        };

        // Save worker threads
        workers.push_back(thread (
                        addedMaxCaller
                        ));
    }

    // Wait for all worker to finish
    for (auto it = workers.begin(); it != workers.end(); ++it) {
        it->join();
    }
}
