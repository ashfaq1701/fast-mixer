//
// Created by asalehin on 10/26/20.
//

#include "FFMpegExtractor.h"
#include "../logging_macros.h"
#include "list"
#include "../utils/Utils.h"

constexpr int kInternalBufferSize = 1152;

FFMpegExtractor::FFMpegExtractor(const string &filePath, const AudioProperties targetProperties) {
    mFilePath = filePath.c_str();
    mTargetProperties = targetProperties;
}

int read(void *data, uint8_t *buf, int buf_size) {
    auto *hctx = (FFMpegExtractor*)data;
    size_t len = fread(buf, 1, buf_size, hctx->fp);
    if (len == 0) {
        // Let FFmpeg know that we have reached EOF, or do something else
        return AVERROR_EOF;
    }
    return (int)len;
}

int64_t seek(void *data, int64_t pos, int whence) {
    auto *hctx = (FFMpegExtractor*)data;
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

void FFMpegExtractor::getAudioFileProperties() {
    auto buffer = reinterpret_cast<uint8_t*>(av_malloc(kInternalBufferSize));

    AVIOContext *ctx = nullptr;
    AVFormatContext *formatCtx = nullptr;
    AVStream *stream = nullptr;

    fp = fopen(mFilePath, "rb");

    if (!createAVIOContext(buffer, kInternalBufferSize, &ctx)){
        LOGE("Could not create an AVIOContext");
        goto cleanup;
    }

    if (!createAVFormatContext(ctx, &formatCtx)) goto cleanup;

    if (!openAVFormatContext(formatCtx)) goto cleanup;

    if (!getStreamInfo(formatCtx)) goto cleanup;

    // Obtain the best audio stream to decode
    stream = getBestAudioStream(formatCtx);
    if (stream == nullptr || stream->codecpar == nullptr){
        LOGE("Could not find a suitable audio stream to decode");
        goto cleanup;
    }

    mSampleRate = stream->codecpar->sample_rate;
    mChannelCount = stream->codecpar->channels;
    mAudioFormat = stream->codecpar->format;

    cleanup:
    if (fp) {
        fclose(fp);
    }
    if (ctx) {
        av_free(ctx->buffer);
        avio_context_free(&ctx);
    }
    if (formatCtx) {
        avformat_free_context(formatCtx);
    }
}

int64_t FFMpegExtractor::decode(uint8_t *targetData) {

    int returnValue = -1; // -1 indicates error

    // Create a buffer for FFmpeg to use for decoding (freed in the custom deleter below)
    auto buffer = reinterpret_cast<uint8_t*>(av_malloc(kInternalBufferSize));

    AVIOContext *ctx = nullptr;
    AVFormatContext *formatCtx = nullptr;
    AVCodecContext *codecCtx = nullptr;

    AVStream *stream = nullptr;

    AVCodec *codec = nullptr;

    int32_t outChannelLayout = 0;

    SwrContext *swr = nullptr;

    int result = 0, bytesWritten = 0, bytesPerSample = 0;

    AVPacket avPacket; // Stores compressed audio data

    AVFrame *decodedFrame = nullptr;

    fp = fopen(mFilePath, "rb");

    if (!createAVIOContext(buffer, kInternalBufferSize, &ctx)){
        LOGE("Could not create an AVIOContext");
        goto cleanup;
    }

    if (!createAVFormatContext(ctx, &formatCtx)) goto cleanup;

    if (!openAVFormatContext(formatCtx)) goto cleanup;

    if (!getStreamInfo(formatCtx)) goto cleanup;

    // Obtain the best audio stream to decode
    stream = getBestAudioStream(formatCtx);
    if (stream == nullptr || stream->codecpar == nullptr){
        LOGE("Could not find a suitable audio stream to decode");
        goto cleanup;
    }

    printCodecParameters(stream->codecpar);

    // Find the codec to decode this stream
    codec = avcodec_find_decoder(stream->codecpar->codec_id);
    if (!codec){
        LOGE("Could not find codec with ID: %d", stream->codecpar->codec_id);
        goto cleanup;
    }

    codecCtx = avcodec_alloc_context3(codec);
    if (!codecCtx) {
        LOGE("Failed to allocate codec context");
        goto cleanup;
    }

    // Copy the codec parameters into the context
    if (avcodec_parameters_to_context(codecCtx, stream->codecpar) < 0){
        LOGE("Failed to copy codec parameters to codec context");
        goto cleanup;
    }

    // Open the codec
    if (avcodec_open2(codecCtx, codec, nullptr) < 0){
        LOGE("Could not open codec");
        goto cleanup;
    }

    // prepare resampler
    outChannelLayout = (1 << mTargetProperties.channelCount) - 1;
    LOGD("Channel layout %d", outChannelLayout);

    swr = swr_alloc();
    av_opt_set_int(swr, "in_channel_count", stream->codecpar->channels, 0);
    av_opt_set_int(swr, "out_channel_count", mTargetProperties.channelCount, 0);
    av_opt_set_int(swr, "in_channel_layout", stream->codecpar->channel_layout, 0);
    av_opt_set_int(swr, "out_channel_layout", outChannelLayout, 0);
    av_opt_set_int(swr, "in_sample_rate", stream->codecpar->sample_rate, 0);
    av_opt_set_int(swr, "out_sample_rate", mTargetProperties.sampleRate, 0);
    av_opt_set_int(swr, "in_sample_fmt", stream->codecpar->format, 0);
    av_opt_set_sample_fmt(swr, "out_sample_fmt", AV_SAMPLE_FMT_FLT, 0);
    av_opt_set_int(swr, "force_resampling", 1, 0);

    // Check that resampler has been inited
    result = swr_init(swr);
    if (result != 0){
        LOGE("swr_init failed. Error: %s", av_err2str(result));
        goto cleanup;
    };
    if (!swr_is_initialized(swr)) {
        LOGE("swr_is_initialized is false\n");
        goto cleanup;
    }

    // Prepare to read data
    av_init_packet(&avPacket);
    decodedFrame = av_frame_alloc(); // Stores raw audio data
    bytesPerSample = av_get_bytes_per_sample((AVSampleFormat)stream->codecpar->format);

    LOGD("Bytes per sample %d", bytesPerSample);

    LOGD("DECODE START");

    // While there is more data to read, read it into the avPacket
    while (av_read_frame(formatCtx, &avPacket) == 0){

        if (avPacket.stream_index == stream->index && avPacket.size > 0) {

            // Pass our compressed data into the codec
            result = avcodec_send_packet(codecCtx, &avPacket);
            if (result != 0) {
                LOGE("avcodec_send_packet error: %s", av_err2str(result));
                goto cleanup;
            }

            // Retrieve our raw data from the codec
            result = avcodec_receive_frame(codecCtx, decodedFrame);
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
    LOGD("DECODE END");

    returnValue = bytesWritten;

    cleanup:
    if (fp) {
        fclose(fp);
    }
    if (ctx) {
        if (ctx->buffer) {
            av_free(ctx->buffer);
        }
        avio_context_free(&ctx);
    }
    if (formatCtx) {
        avformat_free_context(formatCtx);
    }
    if (codecCtx) {
        avcodec_free_context(&codecCtx);
    }
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