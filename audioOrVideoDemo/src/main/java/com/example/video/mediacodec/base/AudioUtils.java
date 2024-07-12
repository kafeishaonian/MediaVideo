package com.example.video.mediacodec.base;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.nio.ByteBuffer;

/**
 * author: hongming.wei
 * data: 2024/7/8
 */
public class AudioUtils {

    private static final String TAG = AudioUtils.class.getSimpleName();

    /**
     * 录音对象
     *
     * @see AudioRecord
     */
    private static AudioRecord mAudioRecord;

    /**
     * 声音通道
     * 默认 单声道 个
     *
     * @see AudioFormat.CHANNEL_IN_MONO 单声道
     * @see AudioFormat.CHANNEL_IN_STEREO 立体声
     */
    public static int AUDIO_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;

    /**
     * 采样率 如果 AudioRecord 初始化失败，那么可以降低为 16000 ，或者检查权限是否开启
     * 默认 44100
     */
    public static int SAMPLE_RATE_IN_HZ = 44100;

    /**
     * 采样格式
     * 默认 16bit 存储
     *
     * @see AudioFormat.ENCODING_PCM_16BIT 兼容大部分手机
     */
    public static int AUDIO_FROMAT = AudioFormat.ENCODING_PCM_16BIT;

    /**
     * 录音源
     *
     * @see MediaRecorder.AudioSource.MIC 手机麦克风
     * @see MediaRecorder.AudioSource.VOICE_RECOGNITION 用于语音识别，等同于默认
     * @see MediaRecorder.AudioSource.VOICE_COMMUNICATION 用于 VOIP 应用
     */
    public static int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;

    /**
     * 配置内部音频缓冲区的大小，由于不同厂商会有不同的实现。那么我们可以通过一个静态函数来 getMinBufferSize 来定义
     *
     * @see AudioRecord.getMinBufferSize
     */
    private static int mBufferSizeInBytes = 0;


    /**
     * 获取音频缓冲区大小
     */
    public static int getMinBufferSize(int sampleRateInHz, int channelConfig, int audioFormat) {
        return AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
    }


    /**
     * 拿到 AudioRecord 对象
     */
    public static boolean initAudioRecord(Context context, int audioSource, int sampleRateInHz, int channelConfig, int audioFormat) {
        //如果 AudioRecord 不为 null  那么直接销毁
        if (mAudioRecord != null) {
            mAudioRecord.release();
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "检查是否打开录音权限？");
            return false;
        }
        try {
            //得到录音缓冲大小
            mBufferSizeInBytes = getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
            mAudioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, mBufferSizeInBytes);
        } catch (Exception error) {
            Log.e(TAG, "AudioRecord init error :${error.message}");
            return false;
        }

        //如果初始化失败那么降低采样率
        if (mAudioRecord == null || mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "检查音频源是否为占用，或者是否打开录音权限？");
            return false;
        }
        return true;
    }

    public static AudioRecord getAudioRecord() {
        return mAudioRecord;
    }


    /**
     * 开始录制
     */
    public static void startRecord() {
        if (mAudioRecord != null && mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
            mAudioRecord.startRecording();
        }
    }

    /**
     * 开始录制
     */
    public static void stopRecord() {
        if (mAudioRecord != null && mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
            mAudioRecord.stop();
        }
    }

    /**
     * 释放资源
     */
    public static void releaseRecord() {
        if (mAudioRecord != null) {
            mAudioRecord.release();
            mAudioRecord = null;
        }
    }

    /**
     * 读取音频数据
     */
    public static int read(int bufferSize, int offsetInBytes, byte[] buffer) {
        int ret = 0;
        if (mAudioRecord != null) {
            ret = mAudioRecord.read(buffer, offsetInBytes, bufferSize);
        }
        return ret;
    }

    /**
     * 读取音频数据
     */
    public static int read(int bufferSize, int offsetInBytes, short[] shorts) {
        int ret = 0;
        if (mAudioRecord != null) {
            ret = mAudioRecord.read(shorts, offsetInBytes, bufferSize);
        }
        return ret;
    }

    /**
     * 读取音频数据
     */
    public static int read(int bufferSize, ByteBuffer buffer) {
        int ret = 0;
        if (mAudioRecord != null) {
            mAudioRecord.read(buffer, bufferSize);
        }
        return ret;
    }

    /**
     * 读取音频数据
     */
    public static int read(int bufferSize, byte[] buffer) {
        int ret = 0;
        if (mAudioRecord != null) {
            ret = mAudioRecord.read(buffer, 0, bufferSize);
        }
        return ret;
    }


    /**
     * 拿到缓冲大小
     */
    public static int getBufferSize() {
        return mBufferSizeInBytes;
    }
}
