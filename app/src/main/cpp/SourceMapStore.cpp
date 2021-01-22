//
// Created by Ashfaq Salehin on 22/1/2021 AD.
//

#include "SourceMapStore.h"

SourceMapStore* SourceMapStore::mInstance = nullptr;

SourceMapStore::SourceMapStore() {}

SourceMapStore* SourceMapStore::getInstance() {
    if (mInstance == nullptr) {
        mInstance = new SourceMapStore();
    }

    return mInstance;
}

SourceMapStore::~SourceMapStore() {
    auto it = sourceMap.begin();
    while (it != sourceMap.end()) {
        it->second.reset();
    }
    sourceMap.clear();
}
