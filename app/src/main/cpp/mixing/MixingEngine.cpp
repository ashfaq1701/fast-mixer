//
// Created by asalehin on 9/9/20.
//

#include "MixingEngine.h"
#include "mixing_jvm_env.h"
#include <utility>

MixingEngine::MixingEngine() {
    mMixingIO.setStopPlaybackCallback([&] () {
        setStopPlayback();
    });
}

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
    shared_ptr<FileDataSource> source = mMixingIO.readFile(filePath);
    sourceMap.insert(pair<string, shared_ptr<FileDataSource>>(filePath, move(source)));
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

    for (int i = 0; i < count; i++) {
        auto it = sourceMap.find(strArr[i]);
        if (it != sourceMap.end()) {
            mMixingIO.addSource(it->first, it->second);
        }
    }

    for (int i = 0; i < count; i++) {
        strArr[i].erase();
    }
}

void MixingEngine::setStopPlayback() {
    call_in_attached_thread([&](auto env) {
        if (kotlinMethodIdsPtr) {
            env->CallStaticVoidMethod(kotlinMethodIdsPtr->mixingScreenVM, kotlinMethodIdsPtr->mixingScreenVMSetStopPlayback);
        }
    });
}

void MixingEngine::clearPlayerSources() {
    mMixingIO.clearPlayerSources();
}
