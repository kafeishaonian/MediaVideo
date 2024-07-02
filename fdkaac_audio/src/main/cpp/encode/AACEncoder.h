//
// Created by MOMO on 2024/7/1.
//

#ifndef MEDIAVIDEO_AACENCODER_H
#define MEDIAVIDEO_AACENCODER_H

#include <aacenc_lib.h>
#include <zconf.h>

#define LOG_TAG encode_
#include "../common.h"


/**
 * 定义 AAC 编码格式
 * LC-AAC:应用于中高码率场景的编码 （>= 80Kbit/s）
 * HE-AAC:主要应用于中低码率场景的编码 (<= 80kit/s)
 * HE-V2-AAC: 主要应用于低码率场景的编码 (<= 48Kbit/s)
 */

typedef enum {
    LC_AAC = 2,
    HE_AAC = 5,
    LC_V2_AAC = 29,
} AACProfile;

class AACEncoder {

private:
    HANDLE_AACENCODER mAacencoder;
    AACENC_InfoStruct mEncInfo = {0};
    uint8_t *mInBuffer = 0;
    int mInBufferCursor;
    int mInputSizeFixed;
    uint8_t mAacOutBuf[20480];
    //设置编码Header
    bool isFlagGlobalHeader = false;

    FILE *aacFile = 0;

public:
    AACEncoder();

    ~AACEncoder();

    int init(AACProfile profile, int sampleRate, int channels, int bitRate);

    int encode(Byte *pData, int dataByteSize, char **outBuffer);

    void destroy();

    int fdkEncodeAudio();

    void addADTS2Packet(uint8_t *packet, int packetLength);

    void writeAACPacketToFile(uint8_t *string, int i);
};


#endif //MEDIAVIDEO_AACENCODER_H
