//
// Created by asalehin on 9/9/20.
//

#include "MixingEngine.h"

#include <utility>

MixingEngine::~MixingEngine() {
    auto it = sourceMap.begin();
    while (it != sourceMap.end()) {
        it->second.reset();
    }
    sourceMap.clear();
}

void MixingEngine::addFile(string filePath) {
    auto it = sourceMap.find(filePath);
    if (it != sourceMap.end()) {
        filePath.erase();
        return;
    }
    shared_ptr<FileDataSource> source = mixingIO.readFile(filePath);
    sourceMap.insert(pair<string, shared_ptr<FileDataSource>>(filePath, source));
    filePath.erase();
}

unique_ptr<buffer_data> MixingEngine::readSamples(string filePath, size_t countPoints) {
    auto it = sourceMap.find(filePath);
    filePath.erase();
    if (it == sourceMap.end()) {
        buffer_data emptyData {
                .ptr = nullptr,
                .countPoints = 0
        };
        return make_unique<buffer_data>(emptyData);
    }
    return it->second->readData(countPoints);
}

void MixingEngine::deleteFile(string filePath) {
    auto it = sourceMap.find(filePath);
    if (it != sourceMap.end()) {
        it->second.reset();
        sourceMap.erase(filePath);
    }
    filePath.erase();
}

int64_t MixingEngine::getAudioFileTotalSamples(string filePath) {
    auto it = sourceMap.find(filePath);
    if (it == sourceMap.end()) {
        return 0;
    }
    if (!it->second) {
        return 0;
    }
    filePath.erase();
    return it->second->getSampleSize();
}

