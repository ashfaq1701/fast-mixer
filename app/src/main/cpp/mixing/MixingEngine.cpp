//
// Created by asalehin on 9/9/20.
//

#include "MixingEngine.h"

#include <utility>

void MixingEngine::addFile(string filePath, string uuid) {
    shared_ptr<FileDataSource> source = mixingIO.readFile(std::move(filePath));
    sourceMap.insert(pair<string, shared_ptr<FileDataSource>>(uuid, source));
}

unique_ptr<buffer_data> MixingEngine::readSamples(string uuid, size_t numSamples) {
    auto it = sourceMap.find(uuid);
    if (it == sourceMap.end()) {
        buffer_data emptyData {
            .ptr = nullptr,
            .numSamples = 0
        };
        return make_unique<buffer_data>(emptyData);
    }
    shared_ptr<FileDataSource> dataSource = it->second;
    return dataSource->readData(numSamples);
}

void MixingEngine::deleteFile(string uuid) {
    auto it = sourceMap.find(uuid);
    if (it != sourceMap.end()) {
        shared_ptr<FileDataSource> dataSource = it->second;
        dataSource.reset();
        sourceMap.erase(uuid);
    }
}

