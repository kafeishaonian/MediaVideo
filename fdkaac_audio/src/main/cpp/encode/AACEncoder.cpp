//
// Created by MOMO on 2024/7/1.
//

#include "AACEncoder.h"
#include <iostream>
#include <dirent.h>
#include <unistd.h>
#include <stdint.h>
#include <assert.h>
#include <stdlib.h>


AACEncoder::AACEncoder() {
    isFlagGlobalHeader = true;
}

AACEncoder::~AACEncoder() {

}

#define ADTS_HEADER_SIZE 7

typedef struct PutBitContext {
    uint32_t bit_buf;
    int bit_left;
    uint8_t *buf, *buf_ptr, *buf_end;
    int size_in_bits;
} PutBitContext;

#ifndef AV_WB32
#   define AV_WB32(p, darg) do {                \
        unsigned d = (darg);                    \
        ((uint8_t*)(p))[3] = (d);               \
        ((uint8_t*)(p))[2] = (d)>>8;            \
        ((uint8_t*)(p))[1] = (d)>>16;           \
        ((uint8_t*)(p))[0] = (d)>>24;           \
    } while(0)
#endif

/**
 * Initialize the PutBitContext s.
 *
 * @param buffer the buffer where to put bits
 * @param buffer_size the size in bytes of buffer
 */
static inline void init_put_bits(PutBitContext *s, uint8_t *buffer,
                                 int buffer_size)
{
    if (buffer_size < 0) {
        buffer_size = 0;
        buffer      = NULL;
    }

    s->size_in_bits = 8 * buffer_size;
    s->buf          = buffer;
    s->buf_end      = s->buf + buffer_size;
    s->buf_ptr      = s->buf;
    s->bit_left     = 32;
    s->bit_buf      = 0;
}
/**
 * Pad the end of the output stream with zeros.
 */
static inline void flush_put_bits(PutBitContext *s)
{
    if (s->bit_left < 32)
        s->bit_buf <<= s->bit_left;
    while (s->bit_left < 32) {
        *s->buf_ptr++ = s->bit_buf >> 24;
        s->bit_buf  <<= 8;
        s->bit_left  += 8;
    }
    s->bit_left = 32;
    s->bit_buf  = 0;
}

static inline void put_bits(PutBitContext *s, int n, unsigned int value)
{
    unsigned int bit_buf;
    int bit_left;

    assert(n <= 31 && value < (1U << n));

    bit_buf  = s->bit_buf;
    bit_left = s->bit_left;

    /* XXX: optimize */
    if (n < bit_left) {
        bit_buf     = (bit_buf << n) | value;
        bit_left   -= n;
    } else {
        bit_buf   <<= bit_left;
        bit_buf    |= value >> (n - bit_left);
        assert(s->buf_ptr+3<s->buf_end);
        AV_WB32(s->buf_ptr, bit_buf);
        s->buf_ptr += 4;
        bit_left   += 32 - n;
        bit_buf     = value;
    }

    s->bit_buf  = bit_buf;
    s->bit_left = bit_left;
}



static const char *fdkaac_error(AACENC_ERROR error) {
    switch (error) {
        case AACENC_OK:
            return "No error";
        case AACENC_INVALID_HANDLE:
            return "invalid handle";
        case AACENC_MEMORY_ERROR:
            return "Memory allocation error";
        case AACENC_UNSUPPORTED_PARAMETER:
            return "Unsupported parameter";
        case AACENC_INVALID_CONFIG:
            return "Invalid config";
        case AACENC_INIT_ERROR:
            return "Initialization error";
        case AACENC_INIT_AAC_ERROR:
            return "AAC library initialization error";
        case AACENC_INIT_SBR_ERROR:
            return "SBR library initialization error";
        case AACENC_INIT_TP_ERROR:
            return "Transport library initialization error";
        case AACENC_INIT_META_ERROR:
            return "Metadata library initialization error";
        case AACENC_ENCODE_ERROR:
            return "Encoding error";
        case AACENC_ENCODE_EOF:
            return "End of file";
        default:
            return "Unknown error";
    }
}

