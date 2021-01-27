//
// Created by asalehin on 9/9/20.
//

#include "MixingEngine.h"
#include "../jvm_env.h"
#include <utility>

MixingEngine::MixingEngine(SourceMapStore* sourceMapStore) : mSourceMapStore(sourceMapStore) {
    mMixingIO.setStopPlaybackCallback([&] () {
        setStopPlayback();
    });
}

void MixingEngine::addFile(string filePath) {
    auto it = mSourceMapStore->sourceMap.find(filePath);
    if (it != mSourceMapStore->sourceMap.end()) {
        filePath.erase();
        return;
    }
    shared_ptr<FileDataSource> source = mMixingIO.readFile(filePath);
    mSourceMapStore->sourceMap.insert(pair<string, shared_ptr<FileDataSource>>(filePath, move(source)));
    filePath.erase();
}

unique_ptr<buffer_data> MixingEngine::readSamples(string filePath, size_t countPoints) {
    auto it = mSourceMapStore->sourceMap.find(filePath);
    filePath.erase();
    if (it == mSourceMapStore->sourceMap.end()) {
        buffer_data emptyData {
                .ptr = nullptr,
                .countPoints = 0
        };
        return make_unique<buffer_data>(emptyData);
    }
    return it->second->readData(countPoints);
}

void MixingEngine::deleteFile(string filePath) {
    auto it = mSourceMapStore->sourceMap.find(filePath);
    if (it != mSourceMapStore->sourceMap.end()) {
        it->second.reset();
        mSourceMapStore->sourceMap.erase(filePath);
    }
    filePath.erase();
}

int64_t MixingEngine::getAudioFileTotalSamples(string filePath) {
    auto it = mSourceMapStore->sourceMap.find(filePath);
    if (it == mSourceMapStore->sourceMap.end()) {
        return 0;
    }
    if (!it->second) {
        return 0;
    }
    filePath.erase();
    return it->second->getSampleSize();
}

bool MixingEngine::startPlayback() {
    lock_guard<mutex> lock(playbackStreamMtx);
    LOGD(TAG, "startPlayback(): ");

    return startPlaybackCallable();
}

void MixingEngine::pausePlayback() {
    lock_guard<mutex> lock(playbackStreamMtx);
    LOGD(TAG, "pausePlayback(): ");
    playbackStream.stopStream();
}

bool MixingEngine::startPlaybackCallable() {
    if (!playbackStream.mStream) {
        if (playbackStream.openStream() != oboe::Result::OK) {
            return false;
        }
    }

    mMixingIO.syncPlayHeads();
    mMixingIO.setPlaying(true);
    return playbackStream.startStream() == oboe::Result::OK;
}

void MixingEngine::stopPlaybackCallable() {
    closePlaybackStream();
    mMixingIO.setPlaying(false);
}

void MixingEngine::closePlaybackStream() {
    if (playbackStream.mStream) {
        if (playbackStream.mStream->getState() != oboe::StreamState::Closed) {
            playbackStream.stopStream();
        } else {
            playbackStream.resetStream();
        }
    }
}

void MixingEngine::addSourcesToPlayer(string* strArr, int count) {
    mMixingIO.clearPlayerSources();

    map<string, shared_ptr<DataSource>> playMap;

    for (int i = 0; i < count; i++) {
        auto it = mSourceMapStore->sourceMap.find(strArr[i]);
        if (it != mSourceMapStore->sourceMap.end()) {
            playMap.insert(pair<string, shared_ptr<FileDataSource>>(it->first, it->second));
        }
    }

    for (int i = 0; i < count; i++) {
        strArr[i].erase();
    }

    mMixingIO.addSourceMap(playMap);
}

void MixingEngine::setStopPlayback() {
    call_in_attached_thread([&](auto env) {
        if (kotlinMixingMethodIdsPtr) {
            env->CallStaticVoidMethod(kotlinMixingMethodIdsPtr->mixingScreenVM, kotlinMixingMethodIdsPtr->mixingScreenVMSetStopPlayback);
        }
    });
}

void MixingEngine::clearPlayerSources() {
    mMixingIO.clearPlayerSources();
}

int MixingEngine::getTotalSampleFrames() {
    return mMixingIO.getTotalSampleFrames();
}

int MixingEngine::getCurrentPlaybackProgress() {
    return mMixingIO.getCurrentPlaybackProgress();
}

void MixingEngine::setPlayerHead(int playHead) {
    mMixingIO.setPlayHead(playHead);
}

void MixingEngine::setSourcePlayHead(string filePath, int playHead) {
    auto it = mSourceMapStore->sourceMap.find(filePath);
    filePath.erase();
    if (it != mSourceMapStore->sourceMap.end()) {
        it->second->setPlayHead(playHead);
    }
}
