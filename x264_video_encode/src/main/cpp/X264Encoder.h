//
// Created by MOMO on 2024/7/5.
//

#ifndef MEDIAVIDEO_X264ENCODER_H
#define MEDIAVIDEO_X264ENCODER_H


#include <stdint.h>
#include <x264.h>
#include <malloc.h>
#include <string>
#include <android/log.h>
#include <sys/time.h>


static enum {
    YUV420p = 1,
    YUV420sp = 2
};

typedef struct {
    uint8_t *data = 0;
    uint32_t type = YUV420p;
} AVPacket;


/**
 * 资料参考
 * @see 雷神 x264编码： https://blog.csdn.net/leixiaohua1020/article/details/42078645
 * @see https://wangpengcheng.github.io/2019/04/19/libx264_learn_note/
 */
class X264Encoder {

protected:
    int mFps, mWidth, mHeight, mVideoBitRate, mYSize, mUVSize, mCsp;
    /**
     * x264 编码器
     */
    x264_t *pX264Codec = 0;
    /**
     * 存储压缩编码前的像素数据。
     */
    x264_picture_t *pIc_in = 0;


public:
    X264Encoder();
    ~X264Encoder();

    void init(const char *outH264Path, int width, int height, int videoBitRate, int frameRate);

    /**
     * 编码的 YUV 数据。
     * @param packet
     */
    void encode(AVPacket *packet);

    /**
     *
     * @param sps 编码的第一帧
     * @param pps 编码的第二帧
     * @param sps_len  sps 长度
     * @param pps_len  pps 长度
     */
     void sendSpsPps(uint8_t *sps, uint8_t *pps, int sps_len, int pps_len);

    /**
     *
     * @param type  type == NAL_SLICE_IDR 是否是关键帧
     * @param payload 编码的帧
     * @param i_payload
     * @param timestamp
     */
     void sendFrame(int type, uint8_t *payload, int i_payload, long timestamp);

     void destroy();

private:
    long long currentTimeMills();



};


#endif //MEDIAVIDEO_X264ENCODER_H
