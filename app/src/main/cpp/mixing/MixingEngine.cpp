//
// Created by asalehin on 9/9/20.
//

#include "MixingEngine.h"
#include "../jvm_env.h"
#include "../synthesizers/GainAdjustment.h"
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

void MixingEngine::gainSourceByDb(string filePath, float db) {
    auto it = mSourceMapStore->sourceMap.find(filePath);
    if (it == mSourceMapStore->sourceMap.end()) {
        return;
    }

    auto gainAdjustment = GainAdjustment(db);
    gainAdjustment.synthesize(it->second);
}

void MixingEngine::applySourceTransformation(string filePath) {
    auto it = mSourceMapStore->sourceMap.find(filePath);
    if (it == mSourceMapStore->sourceMap.end()) {
        return;
    }

    it->second->applyBackupBufferData();
}

void MixingEngine::clearSourceTransformation(string filePath) {
    auto it = mSourceMapStore->sourceMap.find(filePath);
    if (it == mSourceMapStore->sourceMap.end()) {
        return;
    }

    it->second->resetBackupBufferData();
}

void MixingEngine::setSourceBounds(string filePath, int64_t start, int64_t end) {
    auto it = mSourceMapStore->sourceMap.find(filePath);
    if (it == mSourceMapStore->sourceMap.end()) {
        return;
    }

    it->second->setSelectionStart(start);
    it->second->setSelectionEnd(end);
}

void MixingEngine::resetSourceBounds(string filePath) {
    auto it = mSourceMapStore->sourceMap.find(filePath);
    if (it == mSourceMapStore->sourceMap.end()) {
        return;
    }

    it->second->resetSelectionStart();
    it->second->resetSelectionEnd();
}

int64_t MixingEngine::shiftBySamples(string filePath, int64_t position, int64_t numSamples) {
    auto it = mSourceMapStore->sourceMap.find(filePath);
    if (it == mSourceMapStore->sourceMap.end()) {
        return -1;
    }

    return it->second->shiftBySamples(position, numSamples);
}

int64_t MixingEngine::cutToClipboard(string filePath, int64_t startPosition, int64_t endPosition) {
    if (startPosition < 0 || endPosition < 0) {
        return -1;
    }

    auto it = mSourceMapStore->sourceMap.find(filePath);
    if (it == mSourceMapStore->sourceMap.end()) {
        return -1;
    }

    auto source = it->second;

    int channelCount = source->getProperties().channelCount;
    int64_t numElementsToCopy = (endPosition - startPosition + 1) * channelCount;

    vector <float> v;
    clipboard.swap(v);
    clipboard.resize(numElementsToCopy);

    return source->cutToClipboard(startPosition, endPosition, clipboard);
}

bool MixingEngine::copyToClipboard(string filePath, int64_t startPosition, int64_t endPosition) {

    if (startPosition < 0 || endPosition < 0) {
        return false;
    }

    auto it = mSourceMapStore->sourceMap.find(filePath);
    if (it == mSourceMapStore->sourceMap.end()) {
        return false;
    }

    auto source = it->second;

    int channelCount = source->getProperties().channelCount;
    int64_t numElementsToCopy = (endPosition - startPosition + 1) * channelCount;

    vector <float> v;
    clipboard.swap(v);
    clipboard.resize(numElementsToCopy);

    source->copyToClipboard(startPosition, endPosition, clipboard);

    return true;
}

bool MixingEngine::muteAndCopyToClipboard(string filePath, int64_t startPosition, int64_t endPosition) {

    if (startPosition < 0 || endPosition < 0) {
        return false;
    }

    auto it = mSourceMapStore->sourceMap.find(filePath);
    if (it == mSourceMapStore->sourceMap.end()) {
        return false;
    }

    auto source = it->second;

    int channelCount = source->getProperties().channelCount;
    int64_t numElementsToCopy = (endPosition - startPosition + 1) * channelCount;

    vector <float> v;
    clipboard.swap(v);
    clipboard.resize(numElementsToCopy);

    source->muteAndCopyToClipboard(startPosition, endPosition, clipboard);

    return true;
}

void MixingEngine::pasteFromClipboard(string filePath, int64_t position) {
    if (position < 0) return;

    auto it = mSourceMapStore->sourceMap.find(filePath);
    if (it == mSourceMapStore->sourceMap.end()) {
        return;
    }

    auto source = it->second;

    source->pasteFromClipboard(position, clipboard);
}

void MixingEngine::pasteNewFromClipboard(string fileId) {
    auto it = mSourceMapStore->sourceMap.find(fileId);
    if (it != mSourceMapStore->sourceMap.end()) {
        fileId.erase();
        return;
    }

    shared_ptr<BufferedDataSource> source = mMixingIO.createClipboardDataSource(clipboard);
    mSourceMapStore->sourceMap.insert(pair<string, shared_ptr<FileDataSource>>(fileId, move(source)));
    fileId.erase();
}
