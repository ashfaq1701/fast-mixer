//
// Created by asalehin on 9/9/20.
//

#include "MixingEngine.h"

#include <utility>

MixingEngine::~MixingEngine() {
    auto it = sourceMap.begin();
    while (it != sourceMap.end()) {
        unique_ptr<FileDataSource> source = move(it->second);
        source.reset(nullptr);
    }
    sourceMap.clear();
}

void MixingEngine::addFile(string filePath, string uuid) {
    auto it = sourceMap.find(uuid);
    if (it != sourceMap.end()) {
        return;
    }
    unique_ptr<FileDataSource> source = mixingIO.readFile(std::move(filePath));
    sourceMap.insert(pair<string, unique_ptr<FileDataSource>>(uuid, move(source)));
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
    return it->second->readData(numSamples);
}

void MixingEngine::deleteFile(string uuid) {
    auto it = sourceMap.find(uuid);
    if (it != sourceMap.end()) {
        unique_ptr<FileDataSource> dataSource = move(it->second);
        dataSource.reset(nullptr);
        sourceMap.erase(uuid);
    }
}

int64_t MixingEngine::getAudioFileTotalSamples(string uuid) {
    auto it = sourceMap.find(uuid);
    if (it == sourceMap.end()) {
        return 0;
    }
    return it->second->getSampleSize();
}

