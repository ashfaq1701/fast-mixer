//
// Created by asalehin on 10/26/20.
//

#include "FFMpegExtractor.h"
#include "../logging_macros.h"
#include "list"
#include "../utils/Utils.h"

FFMpegExtractor::FFMpegExtractor(const string &filePath, const AudioProperties targetProperties) {
    mFilePath = filePath.c_str();
    mTargetProperties = targetProperties;
}

int64_t FFMpegExtractor::read(uint8_t *targetData) {
    const AVCodec *codec;
    AVCodecContext *c= nullptr;
    AVCodecParserContext *parser = nullptr;
    SwrContext *swr_ctx = nullptr;
    int len, ret;
    FILE *f = nullptr;

    uint8_t *dataPtr = targetData;

    int64_t totalDecoded = 0;

    audioInbufSize = getSizeOfFile(mFilePath);

    uint8_t inbuf[audioInbufSize + AV_INPUT_BUFFER_PADDING_SIZE];
    uint8_t *data;
    size_t data_size;
    AVPacket *pkt;
    AVFrame *decoded_frame = nullptr;
    AVStream *stream = nullptr;
    AVFormatContext* format = nullptr;

    int streamId;

    int32_t outChannelLayout = 0;

    pkt = av_packet_alloc();

    format = avformat_alloc_context();
    if (avformat_open_input(&format, mFilePath, nullptr, nullptr) != 0) {
        LOGE("Could not detect file format");
        goto cleanup;
    }

    streamId = av_find_best_stream(format, AVMEDIA_TYPE_AUDIO, -1, -1, nullptr, 0);
    if (streamId < 0) {
        LOGE("Could not find Audio Stream");
        goto cleanup;
    }

    stream = format->streams[streamId];

    if (stream == nullptr || stream->codecpar == nullptr){
        LOGE("Could not find a suitable audio stream to decode");
        goto cleanup;
    }

    codec = format->audio_codec;
    if (!codec || (codec->id != AV_CODEC_ID_NONE)) {
        LOGE("Codec not found");
        goto cleanup;
    }

    parser = av_parser_init(codec->id);
    if (!parser) {
        LOGE("Parser not found");
        goto cleanup;
    }

    c = avcodec_alloc_context3(codec);
    if (!c) {
        LOGE("Could not allocate audio codec context");
        goto cleanup;
    }

    if (avcodec_open2(c, codec, nullptr) < 0) {
        LOGE("Could not open codec");
        goto cleanup;
    }

    f = fopen(mFilePath, "rb");
    if (!f) {
        LOGE("Could not open input file");
        goto cleanup;
    }

    data = inbuf;
    data_size = fread(inbuf, 1, audioInbufSize, f);

    swr_ctx = swr_alloc();
    if (!swr_ctx) {
        LOGE("Could not allocate resampler context");
        goto cleanup;
    }

    outChannelLayout = (1 << mTargetProperties.channelCount) - 1;

    /* set options */
    av_opt_set_int(swr_ctx, "in_channel_count", stream->codecpar->channels, 0);
    av_opt_set_int(swr_ctx, "in_channel_layout", stream->codecpar->channel_layout, 0);
    av_opt_set_int(swr_ctx, "in_sample_rate", stream->codecpar->sample_rate, 0);
    av_opt_set_int(swr_ctx, "in_sample_fmt", stream->codecpar->format, 0);
    av_opt_set_int(swr_ctx, "out_channel_count", mTargetProperties.channelCount, 0);
    av_opt_set_int(swr_ctx, "out_channel_layout", outChannelLayout, 0);
    av_opt_set_int(swr_ctx, "out_sample_rate", mTargetProperties.sampleRate, 0);
    av_opt_set_sample_fmt(swr_ctx, "out_sample_fmt", AV_SAMPLE_FMT_FLT, 0);
    av_opt_set_int(swr_ctx, "force_resampling", 1, 0);

    if (swr_init(swr_ctx) < 0) {
        LOGE("Failed to initialize the resampling context");
        goto cleanup;
    }

    if (!swr_is_initialized(swr_ctx)) {
        LOGE("swr_is_initialized is false");
        goto cleanup;
    }

    while (data_size > 0) {
        if (!decoded_frame) {
            if (!(decoded_frame = av_frame_alloc())) {
                LOGD("Could not allocate audio frame");
                goto cleanup;
            }
        }

        ret = av_parser_parse2(parser, c, &pkt->data, &pkt->size, data, data_size, AV_NOPTS_VALUE, AV_NOPTS_VALUE, 0);

        if (ret < 0) {
            LOGD("Error while parsing");
            goto cleanup;
        }

        data += ret;
        data_size -= ret;

        if (pkt->size)
            totalDecoded += decode(c, pkt, decoded_frame, &dataPtr, swr_ctx);

        if (data_size < AUDIO_REFILL_THRESH) {
            memmove(inbuf, data, data_size);
            data = inbuf;
            len = fread(data + data_size, 1, audioInbufSize - data_size, f);
            if (len > 0) {
                data_size += len;
            }
        }
    }

    pkt->data = nullptr;
    pkt->size = 0;

    totalDecoded += decode(c, pkt, decoded_frame, &dataPtr, swr_ctx);

    cleanup:

    dataPtr = nullptr;
    if (f) {
        fclose(f);
    }
    if (format) {
        avformat_free_context(format);
    }
    if (c) {
        avcodec_free_context(&c);
    }
    if (parser) {
        av_parser_close(parser);
    }
    if (swr_ctx) {
        swr_free(&swr_ctx);
    }
    if (decoded_frame) {
        av_frame_free(&decoded_frame);
    }
    if (pkt) {
        av_packet_free(&pkt);
    }

    return totalDecoded;
}

