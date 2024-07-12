package com.example.video.mediacodec.base;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;

import com.example.video.mediacodec.audio.AudioConfiguration;

import java.nio.ByteBuffer;

/**
 * author: hongming.wei
 * data: 2024/7/8
 */
public class AudioMediaCodec {

    public static MediaCodecInfo selectCodec(String mimeType) {
        MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
        MediaCodecInfo[] mediaCodecInfos = mediaCodecList.getCodecInfos();
        int numCodecs = mediaCodecInfos.length;

        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = mediaCodecInfos[i];
            if (!codecInfo.isEncoder()) {
                continue;
            }
            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equals(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }


    public static MediaCodec getAudioMediaCodec(AudioConfiguration configuration) {
        //数据类型  "audio/mp4a-latm"
        MediaFormat mediaFormat = MediaFormat.createAudioFormat(configuration.getMime(), configuration.getFrequency(), configuration.getChannelCount());
        if (configuration.getMime().equals(AudioConfiguration.DEFAULT_MIME)) {
            //用来标记aac的类型
            mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, configuration.getAacProfile());
        }
        //比特率
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, configuration.getMaxBps() * 1024);
        //采样率
        mediaFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, configuration.getFrequency());
        //缓冲区大小
        int maxInputSize = AudioUtils.getMinBufferSize(configuration.getFrequency(), configuration.getChannelCount(), configuration.getEncoding());
        //最大的缓冲区
        mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, maxInputSize);
        //通道数量
        mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, configuration.getChannelCount());
        MediaCodec mediaCodec = null;
        try {
            if (configuration.getCodeType() == AudioConfiguration.CodeType.ENCODE) {
                mediaCodec = MediaCodec.createEncoderByType(configuration.getMime());
                mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            } else if (configuration.getCodeType() == AudioConfiguration.CodeType.DECODE) {
                mediaFormat.setInteger(MediaFormat.KEY_IS_ADTS, configuration.getAdts());

                byte[] data = new byte[]{0x11, (byte) 0x90};
                ByteBuffer csd_0 = ByteBuffer.wrap(data);

                mediaFormat.setByteBuffer("csd-0", csd_0);
                mediaCodec = MediaCodec.createDecoderByType(configuration.getMime());
                mediaCodec.configure(mediaFormat, null, null, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (mediaCodec != null) {
                mediaCodec.stop();
                mediaCodec.release();
                mediaCodec = null;
            }
        }
        return mediaCodec;
    }

}
