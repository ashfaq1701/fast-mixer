//
// Created by asalehin on 10/26/20.
//

#ifndef FAST_MIXER_FFMPEGEXTRACTOR_H
#define FAST_MIXER_FFMPEGEXTRACTOR_H

extern "C" {
#include <libavformat/avformat.h>
#include <libswresample/swresample.h>
#include <libavutil/opt.h>
}
#include <string>
#include "../Constants.h"
#include "list"

using namespace std;

#define AUDIO_REFILL_THRESH 4096

class FFMpegExtractor {
public:
    FFMpegExtractor(const string &filePath, const AudioProperties targetProperties);

    int64_t read(uint8_t *targetData);

    int64_t decode(AVCodecContext *dec_ctx, AVPacket *pkt, AVFrame *frame, uint8_t **targetData, SwrContext *swr);

private:
    AudioProperties mTargetProperties{};
    long audioInbufSize = 0;
    const char *mFilePath;
};


#endif //FAST_MIXER_FFMPEGEXTRACTOR_H
