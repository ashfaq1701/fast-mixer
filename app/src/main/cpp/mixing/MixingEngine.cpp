//
// Created by asalehin on 9/9/20.
//

#include "MixingEngine.h"

#include <utility>

unique_ptr<buffer_data> MixingEngine::readAllSamples(string filePath) {
    FileDataSource* source = mixingIO.readFile(move(filePath));
    auto dataRead = source->readAllData();
    return dataRead;
}

int64_t MixingEngine::getAudioFileTotalSamples(string filePath) {
    return mixingIO.getTotalSamples(move(filePath));
}

