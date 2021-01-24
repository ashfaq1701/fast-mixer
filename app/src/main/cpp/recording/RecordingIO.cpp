//
// Created by asalehin on 7/11/20.
//

#include <cstdint>
#include <cstdio>
#include <string>
#include <unistd.h>
#include "RecordingIO.h"
#include "../logging_macros.h"
#include "../utils/Utils.h"
#include <mutex>
#include <condition_variable>
#include <sys/stat.h>
#include "../Constants.h"
#include "RecordingEngine.h"

mutex RecordingIO::mtx;
condition_variable RecordingIO::reallocated;
bool RecordingIO::is_reallocated = false;

RecordingIO::RecordingIO() {
    Player* player = new Player();
    mPlayer.reset(move(player));

    taskQueue = new TaskQueue();
}

bool RecordingIO::check_if_reallocated() {
    return is_reallocated;
}

FileDataSource* RecordingIO::setup_audio_source() {
    if (!validate_audio_file()) {
        return nullptr;
    }

    AudioProperties targetProperties{
            .channelCount = RecordingStreamConstants::mOutputChannelCount,
            .sampleRate = RecordingStreamConstants::mSampleRate
    };

    return FileDataSource::newFromCompressedFile(mRecordingFilePath.c_str(), targetProperties);
}

void RecordingIO::add_source_to_player(shared_ptr<DataSource> fileDataSource) {
    map<string, shared_ptr<DataSource>> sourceMap;
    sourceMap.insert(pair<string, shared_ptr<DataSource>>(mRecordingFilePath, fileDataSource));
    mPlayer->addSourceMap(sourceMap);
    mPlayer->setPlaying(true);

    int32_t playHead = mPlayer->getPlayHead();
    if (playHead >= mPlayer->getTotalSampleFrames()) {
        playHead = 0;
        mPlayer->setPlayHead(playHead);
    }
}

bool RecordingIO::validate_audio_file() {
    return !(mRecordingFilePath.empty() || (access(mRecordingFilePath.c_str(), F_OK) == -1));
}

void RecordingIO::read_playback(float *targetData, int32_t numFrames) {
    mPlayer->renderAudio(targetData, numFrames);
}

void RecordingIO::flush_to_file(int16_t* buffer, int32_t length, const string& recordingFilePath, shared_ptr<SndfileHandle>& recordingFile) {
    if (!recordingFile) {
        int format = SF_FORMAT_WAV | SF_FORMAT_PCM_16;
        SndfileHandle file = SndfileHandle(recordingFilePath, SFM_WRITE, format, RecordingStreamConstants::mInputChannelCount, RecordingStreamConstants::mSampleRate);
        recordingFile = make_shared<SndfileHandle>(file);
    }
    if (!buffer) {
        return;
    }
    recordingFile->write(buffer, length);

    unique_lock<mutex> lck(mtx);
    reallocated.wait(lck, check_if_reallocated);
    delete[] buffer;
    is_reallocated = false;
    lck.unlock();
}

void RecordingIO::perform_flush(int flushIndex) {
    int16_t* oldBuffer = mData;
    is_reallocated = false;
    taskQueue->enqueue([&]() {
        flush_to_file(oldBuffer, flushIndex, mRecordingFilePath, mRecordingFile);
    });
    auto * newData = new int16_t[kMaxSamples]{0};
    copy(mData + flushIndex, mData + mWriteIndex, newData);
    mData = newData;
    is_reallocated = true;
    reallocated.notify_all();
    mWriteIndex -= flushIndex;
    mLivePlaybackReadIndex -= flushIndex;
    readyToFlush = false;
    toFlush = false;
    mIteration = 1;
}

int32_t RecordingIO::write(const int16_t *sourceData, int32_t numSamples) {
    if (mWriteIndex + numSamples > kMaxSamples) {
        readyToFlush = true;
    }

    int flushIndex = 0;
    if (readyToFlush) {
        int upperBound  = 0;
        if (mWriteIndex < kMaxSamples) {
            upperBound = mWriteIndex;
        } else {
            upperBound = kMaxSamples;
        }
        if (mLivePlaybackEnabled && mLivePlaybackReadIndex >= upperBound) {
            flushIndex = upperBound;
            toFlush = true;
        } else if (!mLivePlaybackEnabled) {
            flushIndex = mWriteIndex;
            toFlush = true;
        }
    }

    if (toFlush) {
        perform_flush(flushIndex);
    }

    if (mWriteIndex + numSamples > mIteration * kMaxSamples) {
        readyToFlush = true;
        mIteration++;
        int32_t newSize = mIteration * kMaxSamples;
        auto * newData = new int16_t[newSize]{0};
        copy(mData, mData + mWriteIndex, newData);
        delete[] mData;
        mData = newData;
    }

    for(int i = 0; i < numSamples; i++) {
        mData[mWriteIndex++] = sourceData[i] * gain_factor;
        if (currentMax < abs(sourceData[i])) {
            currentMax = abs(sourceData[i]);
        }
    }
    mTotalSamples += numSamples;

    return numSamples;
}

