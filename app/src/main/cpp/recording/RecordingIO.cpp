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

bool RecordingIO::check_if_reallocated() {
    return is_reallocated;
}

bool RecordingIO::setup_audio_source() {
    if (!validate_audio_file()) {
        mStopPlaybackCallback();
        return false;
    }

    AudioProperties targetProperties{
            .channelCount = StreamConstants::mOutputChannelCount,
            .sampleRate = StreamConstants::mSampleRate
    };

    shared_ptr<FileDataSource> audioSource{
            FileDataSource::newFromCompressedFile(mRecordingFilePath.c_str(), targetProperties)
    };

    if (!audioSource) {
        return false;
    }

    int32_t playHead = 0;
    if (mRecordedTrack) {
        playHead = mRecordedTrack->getPlayHead();
        if (playHead >= mRecordedTrack->getTotalSampleFrames()) {
            playHead = 0;
        }
        mRecordedTrack.reset();
    }

    mRecordedTrack = make_shared<Player>(audioSource, mStopPlaybackCallback);
    mRecordedTrack->setPlayHead(playHead);
    mRecordedTrack->setPlaying(true);

    return true;
}

void RecordingIO::pause_audio_source() {
    if (mRecordedTrack) {
        mRecordedTrack->setPlaying(false);
    }
}

void RecordingIO::clear_audio_source() {
    pause_audio_source();
    mRecordedTrack.reset();
}

bool RecordingIO::validate_audio_file() {
    return !(mRecordingFilePath.empty() || (access(mRecordingFilePath.c_str(), F_OK) == -1));
}

void RecordingIO::read_playback(float *targetData, int32_t numFrames, int32_t channelCount) {
    if (!validate_audio_file()) {
        mStopPlaybackCallback();
        return;
    }

    if (!mRecordedTrack) {
        mStopPlaybackCallback();
        return;
    }

    mRecordedTrack->renderAudio(targetData, numFrames);
}

void RecordingIO::flush_to_file(int16_t* buffer, int32_t length, const string& recordingFilePath, shared_ptr<SndfileHandle>& recordingFile) {
    if (!recordingFile) {
        int format = SF_FORMAT_WAV | SF_FORMAT_PCM_16;
        SndfileHandle file = SndfileHandle(recordingFilePath, SFM_WRITE, format, StreamConstants::mInputChannelCount, StreamConstants::mSampleRate);
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

void RecordingIO::resetCurrentMax() {
    currentMax = 0;
}

void RecordingIO::setStopPlaybackCallback(function<void()> stopPlaybackCallback) {
    mStopPlaybackCallback = stopPlaybackCallback;
}

int RecordingIO::getTotalRecordedFrames() {
    if (mRecordedTrack) {
        return mRecordedTrack->getTotalSampleFrames();
    }
    return 0;
}

int32_t RecordingIO::getCurrentPlaybackProgress() {
    if (mRecordedTrack) {
        return mRecordedTrack->getPlayHead();
    }
    return 0;
}

void RecordingIO::setPlayHead(int position) {
    if (mRecordedTrack) {
        mRecordedTrack->setPlayHead(position);
    }
}

int RecordingIO::getDurationInSeconds() {
    return (int) mTotalSamples / (StreamConstants::mInputChannelCount * StreamConstants::mSampleRate);
}

void RecordingIO::resetProperties() {
    taskQueue->clear_queue();
    mRecordedTrack.reset();
    mRecordedTrack.reset();
    mRecordingFile.reset();
    mTotalSamples = 0;
    mIteration = 1;
    mWriteIndex = 0;
    mLivePlaybackReadIndex = 0;
    readyToFlush = false;
    toFlush = false;

    unlink(mRecordingFilePath.c_str());
}


