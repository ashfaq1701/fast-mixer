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
#include "../logging_macros.h"
#include "list"

using namespace std;

class FFMpegExtractor {
public:
    FFMpegExtractor(const string &filePath, const AudioProperties targetProperties);
    std::unique_ptr<FILE, void(*)(FILE *)> fp {
            nullptr,
            [](FILE *f) {
                fclose(f);
            }
    };
    const char *mFilePath;

    int64_t decode(uint8_t *targetData);

    int mSampleRate = 0;
    int mChannelCount = 0;
    int mAudioFormat = 0;

private:
    AudioProperties mTargetProperties{};

    bool createAVIOContext(uint8_t *buffer, uint32_t bufferSize,
                                            AVIOContext **avioContext);

    bool createAVFormatContext(AVIOContext *avioContext, AVFormatContext **avFormatContext);

    bool openAVFormatContext(AVFormatContext *avFormatContext);

    bool getStreamInfo(AVFormatContext *avFormatContext);

    AVStream* getBestAudioStream(AVFormatContext *avFormatContext);

    void printCodecParameters(AVCodecParameters *params);

    int64_t decodeOp(uint8_t *targetData, function<void(uint8_t *, int, short *, int64_t)> f);
};


#endif //FAST_MIXER_FFMPEGEXTRACTOR_H
