//
// Created by asalehin on 7/11/20.
//

#ifndef FAST_MIXER_SOUNDRECORDING_H
#define FAST_MIXER_SOUNDRECORDING_H

#include<TaskQueue.h>

#ifndef MODULE_NAME
#define MODULE_NAME  "SoundRecording"
#endif

class SoundRecording {
public:
    SoundRecording() {
        taskQueue = new TaskQueue();
    }

    ~SoundRecording() {
        taskQueue->stop_queue();
    }

    TaskQueue *taskQueue;

    int32_t write(const int16_t *sourceData, int32_t numSamples);
    int32_t read(int16_t *targetData, int32_t numSamples);
    int32_t getTotalSamples() const { return mTotalSamples; }

    void flush_buffer();

    void setRecordingFilePath(std::string recordingFilePath) {
        mRecordingFilePath = recordingFilePath;
    }

private:
    const char* TAG = "SoundRecording:: %s";

    std::string mRecordingFilePath;

    std::atomic<int32_t> mTotalSamples {0};
    std::atomic<int32_t> mIteration { 1 };
    std::atomic<int32_t> mWriteIndex { 0 };
    std::atomic<int32_t> mLivePlaybackReadIndex {0 };
    const int kMaxSamples = 480000; // 10s of audio data @ 48kHz
    const int16_t gain_factor = 2;

    bool livePlaybackEnabled = true;
    bool readyToFlush = false;
    bool toFlush = false;

    int16_t* mData = new int16_t[kMaxSamples]{0};

    static void flush_to_file(int16_t* buffer, int length, const std::string& recordingFilePath);
};


#endif //FAST_MIXER_SOUNDRECORDING_H
