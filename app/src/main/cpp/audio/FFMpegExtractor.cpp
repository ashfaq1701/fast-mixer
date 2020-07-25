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
#include <oboe/Definitions.h>
#include <unistd.h>
#include "FFMpegExtractor.h"
#include "../logging_macros.h"

constexpr int kInternalBufferSize = 1152; // Use MP3 block size. https://wiki.hydrogenaud.io/index.php?title=MP3

int read(void *opaque, uint8_t *buf, int buf_size) {
    auto asset = (FILE *) opaque;
    int bytesRead = fread(buf, sizeof(*buf), (size_t) buf_size, asset);
    return bytesRead;
}

long getFileSize(FILE *fp) {
    fpos_t pos;
    fgetpos(fp, &pos);
    fseek(fp, 0L, SEEK_END);
    long res = ftell(fp);
    fseek(fp, 0L, pos);
    return res;
}

int64_t seek(void *opaque, int64_t offset, int whence){
    auto asset = (FILE*)opaque;
    if (whence == AVSEEK_SIZE) return getFileSize(asset);
    int rs = fseek(asset, (long)offset, whence);
    if (rs != 0) {
        return -1;
    }
    long fpos = ftell(asset);
    return (int64_t)fpos;
}

bool FFMpegExtractor::createAVIOContext(FILE *asset, uint8_t *buffer, uint32_t bufferSize,
                                        AVIOContext **avioContext) {

    constexpr int isBufferWriteable = 0;

    *avioContext = avio_alloc_context(
            buffer, // internal buffer for FFmpeg to use
            bufferSize, // For optimal decoding speed this should be the protocol block size
            isBufferWriteable,
            asset, // Will be passed to our callback functions as a (void *)
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

bool FFMpegExtractor::openAVFormatContext(AVFormatContext *avFormatContext) {

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

int64_t FFMpegExtractor::decode(
        FILE *fl,
        uint8_t *targetData,
        AudioProperties targetProperties) {

    int returnValue = -1; // -1 indicates error

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
        LOGD("1. CREATING AVIO CONTEXT");
        AVIOContext *tmp = nullptr;
        if (!createAVIOContext(fl, buffer, kInternalBufferSize, &tmp)){
            LOGE("Could not create an AVIOContext");
            return returnValue;
        }
        ioContext.reset(tmp);
        LOGD("1. CREATED AVIO CONTEXT");
    }

    // Create an AVFormatContext using the avformat_free_context as the deleter function
    std::unique_ptr<AVFormatContext, decltype(&avformat_free_context)> formatContext {
            nullptr,
            &avformat_free_context
    };
    {
        LOGD("2. CREATING AVFormat CONTEXT");
        AVFormatContext *tmp;
        if (!createAVFormatContext(ioContext.get(), &tmp)) return returnValue;
        formatContext.reset(tmp);
        LOGD("2. CREATED AVFormat CONTEXT");
    }

    if (!openAVFormatContext(formatContext.get())) {
        LOGD("3. AVFormat Context empty, returning");
        return returnValue;
    }

    if (!getStreamInfo(formatContext.get())) {
        LOGD("4. Get StreamInfo empty, returning");
        return returnValue;
    }

    LOGD("5. Getting Audio Stream");
    // Obtain the best audio stream to decode
    AVStream *stream = getBestAudioStream(formatContext.get());
    if (stream == nullptr || stream->codecpar == nullptr){
        LOGE("Could not find a suitable audio stream to decode");
        return returnValue;
    }
    LOGD("5. Got Audio Stream");

    LOGD("6. Printing Codec");
    printCodecParameters(stream->codecpar);
    LOGD("6. Printed Codec");

    LOGD("7. Finding Decoder");
    // Find the codec to decode this stream
    AVCodec *codec = avcodec_find_decoder(stream->codecpar->codec_id);
    if (!codec){
        LOGE("Could not find codec with ID: %d", stream->codecpar->codec_id);
        return returnValue;
    }
    LOGD("7. Found Codec");

    LOGD("8. Creating Codec Context");
    // Create the codec context, specifying the deleter function
    std::unique_ptr<AVCodecContext, void(*)(AVCodecContext *)> codecContext {
            nullptr,
            [](AVCodecContext *c) { avcodec_free_context(&c); }
    };
    {
        AVCodecContext *tmp = avcodec_alloc_context3(codec);
        if (!tmp){
            LOGE("Failed to allocate codec context");
            return returnValue;
        }
        codecContext.reset(tmp);
    }
    LOGD("8. Created Codec Context");

    LOGD("9. Creating Codec Context");
    // Copy the codec parameters into the context
    if (avcodec_parameters_to_context(codecContext.get(), stream->codecpar) < 0){
        LOGE("Failed to copy codec parameters to codec context");
        return returnValue;
    }
    LOGD("9. Created Codec Context");

    LOGD("8. Opening Codec");
    // Open the codec
    if (avcodec_open2(codecContext.get(), codec, nullptr) < 0){
        LOGE("Could not open codec");
        return returnValue;
    }
    LOGD("8. Opened Codec");

    LOGD("9. Preparing Resampler");
    // prepare resampler
    int32_t outChannelLayout = (1 << targetProperties.channelCount) - 1;
    LOGD("Channel layout %d", outChannelLayout);
    LOGD("9. Prepared Resampler");

    LOGD("10. Preparing Resampler");
    SwrContext *swr = swr_alloc();
    av_opt_set_int(swr, "in_channel_count", stream->codecpar->channels, 0);
    av_opt_set_int(swr, "out_channel_count", targetProperties.channelCount, 0);
    av_opt_set_int(swr, "in_channel_layout", stream->codecpar->channel_layout, 0);
    av_opt_set_int(swr, "out_channel_layout", outChannelLayout, 0);
    av_opt_set_int(swr, "in_sample_rate", stream->codecpar->sample_rate, 0);
    av_opt_set_int(swr, "out_sample_rate", targetProperties.sampleRate, 0);
    av_opt_set_int(swr, "in_sample_fmt", stream->codecpar->format, 0);
    av_opt_set_sample_fmt(swr, "out_sample_fmt", AV_SAMPLE_FMT_FLT, 0);
    av_opt_set_int(swr, "force_resampling", 1, 0);
    LOGD("10. Prepared Resampler");

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
    int bytesPerSample = av_get_bytes_per_sample((AVSampleFormat)stream->codecpar->format);

    LOGD("Bytes per sample %d", bytesPerSample);

    LOGD("DECODE START");

    // While there is more data to read, read it into the avPacket
    while (av_read_frame(formatContext.get(), &avPacket) == 0){

        if (avPacket.stream_index == stream->index && avPacket.size > 0) {

            // Pass our compressed data into the codec
            result = avcodec_send_packet(codecContext.get(), &avPacket);
            if (result != 0) {
                LOGE("avcodec_send_packet error: %s", av_err2str(result));
                goto cleanup;
            }

            // Retrieve our raw data from the codec
            result = avcodec_receive_frame(codecContext.get(), decodedFrame);
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
                    targetProperties.sampleRate,
                    decodedFrame->sample_rate,
                    AV_ROUND_UP);

            short *buffer1;
            av_samples_alloc(
                    (uint8_t **) &buffer1,
                    nullptr,
                    targetProperties.channelCount,
                    dst_nb_samples,
                    AV_SAMPLE_FMT_FLT,
                    0);
            int frame_count = swr_convert(
                    swr,
                    (uint8_t **) &buffer1,
                    dst_nb_samples,
                    (const uint8_t **) decodedFrame->data,
                    decodedFrame->nb_samples);

            int64_t bytesToWrite = frame_count * sizeof(float) * targetProperties.channelCount;
            memcpy(targetData + bytesWritten, buffer1, (size_t)bytesToWrite);
            bytesWritten += bytesToWrite;
            av_freep(&buffer1);

            avPacket.size = 0;
            avPacket.data = nullptr;
        }
    }

    av_frame_free(&decodedFrame);
    LOGD("DECODE END");

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




