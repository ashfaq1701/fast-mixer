//
// Created by asalehin on 7/21/20.
//

#ifndef FAST_MIXER_CONSTANTS_H
#define FAST_MIXER_CONSTANTS_H

struct AudioProperties {
    int32_t channelCount;
    int32_t sampleRate;
};

struct buffer_data {
    uint8_t *ptr;
    size_t size; ///< size left in the buffer
};

#endif //FAST_MIXER_CONSTANTS_H
