//
// Created by asalehin on 10/26/20.
//

#ifndef FAST_MIXER_FFMPEGEXTRACTOR_H
#define FAST_MIXER_FFMPEGEXTRACTOR_H

#include <string>
#include "../Constants.h"
#include "list"

using namespace std;

#define AUDIO_INBUF_SIZE 20480
#define AUDIO_REFILL_THRESH 4096

class FFMpegExtractor {
    FFMpegExtractor(const string &filePath, const AudioProperties targetProperties);

    list<uint8_t>& read();

    static void decode(AVCodecContext *dec_ctx, AVPacket *pkt, AVFrame *frame, list<uint8_t>& samples, list<uint8_t>::iterator& it);

private:
    AudioProperties mTargetProperties{};
    const char *mFilePath;
    list<uint8_t> samples;
};


#endif //FAST_MIXER_FFMPEGEXTRACTOR_H
