//
// Created by MOMO on 2024/6/13.
//

#ifndef MEDIAVIDEO_BASEDECODER_H
#define MEDIAVIDEO_BASEDECODER_H

#include "IDecoder.h"

extern "C" {
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libavutil/frame.h>
#include <libavutil/time.h>
};


class BaseDecoder: public IDecoder {

private:

    //解码信息上下文
    AVFormatContext *mFormatCtx = NULL;

    //解码器
    AVCodec *mCodec = NULL;

    //解码器上下文
    AVCodecContext *mCodecCtx = NULL;

    //待解码包
    AVPacket *mPacket = NULL;

    //最终解码数据
    AVFrame *mFrame = NULL;


    void InitDecoder();


public:
    BaseDecoder();
    ~BaseDecoder();

    void start() override;
    void stop() override;
    void pause() override;
    void isPlaying() override;
};


#endif //MEDIAVIDEO_BASEDECODER_H