int AACEncoder::init(AACProfile profile, int sampleRate, int channels, int bitRate) {
    AACENC_ERROR ret;
    // 打开编码器,如果非常需要节省内存则可以调整encModules
    if ((ret = aacEncOpen(&mAacencoder, 0x0, channels)) != AACENC_OK) {
        LOGE("Unable to open fdkaac encoder, ret = 0x%x, error is %s\n", ret, fdkaac_error(ret));
        return NULL;
    }

    /**编码规格
     *  - 2: LC. MPEG-4 AAC Low Complexity
     *  - 5: HE-AAC. MPEG-4 AAC Low Complexity with Spectral Band Replication
     *  - 29: HE-AAC v2. AAC Low Complexity with Spectral Band Replication and Parametric Stereo(声道数必须是2)
     */
    // 设置AAC标准格式
    if ((ret = aacEncoder_SetParam(mAacencoder, AACENC_AOT, profile)) != AACENC_OK) {
        LOGE("Unable to set the AACENC_AOT, ret = 0x%x, error is %s\n", ret, fdkaac_error(ret));
        destroy();
        return false;
    }

    // 设置采样率
    if ((ret = aacEncoder_SetParam(mAacencoder, AACENC_SAMPLERATE, sampleRate)) != AACENC_OK) {
        LOGE("Unable to set the SAMPLERATE, ret = 0x%x, error is %s\n", ret, fdkaac_error(ret));
        destroy();
        return false;
    }
    // 设置通道数
    if ((ret = aacEncoder_SetParam(mAacencoder, AACENC_CHANNELMODE, channels)) != AACENC_OK) {
        LOGE("Unable to set the AACENC_CHANNELMODE, ret = 0x%x, error is %s\n", ret,
             fdkaac_error(ret));
        destroy();
        return false;
    }

    // 设置比特率
    if ((ret = aacEncoder_SetParam(mAacencoder, AACENC_BITRATE, bitRate)) != AACENC_OK) {
        LOGE("Unable to set the AACENC_BITRATE, ret = 0x%x, error is %s\n", ret, fdkaac_error(ret));
        destroy();
        return false;
    }

    /**
     *  设置输出格式，是AAC的裸流还是ADTS的流
     *  0-raw 2-adts
     */
    //已经封装好了 ADTS 流
     int encode_mode = TT_MP4_ADTS;
    //     encode_mode = TT_MP4_LATM_MCP1; //LATM 流
    if (isFlagGlobalHeader) {
        //没有任何格式的封装
        encode_mode = TT_MP4_RAW;
    }

    // 设置编码出来的数据带aac adts头
    if ((ret = aacEncoder_SetParam(mAacencoder, AACENC_TRANSMUX, encode_mode)) != AACENC_OK) {
        LOGE("Unable to set the ADTS AACENC_TRANSMUX, ret = 0x%x, error is %s\n", ret, fdkaac_error(ret));
        destroy();
        return false;
    }

    // 初始化编码器
    if ((ret = aacEncEncode(mAacencoder, NULL, NULL, NULL, NULL)) != AACENC_OK) {
        LOGE("Unable to initialize the aacEncEncode, ret = 0x%x, error is %s\n", ret, fdkaac_error(ret));
        destroy();
        return false;
    }

    // 获取编码器信息
    if ((ret == aacEncInfo(mAacencoder, &mEncInfo)) != AACENC_OK) {
        LOGE("Unable to get the aacEncInfo info, ret = 0x%x, error is %s\n", ret, fdkaac_error(ret));
        destroy();
        return false;
    }

    // 计算pcm帧长
    //frameLength是每帧每个channel的采样点数
    mInputSizeFixed = channels * 2 * mEncInfo.frameLength;
    mInBufferCursor = 0;
    mInBuffer = new uint8_t[mInputSizeFixed];
    memset(mInBuffer, 0, mInputSizeFixed);
    //如果是编码裸流，可以取出编码器的SpecInfo来
//    unsigned char *audioSpecInfo = mEncInfo.confBuf;
    if (0 != access("/sdcard/avsample/", 0)) {
        //TODO -- 没有找到 NDK 中怎么创建文件
//        mkdir("/sdcard/avsample/", 777);
        LOGE("not find Dir: /sdcard/avsample/");
        return 0;
    }

    aacFile = fopen("/sdcard/avsample/fdkaac_encode.aac", "wb+");
    //    fwrite(audioSpecInfo, 1, mEncInfo.confSize, audioSpecInfoFile);
//    fclose(audioSpecInfoFile);
    LOGI("固定每次编码长度为: mInputSizeFixed = %d", mInputSizeFixed);
    return 1;
}


int AACEncoder::encode(Byte *pData, int dataByteSize, char **outBuffer) {
    int pDataCursor = 0;

    *outBuffer = new char[dataByteSize];
    int packetSize = 0;

    while (dataByteSize > 0) {
        int cpySize = 0;
        if (mInBufferCursor + dataByteSize >= mInputSizeFixed) {
            cpySize = mInputSizeFixed - mInBufferCursor;
            memcpy(mInBuffer + mInBufferCursor, pData + pDataCursor, cpySize);
            int aacPktSize = this->fdkEncodeAudio();
            if (aacPktSize > 0) {
                this->writeAACPacketToFile(mAacOutBuf, aacPktSize);
                memcpy(*outBuffer + packetSize, mAacOutBuf, aacPktSize);
                packetSize += aacPktSize;
            }
            mInBufferCursor = 0;
            memset(mInBuffer, 0, mInputSizeFixed);
        } else {
            cpySize = dataByteSize;
            memcpy(mInBuffer + mInBufferCursor, pData + pDataCursor, cpySize);
            mInBufferCursor += cpySize;
        }

        dataByteSize -= cpySize;
        pDataCursor += cpySize;
    }

    return packetSize;
}

/**
 * 进行编码
 * @return 返回编码的大小
 */
