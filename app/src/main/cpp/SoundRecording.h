//
// Created by asalehin on 7/11/20.
//

#ifndef FAST_MIXER_SOUNDRECORDING_H
#define FAST_MIXER_SOUNDRECORDING_H

#ifndef MODULE_NAME
#define MODULE_NAME  "SoundRecording"
#endif


class SoundRecording {
public:
    int32_t write(const int16_t *sourceData, int32_t numSamples);
    void read(int16_t *targetData, int32_t numSamples);
    int32_t getTotalSamples() const { return mTotalSamples; }

    void openRecordingFp();
    void closeRecordingFp();

    void openLivePlaybackFp();
    void closeLivePlaybackFp();

    void setRecordingFilePath(std::string recordingFilePath) {
        mRecordingFilePath = recordingFilePath;
    }

private:
    const char* TAG = "SoundRecording:: %s";

    std::string mRecordingFilePath;

    FILE* recordingFp = nullptr;
    FILE* livePlaybackFp = nullptr;

    std::atomic<bool> isRecordingFpOpen{false};
    std::atomic<bool> isLiveFpOpen{ false };

    std::atomic<int32_t> mTotalSamples {0};
    std::atomic<int32_t> mTotalRead {0};
    int16_t gain_factor = 2;

    static void write_runnable(const int16_t *sourceData, int32_t numSamples, SoundRecording* soundRecording);

    static void read_runnable(int16_t *targetData, int32_t numSamples, SoundRecording* soundRecording);
};


#endif //FAST_MIXER_SOUNDRECORDING_H
