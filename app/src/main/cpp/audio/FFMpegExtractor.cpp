/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <memory>
#include <string>
#include <oboe/Definitions.h>
#include <unistd.h>
#include "FFMpegExtractor.h"
#include "../logging_macros.h"
#include "../Utils.h"

constexpr int kInternalBufferSize = 1152; // Use MP3 block size. https://wiki.hydrogenaud.io/index.php?title=MP3

FFMpegExtractor::FFMpegExtractor(const std::string &filePath, const AudioProperties targetProperties) {
    mFilePath = filePath.c_str();
    mTargetProperties = targetProperties;
}

int read(void *data, uint8_t *buf, int buf_size) {
    FFMpegExtractor *hctx = (FFMpegExtractor*)data;
    size_t len = fread(buf, 1, buf_size, hctx->fp);
    if (len == 0) {
        // Let FFmpeg know that we have reached EOF, or do something else
        return AVERROR_EOF;
    }
    return (int)len;
}

long getFileSize(FILE *fp) {
    fpos_t pos;
    fgetpos(fp, &pos);
    fseek(fp, 0L, SEEK_END);
    long res = ftell(fp);
    fseek(fp, 0L, pos);
    return res;
}

int64_t seek(void *data, int64_t pos, int whence) {
    FFMpegExtractor *hctx = (FFMpegExtractor*)data;
    if (whence == AVSEEK_SIZE) {
        return getSizeOfFile(hctx->mFilePath);
    }
    int rs = fseek(hctx->fp, (long)pos, whence);
    if (rs != 0) {
        return -1;
    }
    long fpos = ftell(hctx->fp); // int64_t is usually long long
    return (int64_t)fpos;
}

bool FFMpegExtractor::createAVIOContext(uint8_t *buffer, uint32_t bufferSize,
                                        AVIOContext **avioContext) {

    constexpr int isBufferWriteable = 0;

    *avioContext = avio_alloc_context(
            buffer, // internal buffer for FFmpeg to use
            bufferSize, // For optimal decoding speed this should be the protocol block size
            isBufferWriteable,
            (void*)this, // Will be passed to our callback functions as a (void *)
            read, // Read callback function
            nullptr, // Write callback function (not used)
            seek); // Seek callback function

    if (*avioContext == nullptr){
        LOGE("Failed to create AVIO context");
        return false;
    } else {
        return true;
    }
}

bool
FFMpegExtractor::createAVFormatContext(AVIOContext *avioContext, AVFormatContext **avFormatContext) {

    *avFormatContext = avformat_alloc_context();
    (*avFormatContext)->pb = avioContext;

    if (*avFormatContext == nullptr){
        LOGE("Failed to create AVFormatContext");
        return false;
    } else {
        return true;
    }
}

bool FFMpegExtractor::openAVFormatContext(AVFormatContext *avFormatContext, FILE* fl) {
    int result = avformat_open_input(&avFormatContext,
                                     "", /* URL is left empty because we're providing our own I/O */
                                     nullptr /* AVInputFormat *fmt */,
                                     nullptr /* AVDictionary **options */
    );

    if (result == 0) {
        return true;
    } else {
        LOGE("Failed to open file. Error code %s", av_err2str(result));
        return false;
    }
}

bool FFMpegExtractor::getStreamInfo(AVFormatContext *avFormatContext) {

    int result = avformat_find_stream_info(avFormatContext, nullptr);
    if (result == 0 ){
        return true;
    } else {
        LOGE("Failed to find stream info. Error code %s", av_err2str(result));
        return false;
    }
}

AVStream *FFMpegExtractor::getBestAudioStream(AVFormatContext *avFormatContext) {

    int streamIndex = av_find_best_stream(avFormatContext, AVMEDIA_TYPE_AUDIO, -1, -1, nullptr, 0);

    if (streamIndex < 0){
        LOGE("Could not find stream");
        return nullptr;
    } else {
        return avFormatContext->streams[streamIndex];
    }
}

bool FFMpegExtractor::decode() {
    decodedSuccessfully = false;

    fp = fopen(mFilePath, "rb");

    // Create a buffer for FFmpeg to use for decoding (freed in the custom deleter below)
    auto buffer = reinterpret_cast<uint8_t*>(av_malloc(kInternalBufferSize));

    // Create an AVIOContext with a custom deleter
    std::unique_ptr<AVIOContext, void(*)(AVIOContext *)> ioContext {
            nullptr,
            [](AVIOContext *c) {
                av_free(c->buffer);
                avio_context_free(&c);
            }
    };
    {
        AVIOContext *tmp = nullptr;
        if (!createAVIOContext(buffer, kInternalBufferSize, &tmp)){
            LOGE("Could not create an AVIOContext");
            return decodedSuccessfully;
        }
        ioContext.reset(tmp);
    }

    // Create an AVFormatContext using the avformat_free_context as the deleter function
    mFormatContext = std::unique_ptr<AVFormatContext, decltype(&avformat_free_context)> {
            nullptr,
            &avformat_free_context
    };
    {
        AVFormatContext *tmp;
        if (!createAVFormatContext(ioContext.get(), &tmp)) return decodedSuccessfully;
        mFormatContext.reset(tmp);
    }

    if (!openAVFormatContext(mFormatContext.get(), fp)) {
        return decodedSuccessfully;
    }

    if (!getStreamInfo(mFormatContext.get())) {
        return decodedSuccessfully;
    }

    // Obtain the best audio stream to decode
    mStream = getBestAudioStream(mFormatContext.get());
    if (mStream == nullptr || mStream->codecpar == nullptr){
        LOGE("Could not find a suitable audio stream to decode");
        return decodedSuccessfully;
    }

    printCodecParameters(mStream->codecpar);

    // Find the codec to decode this stream
    AVCodec *codec = avcodec_find_decoder(mStream->codecpar->codec_id);
    if (!codec){
        LOGE("Could not find codec with ID: %d", mStream->codecpar->codec_id);
        return decodedSuccessfully;
    }

    // Create the codec context, specifying the deleter function
    mCodecContext = std::unique_ptr<AVCodecContext, void(*)(AVCodecContext *)> {
            nullptr,
            [](AVCodecContext *c) { avcodec_free_context(&c); }
    };
    {
        AVCodecContext *tmp = avcodec_alloc_context3(codec);
        if (!tmp){
            LOGE("Failed to allocate codec context");
            return decodedSuccessfully;
        }
        mCodecContext.reset(tmp);
    }

    // Copy the codec parameters into the context
    if (avcodec_parameters_to_context(mCodecContext.get(), mStream->codecpar) < 0){
        LOGE("Failed to copy codec parameters to codec context");
        return decodedSuccessfully;
    }

    // Open the codec
    if (avcodec_open2(mCodecContext.get(), codec, nullptr) < 0){
        LOGE("Could not open codec");
        return decodedSuccessfully;
    }

    decodedSuccessfully = true;
    return decodedSuccessfully;
}