void RecordingIO::flush_buffer() {
    if (mWriteIndex > 0) {
        int16_t* oldBuffer = mData;
        if (!oldBuffer) {
            return;
        }
        is_reallocated = false;
        int32_t flushIndex = mWriteIndex;
        taskQueue->enqueue([&]() {
            flush_to_file(oldBuffer, flushIndex, mRecordingFilePath, mRecordingFile);
        });
        mIteration = 1;
        mWriteIndex = 0;
        mLivePlaybackReadIndex = 0;
        mData = new int16_t[kMaxSamples]{0};
        is_reallocated = true;
        reallocated.notify_all();
        readyToFlush = false;
        toFlush = false;
    }
}

int32_t RecordingIO::read_live_playback(int16_t *targetData, int32_t numSamples) {
    if (mLivePlaybackReadIndex > mWriteIndex) {
        return 0;
    }
    int32_t framesRead = 0;
    auto framesToCopy = numSamples;
    if (mLivePlaybackReadIndex + numSamples >= mTotalSamples) {
        framesToCopy = mTotalSamples - mLivePlaybackReadIndex;
    }
    if (framesToCopy > 0) {
        memcpy(targetData, mData + mLivePlaybackReadIndex, framesToCopy * sizeof(int16_t));
        mLivePlaybackReadIndex += framesToCopy;
    }
    return framesRead;
}

void RecordingIO::sync_live_playback() {
    mLivePlaybackReadIndex = mWriteIndex;
}

void RecordingIO::setLivePlaybackEnabled(bool livePlaybackEnabled) {
    mLivePlaybackEnabled = livePlaybackEnabled;
}

int RecordingIO::getCurrentMax() {
    int temp = currentMax;
    currentMax = 0;
    return temp;
}

int64_t RecordingIO::getPlayerMaxTotalSourceFrames() {
    return mPlayer->getMaxTotalSourceFrames();
}

void RecordingIO::resetCurrentMax() {
    currentMax = 0;
}

void RecordingIO::setStopPlaybackCallback(function<void()> stopPlaybackCallback) {
    mStopPlaybackCallback = stopPlaybackCallback;
    mPlayer->setPlaybackCallback(mStopPlaybackCallback);
}

int RecordingIO::getTotalRecordedFrames() {
    if (mPlayer) {
        return mPlayer->getTotalSampleFrames();
    }
    return 0;
}

int32_t RecordingIO::getCurrentPlaybackProgress() {
    return mPlayer->getPlayHead();
}

void RecordingIO::setPlayHead(int position) {
    mPlayer->setPlayHead(position);
}

int RecordingIO::getDurationInSeconds() {
    return (int) mTotalSamples / (RecordingStreamConstants::mInputChannelCount * RecordingStreamConstants::mSampleRate);
}

void RecordingIO::clearPlayerSources() {
    mPlayer->clearSources();
}

void RecordingIO::setPlaybackPlaying(bool playing) {
    mPlayer->setPlaying(playing);
}

void RecordingIO::addSourceMap(map<string, shared_ptr<DataSource>> playMap) {
    mPlayer->addSourceMap(playMap);
}

void RecordingIO::addSourceMapWithRecordedSource(map<string, shared_ptr<DataSource>> playMap, shared_ptr<DataSource> recordedSource) {
    playMap.insert(pair<string, shared_ptr<DataSource>>(mRecordingFilePath, recordedSource));
    mPlayer->addSourceMap(playMap);
}

bool RecordingIO::checkPlayerSources(map<string, shared_ptr<DataSource>> playMap) {
    return mPlayer->checkPlayerSources(playMap);
}

void RecordingIO::resetProperties() {
    taskQueue->clear_queue();
    mPlayer.reset();
    mPlayer.reset();
    mRecordingFile.reset();
    mTotalSamples = 0;
    mIteration = 1;
    mWriteIndex = 0;
    mLivePlaybackReadIndex = 0;
    readyToFlush = false;
    toFlush = false;

    unlink(mRecordingFilePath.c_str());
}


