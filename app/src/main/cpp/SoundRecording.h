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
};


#endif //FAST_MIXER_SOUNDRECORDING_H
