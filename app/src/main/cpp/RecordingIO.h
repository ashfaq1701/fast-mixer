//
// Created by asalehin on 7/11/20.
//

#ifndef FAST_MIXER_RECORDINGIO_H
#define FAST_MIXER_RECORDINGIO_H

#include<TaskQueue.h>
#include <AAssetDataSource.h>
#include <Player.h>

#ifndef MODULE_NAME
#define MODULE_NAME  "RecordingIO"
#endif

class RecordingIO {
public:
    RecordingIO(AAssetManager &assetManager) : mAssetManager(assetManager) {
        taskQueue = new TaskQueue();
    }

    ~RecordingIO() {
        taskQueue->stop_queue();
    }

    TaskQueue *taskQueue;

    int32_t write(const int16_t *sourceData, int32_t numSamples);
    int32_t read_live_playback(int16_t *targetData, int32_t numSamples);
    void read_playback(int16_t *targetData, int32_t numSamples);
    int32_t getTotalSamples() const { return mTotalSamples; }

    void flush_buffer();

    void setRecordingFilePath(std::string recordingFilePath) {
        mRecordingFilePath = recordingFilePath;
    }

    void set_channel_count(int32_t channelCount) {
        mChannelCount = channelCount;
    }

    void set_sample_rate(int32_t sampleRate) {
        mSampleRate = sampleRate;
    }

    void setup_audio_source();
    void pause_audio_source();
    void stop_audio_source();
    void openPlaybackFp();
    void closePlaybackFp();

private:
    const char* TAG = "RecordingIO:: %s";

    std::string mRecordingFilePath;

    FILE* playbackFp = nullptr;
    bool isPlaybackFpOpen = false;
    int32_t mTotalReadPlayback = 0;

    std::unique_ptr<Player> mRecordedTrack;

    int32_t mChannelCount = 0;
    int32_t mSampleRate = 0;

    int32_t mTotalSamples = 0;
    int32_t mIteration = 1;
    int32_t mWriteIndex = 0;
    int32_t mLivePlaybackReadIndex = 0;
    const int kMaxSamples = 480000; // 10s of audio data @ 48kHz
    const int16_t gain_factor = 2;

    bool livePlaybackEnabled = true;
    bool readyToFlush = false;
    bool toFlush = false;

    int16_t* mData = new int16_t[kMaxSamples]{0};

    static void flush_to_file(int16_t* buffer, int length, const std::string& recordingFilePath);

    static void read_playback_runnable(int16_t *targetData, int32_t numSamples, RecordingIO* recordingIo);

    bool validate_audio_file();

    void perform_flush(int flushIndex);

    AAssetManager& mAssetManager;

    static std::mutex mtx;
    static std::condition_variable reallocated;
    static bool is_reallocated;

    static bool check_if_reallocated();
};


#endif //FAST_MIXER_RECORDINGIO_H
