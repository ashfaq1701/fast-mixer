//
// Created by asalehin on 9/9/20.
//

#include "MixingEngine.h"

#include <utility>

void MixingEngine::addFile(string filePath) {
    shared_ptr<FileDataSource> source = mixingIO.readFile(std::move(filePath));
    sourceList.push_back(source);
}
