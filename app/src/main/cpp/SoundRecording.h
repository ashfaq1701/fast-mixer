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
    SoundRecording() = default;

    explicit SoundRecording(std::string recordingFilePath) {
        mRecordingFilePath = recordingFilePath;
    }

    int32_t write(const int16_t *sourceData, int32_t numSamples);
    int32_t read(int16_t *targetData, int32_t numSamples);
    int32_t getTotalSamples() const { return mTotalSamples; }

    void openRecordingFp();
    void closeRecordingFp();

    void openLivePlaybackFp();
    void closeLivePlaybackFp();

private:
    const char* TAG = "SoundRecording:: %s";

    std::string mRecordingFilePath;

    FILE* recordingFp = nullptr;
    FILE* livePlaybackFp = nullptr;

    bool isRecordingFpOpen = false;
    bool isLiveFpOpen = false;

    int32_t mTotalSamples = 0;
    int32_t mTotalRead = 0;
    int16_t gain_factor = 1;
};


#endif //FAST_MIXER_SOUNDRECORDING_H
