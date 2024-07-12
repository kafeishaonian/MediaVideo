package com.example.video.mediacodec.audio;

import android.media.AudioRecord;
import android.media.MediaFormat;

import com.example.video.mediacodec.base.AudioUtils;

import java.util.Arrays;

/**
 * author: hongming.wei
 * data: 2024/7/5
 */
public class AudioProcessor extends Thread{

    private volatile boolean mPauseFlag = false;
    private volatile boolean mStopFlag = false;
    private volatile boolean mMute = false;

    private AudioRecord mAudioRecord;
    private AudioConfiguration mAudioConfiguration;
    private AACEncoder mAudioEncoder;
    private byte[] mRecordBuffer;
    private int mRecordBufferSize;


    public AudioProcessor(AudioRecord audioRecord, AudioConfiguration audioConfiguration) {
        mAudioRecord = audioRecord;
        mAudioConfiguration = audioConfiguration;

        mRecordBufferSize = AudioUtils.getMinBufferSize(audioConfiguration.getFrequency(), audioConfiguration.getChannelCount(), audioConfiguration.getEncoding());
        mRecordBuffer = new byte[mRecordBufferSize];
        mAudioEncoder = new AACEncoder(mAudioConfiguration);
        mAudioEncoder.prepareCoder();
    }

    /**
     * 设置音频硬编码监听
     */
    public void setAudioHEncodeListener(AACEncoder.OnAudioEncodeListener listener) {
        mAudioEncoder.setOnAudioEncodeListener(listener);
    }

    /**
     * 停止
     */
    public void stopEncode() {
        mStopFlag = true;
        if (mAudioEncoder != null) {
            mAudioEncoder.stop();
            mAudioEncoder = null;
        }
    }

    /**
     * 暂停
     */
    public void pauseEncode(boolean pause) {
        mPauseFlag = pause;
    }

    /**
     * 静音
     */
    public void setMute(boolean mute) {
        mMute = mute;
    }

    public MediaFormat getOutputFormat() {
        return mAudioEncoder.getOutputFormat();
    }


    @Override
    public void run() {
        while (!mStopFlag) {
            while (mPauseFlag) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            int readLen = mAudioRecord.read(mRecordBuffer, 0, mRecordBufferSize);
            if (readLen > 0) {
                if (mMute) {
                    byte clearM = 0;
                    //内部全部是 0 bit
                    Arrays.fill(mRecordBuffer, clearM);
                }
                mAudioEncoder.enqueueCodec(mRecordBuffer);
            }
        }
    }
}