int AACEncoder::fdkEncodeAudio() {
    AACENC_BufDesc in_buf = {0}, out_buf = {0};
    AACENC_InArgs in_args = {0};
    AACENC_OutArgs out_args = {0};
    int in_identifier = IN_AUDIO_DATA;
    int in_elem_size = 2;

    in_args.numInSamples = mInputSizeFixed / 2;  //所有通道的加起来的采样点数，每个采样点是2个字节所以/2
    in_buf.numBufs = 1;
    in_buf.bufs = (void **) (&mInBuffer);  //pData为pcm数据指针
    in_buf.bufferIdentifiers = &in_identifier;
    in_buf.bufSizes = &mInputSizeFixed;
    in_buf.bufElSizes = &in_elem_size;

    int out_identifier = OUT_BITSTREAM_DATA;
    void *out_ptr = mAacOutBuf;
    int out_size = sizeof(mAacOutBuf);
    int out_elem_size = 1;
    out_buf.numBufs = 1;
    out_buf.bufs = &out_ptr;
    out_buf.bufferIdentifiers = &out_identifier;
    out_buf.bufSizes = &out_size;
    out_buf.bufElSizes = &out_elem_size;

    if ((aacEncEncode(mAacencoder, &in_buf, &out_buf, &in_args, &out_args)) != AACENC_OK) {
        LOGI("Encoding aac failed\n");
        return 0;
    }
    if (out_args.numOutBytes == 0)
        return 0;
    //编码后的aac数据存在outbuf中，大小为out_args.numOutBytes
    return out_args.numOutBytes;
}

void AACEncoder::writeAACPacketToFile(uint8_t *data, int datalen) {
    LOGI("After One Encode Size is : %d", datalen);
    uint8_t *buffer = data;
    if (isFlagGlobalHeader) {//写入 ADTS
        datalen += 7;
        uint8_t *_buffer = new uint8_t[datalen];
        memset(_buffer, 0, datalen);
        memcpy(_buffer + 7, data, datalen - 7);
        addADTS2Packet(_buffer, datalen);
        fwrite(_buffer, sizeof(uint8_t), datalen, aacFile);
        delete[]_buffer;
        return;
    }
    if (aacFile)//完整的流
        fwrite(buffer, sizeof(uint8_t), datalen, aacFile);

}


/**
 *
 * ADTS packet 信息可以参考：https://www.jianshu.com/p/5c770a22e8f8
 *
 * 参考ffmpeg源码中 添加 ADTS 头部
 *
 * @param packetLen 该帧的长度，包括header的长度
 * @param profile  0-Main profile, 1-AAC LC，2-SSR
 * @param freqIdx    采样率
                    0: 96000 Hz
                    1: 88200 Hz
                    2: 64000 Hz
                    3: 48000 Hz
                    4: 44100 Hz
                    5: 32000 Hz
                    6: 24000 Hz
                    7: 22050 Hz
                    8: 16000 Hz
                    9: 12000 Hz
                    10: 11025 Hz
                    11: 8000 Hz
                    12: 7350 Hz
                    13: Reserved
                    14: Reserved
                    15: frequency is written explictly
* @param chanCfg    通道
                    2：L+R
                    3：C+L+R
*/

void AACEncoder::addADTS2Packet(uint8_t *packet, int packetLen) {
    int profile = 1;
    int freqIdx = 4; // 44.1KHz
    int chanCfg = 1; // 通道

    PutBitContext pb;
    init_put_bits(&pb, packet, ADTS_HEADER_SIZE);
    /* adts_fixed_header */
    put_bits(&pb, 12, 0xfff);   /* syncword ：代表一个ADTS帧的开始, 用于同步，解码器可通过0xFFF确定每个ADTS的开始位置*/
    put_bits(&pb, 1, 0);        /* ID ：MPEG Version: 0 for MPEG-4, 1 for MPEG-2*/
    put_bits(&pb, 2, 0);        /* layer always: '00'*/
    put_bits(&pb, 1, 1);        /* protection_absent */
    put_bits(&pb, 2, profile); /* profile_objecttype 表示使用哪个级别的AAC，如01 Low Complexity(LC) -- AAC LC*/
    put_bits(&pb, 4, freqIdx); //采样率的下标.
    put_bits(&pb, 1, 0);        /* private_bit */
    put_bits(&pb, 3, chanCfg); /* channel_configuration 声道数. 比如2表示立体声双声道.*/
    put_bits(&pb, 1, 0);        /* original_copy */
    put_bits(&pb, 1, 0);        /* home */

    /* adts_variable_header */
    put_bits(&pb, 1, 0);        /* copyright_identification_bit */
    put_bits(&pb, 1, 0);        /* copyright_identification_start */
    put_bits(&pb, 13, packetLen); /* aac_frame_length */
    put_bits(&pb, 11, 0x7ff);   /* adts_buffer_fullness */
    put_bits(&pb, 2, 0);        /* number_of_raw_data_blocks_in_frame */

    flush_put_bits(&pb);
}


void AACEncoder::destroy() {
    LOGI("Enter AACEncoder Destroy");
    if (mAacencoder) {
        aacEncClose(&mAacencoder);
    }
}