int64_t FFMpegExtractor::readData(
        uint8_t *targetData) {
    int returnValue = -1;

    if (!decodedSuccessfully) {
        return returnValue;
    }

    int32_t outChannelLayout = (1 << mTargetProperties.channelCount) - 1;
    LOGD("Channel layout %d", outChannelLayout);

    SwrContext *swr = swr_alloc();
    av_opt_set_int(swr, "in_channel_count", mStream->codecpar->channels, 0);
    av_opt_set_int(swr, "out_channel_count", mTargetProperties.channelCount, 0);
    av_opt_set_int(swr, "in_channel_layout", mStream->codecpar->channel_layout, 0);
    av_opt_set_int(swr, "out_channel_layout", outChannelLayout, 0);
    av_opt_set_int(swr, "in_sample_rate", mStream->codecpar->sample_rate, 0);
    av_opt_set_int(swr, "out_sample_rate", mTargetProperties.sampleRate, 0);
    av_opt_set_int(swr, "in_sample_fmt", mStream->codecpar->format, 0);
    av_opt_set_sample_fmt(swr, "out_sample_fmt", AV_SAMPLE_FMT_FLT, 0);
    av_opt_set_int(swr, "force_resampling", 1, 0);

    // Check that resampler has been inited
    int result = swr_init(swr);
    if (result != 0){
        LOGE("swr_init failed. Error: %s", av_err2str(result));
        return returnValue;
    };
    if (!swr_is_initialized(swr)) {
        LOGE("swr_is_initialized is false\n");
        return returnValue;
    }

    // Prepare to read data
    int bytesWritten = 0;
    AVPacket avPacket; // Stores compressed audio data
    av_init_packet(&avPacket);
    AVFrame *decodedFrame = av_frame_alloc(); // Stores raw audio data
    int bytesPerSample = av_get_bytes_per_sample((AVSampleFormat)mStream->codecpar->format);

    LOGD("Bytes per sample %d", bytesPerSample);

    // While there is more data to read, read it into the avPacket
    while (av_read_frame(mFormatContext.get(), &avPacket) == 0){

        if (avPacket.stream_index == mStream->index && avPacket.size > 0) {

            // Pass our compressed data into the codec
            result = avcodec_send_packet(mCodecContext.get(), &avPacket);
            if (result != 0) {
                LOGE("avcodec_send_packet error: %s", av_err2str(result));
                goto cleanup;
            }

            // Retrieve our raw data from the codec
            result = avcodec_receive_frame(mCodecContext.get(), decodedFrame);
            if (result == AVERROR(EAGAIN)) {
                // The codec needs more data before it can decode
                LOGI("avcodec_receive_frame returned EAGAIN");
                avPacket.size = 0;
                avPacket.data = nullptr;
                continue;
            } else if (result != 0) {
                LOGE("avcodec_receive_frame error: %s", av_err2str(result));
                goto cleanup;
            }

            // DO RESAMPLING
            auto dst_nb_samples = (int32_t) av_rescale_rnd(
                    swr_get_delay(swr, decodedFrame->sample_rate) + decodedFrame->nb_samples,
                    mTargetProperties.sampleRate,
                    decodedFrame->sample_rate,
                    AV_ROUND_UP);

            short *buffer1;
            av_samples_alloc(
                    (uint8_t **) &buffer1,
                    nullptr,
                    mTargetProperties.channelCount,
                    dst_nb_samples,
                    AV_SAMPLE_FMT_FLT,
                    0);
            int frame_count = swr_convert(
                    swr,
                    (uint8_t **) &buffer1,
                    dst_nb_samples,
                    (const uint8_t **) decodedFrame->data,
                    decodedFrame->nb_samples);

            int64_t bytesToWrite = frame_count * sizeof(float) * mTargetProperties.channelCount;
            memcpy(targetData + bytesWritten, buffer1, (size_t)bytesToWrite);
            bytesWritten += bytesToWrite;
            av_freep(&buffer1);

            avPacket.size = 0;
            avPacket.data = nullptr;
        }
    }

    av_frame_free(&decodedFrame);

    returnValue = bytesWritten;

    cleanup:
    return returnValue;
}

void FFMpegExtractor::printCodecParameters(AVCodecParameters *params) {

    LOGD("Stream properties");
    LOGD("Channels: %d", params->channels);
    LOGD("Channel layout: %" PRId64, params->channel_layout);
    LOGD("Sample rate: %d", params->sample_rate);
    LOGD("Format: %s", av_get_sample_fmt_name((AVSampleFormat)params->format));
    LOGD("Frame size: %d", params->frame_size);
}