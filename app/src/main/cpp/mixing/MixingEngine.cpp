//
// Created by asalehin on 9/9/20.
//

#include "MixingEngine.h"

#include <utility>

void MixingEngine::addFile(string filePath) {
    shared_ptr<FileDataSource> source = mixingIO.readFile(std::move(filePath));
    sourceList.push_back(source);
}

unique_ptr<buffer_data> MixingEngine::readSamples(int index, size_t numSamples) {
    if (index >= sourceList.size()) {
        buffer_data emptyData {
            .ptr = nullptr,
            .numSamples = 0
        };
        return make_unique<buffer_data>(emptyData);
    }
    shared_ptr<FileDataSource> dataSource = sourceList.at(index);
    return dataSource->readData(numSamples);
}

void MixingEngine::deleteFile(int idx) {
    if (idx < sourceList.size()) {
        shared_ptr<FileDataSource> dataSource = sourceList.at(idx);
        dataSource.reset();
        sourceList.erase(sourceList.begin() + idx);
    }
}

