//
// Created by asalehin on 7/11/20.
//

#ifndef FAST_MIXER_SOUNDRECORDING_H
#define FAST_MIXER_SOUNDRECORDING_H

#ifndef MODULE_NAME
#define MODULE_NAME  "SoundRecording"
#endif

constexpr int kMaxSamples = 480000; // 10s of audio data @ 48kHz
class SoundRecording {
public:
    int32_t write(const int16_t *sourceData, int32_t numSamples);
    int32_t read(int16_t *targetData, int32_t numSamples);
    int32_t getTotalSamples() const { return mTotalSamples; }

private:
    const char* TAG = "SoundRecording:: %s";

    std::string mRecordingFilePath;

    std::atomic<int32_t> mTotalSamples {0};
    std::atomic<int32_t> mIteration { 1 };
    std::atomic<int32_t> mWriteIndex { 0 };
    std::atomic<int32_t> mReadIndex { 0 };
    int16_t gain_factor = 2;

    int16_t* mData = new int16_t[kMaxSamples]{0};
};


#endif //FAST_MIXER_SOUNDRECORDING_H
