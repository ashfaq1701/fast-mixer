//
// Created by Ashfaq Salehin on 16/3/2021 AD.
//

#include "SourceStore.h"
#include "vector"
#include "thread"
#include <mutex>

mutex addedMaxMtx;

void SourceStore::updateAddedMax() {
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

float SourceStore::getMaxValueAcrossSources() {
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

int64_t SourceStore::getMaxTotalSourceFrames() {
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

int64_t SourceStore::getTotalSampleFrames() {
    return getMaxTotalSourceFrames();
}
