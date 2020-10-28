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

class FFMpegExtractor {
public:
    FFMpegExtractor(const string &filePath, const AudioProperties targetProperties);
    FILE* fp = nullptr;
    const char *mFilePath;

    int64_t decode(uint8_t *targetData);

private:
    AudioProperties mTargetProperties{};

    bool createAVIOContext(uint8_t *buffer, uint32_t bufferSize,
                                            AVIOContext **avioContext);

    bool createAVFormatContext(AVIOContext *avioContext, AVFormatContext **avFormatContext);

    bool openAVFormatContext(AVFormatContext *avFormatContext);

    bool getStreamInfo(AVFormatContext *avFormatContext);

    AVStream* getBestAudioStream(AVFormatContext *avFormatContext);

    void printCodecParameters(AVCodecParameters *params);
};


#endif //FAST_MIXER_FFMPEGEXTRACTOR_H
