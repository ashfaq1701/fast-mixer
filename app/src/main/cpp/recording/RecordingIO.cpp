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

mutex RecordingIO::flushMtx;
condition_variable RecordingIO::flushed;
bool RecordingIO::ongoing_flush_completed = true;

bool RecordingIO::check_if_flush_completed() {
    return ongoing_flush_completed;
}

mutex RecordingIO::livePlaybackMtx;
condition_variable RecordingIO::livePlaybackRead;
bool RecordingIO::live_playback_read_completed = true;

bool RecordingIO::check_if_live_playback_read_completed() {
    return live_playback_read_completed;
}


RecordingIO::RecordingIO() {
    Player* player = new Player();
    mPlayer.reset(move(player));

    mData.reserve(kMaxSamples);
    taskQueue = new TaskQueue();
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
    if (targetData && mPlayer) {
        mPlayer->renderAudio(targetData, numFrames);
    }
}

void RecordingIO::flush_to_file(int16_t* data, int32_t length, const string& recordingFilePath, shared_ptr<SndfileHandle>& recordingFile) {
    if (!recordingFile) {
        int format = SF_FORMAT_WAV | SF_FORMAT_PCM_16;
        SndfileHandle file = SndfileHandle(recordingFilePath, SFM_WRITE, format, RecordingStreamConstants::mInputChannelCount, RecordingStreamConstants::mSampleRate);
        recordingFile = make_shared<SndfileHandle>(file);
    }

    recordingFile->write(data, length);
}

int32_t RecordingIO::write(const int16_t *sourceData, int32_t numSamples) {
    // Wait if a flush action is already in progress
    // Known Issue: If live playback is on, this will blink in the live playback for a while

    unique_lock<mutex> lck(flushMtx);
    flushed.wait(lck, check_if_flush_completed);
    lck.unlock();

    if (mData.size() + numSamples >= kMaxSamples) {
        flush_buffer();
    }

    for(int i = 0; i < numSamples; i++) {
        mData.push_back(sourceData[i] * gain_factor);
        if (currentMax < abs(sourceData[i])) {
            currentMax = abs(sourceData[i]);
        }
    }
    mTotalSamples += numSamples;

    return numSamples;
}

void RecordingIO::flush_buffer() {

    if (mData.size() > 0) {

        ongoing_flush_completed = false;

        int numItems = mData.size();

        copy(mData.begin(), mData.begin() + numItems, mBuff);

        taskQueue->enqueue(move([&]() {
            flush_to_file(mBuff, numItems, mRecordingFilePath, mRecordingFile);
        }));

        // Wait if a live playback read action is already in progress
        unique_lock<mutex> lck(livePlaybackMtx);
        livePlaybackRead.wait(lck, check_if_live_playback_read_completed);
        lck.unlock();

        mData.clear();
        mLivePlaybackReadIndex = 0;

        ongoing_flush_completed = true;
        flushed.notify_all();
    }
}

int32_t RecordingIO::read_live_playback(int16_t *targetData, int32_t numSamples) {
    if (mLivePlaybackReadIndex > mData.size()) {
        return 0;
    }
    int32_t framesRead = 0;
    auto framesToCopy = numSamples;
    if (mLivePlaybackReadIndex + numSamples >= mData.size()) {
        framesToCopy = mData.size() - mLivePlaybackReadIndex;
    }
    if (framesToCopy > 0) {

        // Set the flag so that any flush action waits
        live_playback_read_completed = false;
        copy(mData.begin() + mLivePlaybackReadIndex, mData.begin() + mLivePlaybackReadIndex + framesToCopy, targetData);
        mLivePlaybackReadIndex += framesToCopy;
        live_playback_read_completed = true;
        livePlaybackRead.notify_all();
    }
    return framesRead;
}

void RecordingIO::sync_live_playback() {
    mLivePlaybackReadIndex = mData.size();
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

int RecordingIO::getTotalSampleFrames() {
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
    mPlayer->setPlayHead(0);
    mRecordingFile.reset();
    mTotalSamples = 0;
    mLivePlaybackReadIndex = 0;

    unlink(mRecordingFilePath.c_str());
}


