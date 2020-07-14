//
// Created by asalehin on 7/11/20.
//

#ifndef FAST_MIXER_SOUNDRECORDING_H
#define FAST_MIXER_SOUNDRECORDING_H

#include<atomic>

#ifndef MODULE_NAME
#define MODULE_NAME  "SoundRecording"
#endif


class SoundRecording {
public:
    int32_t write(const int16_t *sourceData, int32_t numSamples, char* recordingFilePath);
    int32_t getTotalSamples() const { return mTotalSamples; }

private:
    const char* TAG = "SoundRecording:: %d";

    std::atomic<int32_t> mTotalSamples {0};
    int16_t gain_factor = 2;
};


#endif //FAST_MIXER_SOUNDRECORDING_H
