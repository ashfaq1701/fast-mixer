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
#include "../structs.h"
#include "../logging_macros.h"
#include "vector"

using namespace std;

class FFMpegExtractor {
public:
    std::unique_ptr<FILE, void(*)(FILE *)> fp {
            nullptr,
            [](FILE *f) {
                fseek(f, 0, SEEK_SET);
                fclose(f);
            }
    };

    int64_t decode(int fd, uint8_t *targetData, AudioProperties targetProperties);
    int64_t getTotalBytes(int fd, AudioProperties targetProperties);

    int mSampleRate = 0;
    int mChannelCount = 0;
    int mAudioFormat = 0;

private:

    bool createAVIOContext(uint8_t *buffer, uint32_t bufferSize,
                           AVIOContext **avioContext);

    bool createAVFormatContext(AVIOContext *avioContext, AVFormatContext **avFormatContext);

    bool openAVFormatContext(AVFormatContext *avFormatContext);

    bool getStreamInfo(AVFormatContext *avFormatContext);

    AVStream* getBestAudioStream(AVFormatContext *avFormatContext);

    int64_t decodeOp(int fd, uint8_t* targetData, AudioProperties targetProperties, function<void(uint8_t *, int64_t, short *, int64_t)> f);

    void printCodecParameters(AVCodecParameters *params);
};


#endif //FAST_MIXER_FFMPEGEXTRACTOR_H
