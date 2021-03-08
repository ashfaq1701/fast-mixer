//
// Created by Ashfaq Salehin on 26/2/2021 AD.
//

#include "BufferedDataSource.h"

BufferedDataSource::BufferedDataSource(
        bufferDataType data,
        size_t size,
        const AudioProperties properties) : FileDataSource(move(data), size, properties) {}

BufferedDataSource* BufferedDataSource::newFromClipboard(vector<float>& clipboard, const AudioProperties targetProperties) {
    auto outputBuffer = bufferDataType {
        new float[clipboard.size()],
        FileDataSource::deleter
    };

    copy(clipboard.begin(), clipboard.end(), outputBuffer.get());

    return new BufferedDataSource(move(outputBuffer),
                              clipboard.size(),
                              move(targetProperties));
}