int64_t FFMpegExtractor::decode(AVCodecContext *dec_ctx, AVPacket *pkt, AVFrame *frame, uint8_t **dataPtr, SwrContext *swr) {
    int ret, data_size, frame_count, dst_nb_samples = 0;

    int64_t bytesToWrite = 0;

    uint8_t **buffer1 = nullptr;
    /* send the packet with the compressed data to the decoder */
    ret = avcodec_send_packet(dec_ctx, pkt);
    if (ret < 0) {
        LOGE("Error submitting the packet to the decoder");
        goto ret;
    }
    /* read all the output frames (in general there may be any number of them */
    while (ret >= 0) {
        ret = avcodec_receive_frame(dec_ctx, frame);
        if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF)
            goto ret;
        else if (ret < 0) {
            LOGE("Error during decoding");
            goto ret;
        }
        data_size = av_get_bytes_per_sample(dec_ctx->sample_fmt);
        if (data_size < 0) {
            /* This should not occur, checking just for paranoia */
            LOGE("Failed to calculate data size");
            goto ret;
        }

        dst_nb_samples = (int32_t) av_rescale_rnd(
                swr_get_delay(swr, dec_ctx->sample_rate) + frame->nb_samples,
                mTargetProperties.sampleRate,
                frame->sample_rate,
                AV_ROUND_UP);

        av_samples_alloc(
                buffer1,
                nullptr,
                mTargetProperties.channelCount,
                dst_nb_samples,
                AV_SAMPLE_FMT_FLT,
                0);

        frame_count = swr_convert(
                swr,
                buffer1,
                dst_nb_samples,
                (const uint8_t **) frame->data,
                frame->nb_samples);

        bytesToWrite = frame_count * sizeof(float) * mTargetProperties.channelCount;

        memcpy(*dataPtr, *buffer1, (size_t)bytesToWrite);

        *dataPtr += bytesToWrite;

        ret:
        if (buffer1)
            av_freep(&buffer1[0]);
        av_freep(&buffer1);
        return bytesToWrite;
    }
}
