//
// Created by asalehin on 10/26/20.
//

#include <libavcodec/codec.h>
#include <libavcodec/avcodec.h>
#include "FFMpegExtractor.h"
#include "../logging_macros.h"
#include "list"

FFMpegExtractor::FFMpegExtractor(const string &filePath, const AudioProperties targetProperties) {
    mFilePath = filePath.c_str();
    mTargetProperties = targetProperties;
}

list<uint8_t>& FFMpegExtractor::read() {
    const AVCodec *codec;
    AVCodecContext *c= nullptr;
    AVCodecParserContext *parser = nullptr;
    int len, ret;
    FILE *f = nullptr;
    uint8_t inbuf[AUDIO_INBUF_SIZE + AV_INPUT_BUFFER_PADDING_SIZE];
    uint8_t *data;
    size_t data_size;
    AVPacket *pkt;
    AVFrame *decoded_frame = nullptr;

    auto it = samples.begin();

    pkt = av_packet_alloc();

    codec = avcodec_find_decoder(AV_CODEC_ID_MP2);
    if (!codec) {
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

    if (avcodec_open2(c, codec, NULL) < 0) {
        LOGE("Could not open codec");
        goto cleanup;
    }

    f = fopen(mFilePath, "rb");
    if (!f) {
        LOGE("Could not open input file");
        goto cleanup;
    }

    data = inbuf;
    data_size = fread(inbuf, 1, AUDIO_INBUF_SIZE, f);

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
            // Call decode
            decode(c, pkt, decoded_frame, samples, it);

        if (data_size < AUDIO_REFILL_THRESH) {
            memmove(inbuf, data, data_size);
            data = inbuf;
            len = fread(data + data_size, 1, AUDIO_INBUF_SIZE - data_size, f);
            if (len > 0) {
                data_size += len;
            }
        }
    }

    pkt->data = nullptr;
    pkt->size = 0;
    decode(c, pkt, decoded_frame, samples, it);

    cleanup:

    if (c) {
        avcodec_free_context(&c);
    }
    if (f) {
        fclose(f);
    }
    if (parser) {
        av_parser_close(parser);
    }
    if (decoded_frame) {
        av_frame_free(&decoded_frame);
    }
    if (pkt) {
        av_packet_free(&pkt);
    }

    return samples;
}

void FFMpegExtractor::decode(AVCodecContext *dec_ctx, AVPacket *pkt, AVFrame *frame, list<uint8_t>& samples, list<uint8_t>::iterator& it) {
    int i, ch;
    int ret, data_size;
    /* send the packet with the compressed data to the decoder */
    ret = avcodec_send_packet(dec_ctx, pkt);
    if (ret < 0) {
        LOGE("Error submitting the packet to the decoder");
        return;
    }
    /* read all the output frames (in general there may be any number of them */
    while (ret >= 0) {
        ret = avcodec_receive_frame(dec_ctx, frame);
        if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF)
            return;
        else if (ret < 0) {
            LOGE("Error during decoding");
            return;
        }
        data_size = av_get_bytes_per_sample(dec_ctx->sample_fmt);
        if (data_size < 0) {
            /* This should not occur, checking just for paranoia */
            LOGE("Failed to calculate data size");
            return;
        }
        for (i = 0; i < frame->nb_samples; i++) {
            for (ch = 0; ch < dec_ctx->channels; ch++) {
                for (int k = 0; k < data_size; k++) {
                    samples.insert(it, *(frame->data[ch] + data_size * i + k));
                }
            }
        }
    }
}
