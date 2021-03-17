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
#include "vector"

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

    auto firstSource = mSourceMap.begin()->second;

    auto selectionStart = firstSource->getSelectionStart();
    auto selectionEnd = firstSource->getSelectionEnd();

    // All the sources should have the same properties
    const AudioProperties properties = firstSource->getProperties();

    if (mIsPlaying) {

        if (mSourceMap.size() == 1) {
            if (selectionStart >= 0 && selectionEnd >= 0 && !(mReadFrameIndex >= selectionStart && mReadFrameIndex <= selectionEnd)) {
                mReadFrameIndex = selectionStart;
            }
        }

        if (mSourceBoundStart >= 0 && mSourceBoundEnd >= 0 && !(mReadFrameIndex >= mSourceBoundStart && mReadFrameIndex <= mSourceBoundEnd)) {
            mReadFrameIndex = mSourceBoundStart;
        }

        float allMaxValue = getMaxValueAcrossSources();

        int64_t sourceFramesUpperBound = getMaxTotalSourceFrames();
        if (mSourceMap.size() == 1) {
            if (selectionEnd >= 0) {
                sourceFramesUpperBound = selectionEnd + 1;
            }
        }

        if (mSourceBoundEnd >= 0) {
            sourceFramesUpperBound = mSourceBoundEnd + 1;
        }

        int64_t sourceFramesLowerBound = 0;
        if (mSourceBoundStart >= 0) {
            sourceFramesLowerBound = mSourceBoundStart;
        }

        int64_t framesToRenderFromData = numFrames;

        bool toStop = mReadFrameIndex + numFrames >= sourceFramesUpperBound;
        if (toStop) {
            framesToRenderFromData = sourceFramesUpperBound - mReadFrameIndex;
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

            if (++mReadFrameIndex >= sourceFramesUpperBound) {
                if (mSourceMap.size() == 1 && selectionStart >= 0) {
                    mReadFrameIndex = selectionStart;
                } else {
                    mReadFrameIndex = sourceFramesLowerBound;
                }
                break;
            }
        }

        if (mSourceMap.size() == 1 && mSourceMap.begin()->second) {
            mSourceMap.begin()->second->setPlayHead(mReadFrameIndex);
        }

        if (toStop) {
            mIsPlaying = false;
            if (mStopPlaybackCallback) {
                mStopPlaybackCallback();
            }
        }
    }
}

void Player::setPlayHead(int32_t playHead) {
    int64_t maxTotalSourceFrames = getMaxTotalSourceFrames();
    if (playHead < maxTotalSourceFrames || playHead == 0) {
        mReadFrameIndex = playHead;

        if (mSourceMap.size() == 1) {
            mSourceMap.begin()->second->setPlayHead(mReadFrameIndex);
        }
    }
}

void Player::clearSources() {
    mSourceMap.clear();
}

void Player::syncPlayHeads() {
    if (mSourceMap.size() == 1) {
        setPlayHead(mSourceMap.begin()->second->getPlayHead());
    }
}

void Player::setPlayerBoundStart(int64_t boundStart) {
    mSourceBoundStart = boundStart;
}

void Player::setPlayerBoundEnd(int64_t boundEnd) {
    mSourceBoundEnd = boundEnd;
}

void Player::resetPlayerBoundStart() {
    mSourceBoundStart = -1;
}

void Player::resetPlayerBoundEnd() {
    mSourceBoundEnd = -1;
}
