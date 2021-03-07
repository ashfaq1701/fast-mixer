//
// Created by asalehin on 7/11/20.
//

#ifndef FAST_MIXER_RECORDINGIO_H
#define FAST_MIXER_RECORDINGIO_H

#include "../audio/FileDataSource.h"
#include "sndfile.hh"
#include "../audio/Player.h"
#include "../taskqueue/TaskQueue.h"
#include "oboe/Definitions.h"
#include "streams/RecordingStreamConstants.h"

#ifndef MODULE_NAME
#define MODULE_NAME  "RecordingIO"
#endif

using namespace std;

const int kMaxSamples = 30 * oboe::DefaultStreamValues::SampleRate * oboe::ChannelCount::Stereo;

class RecordingIO {
public:

    RecordingIO();

    ~RecordingIO() {
        taskQueue->stop_queue();
    }
    int32_t write(const int16_t *sourceData, int32_t numSamples);
    int32_t read_live_playback(int16_t *targetData, int32_t numSamples);
    void read_playback(float *targetData, int32_t numSamples);
    int32_t getTotalSamples() const { return mTotalSamples; }

    void flush_buffer();

    void setRecordingFilePath(string recordingFilePath) {
        mRecordingFilePath = move(recordingFilePath);
    }

    FileDataSource* setup_audio_source(int fd);
    void add_source_to_player(shared_ptr<DataSource> fileDataSource);
    void clear_audio_source();

    void sync_live_playback();

    void setLivePlaybackEnabled(bool livePlaybackEnabled);

    int getCurrentMax();

    int64_t getPlayerMaxTotalSourceFrames();

    void resetCurrentMax();

    void setStopPlaybackCallback(function<void(void)> stopPlaybackCallback);

    int getTotalSampleFrames();

    int getCurrentPlaybackProgress();

    void setPlayHead(int position);

    int getDurationInSeconds();

    void resetProperties();

    void clearPlayerSources();

    void setPlaybackPlaying(bool playing);

    void addSourceMap(map<string, shared_ptr<DataSource>> playMap);

    void addSourceMapWithRecordedSource(map<string, shared_ptr<DataSource>> playMap, shared_ptr<DataSource> recordedSource);

    bool checkPlayerSources(map<string, shared_ptr<DataSource>> playMap);

    void reserveRecordingBuffer();

    void clearRecordingBuffer();

private:
    const char* TAG = "RecordingIO:: %d";

    TaskQueue *taskQueue;

    string mRecordingFilePath;

    shared_ptr<SndfileHandle> mRecordingFile {nullptr};
    shared_ptr<Player> mPlayer {nullptr};

    int32_t mTotalSamples = 0;
    int32_t mLivePlaybackReadIndex = 0;
    const int16_t gain_factor = 1;

    int currentMax = 0;

    bool mLivePlaybackEnabled = true;

    function<void()> mStopPlaybackCallback = nullptr;

    vector<int16_t> mData;
    int16_t* mBuff = new int16_t[kMaxSamples]{0};

    static void flush_to_file(int16_t* data, int32_t length, const string& recordingFilePath, shared_ptr<SndfileHandle>& recordingFile);

    bool validate_audio_file();

    static mutex flushMtx;
    static condition_variable flushed;
    static bool ongoing_flush_completed;
    static bool check_if_flush_completed();

    static mutex livePlaybackMtx;
    static condition_variable livePlaybackRead;
    static bool live_playback_read_completed;
    static bool check_if_live_playback_read_completed();
};


#endif //FAST_MIXER_RECORDINGIO_H
