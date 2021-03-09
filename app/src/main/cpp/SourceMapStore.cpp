//
// Created by Ashfaq Salehin on 22/1/2021 AD.
//

#include "SourceMapStore.h"

shared_ptr<SourceMapStore> SourceMapStore::mInstance {nullptr};

SourceMapStore::SourceMapStore() {}

shared_ptr<SourceMapStore> SourceMapStore::getInstance() {
    if (!mInstance) {
        mInstance = shared_ptr<SourceMapStore> {
            new SourceMapStore()
        };
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

void SourceMapStore::reset() {
    mInstance = shared_ptr<SourceMapStore> {nullptr};
}
