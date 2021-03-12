//
// Created by Ashfaq Salehin on 26/2/2021 AD.
//

#include "BufferedDataSource.h"

BufferedDataSource::BufferedDataSource(
        unique_ptr<float[]> data,
        size_t size,
        const AudioProperties properties) : FileDataSource(move(data), size, properties) {}

BufferedDataSource* BufferedDataSource::newFromClipboard(vector<float>& clipboard, const AudioProperties targetProperties) {
    auto outputBuffer = make_unique<float[]>(clipboard.size());

    copy(clipboard.begin(), clipboard.end(), outputBuffer.get());

    return new BufferedDataSource(move(outputBuffer),
                              clipboard.size(),
                              move(targetProperties));
}
