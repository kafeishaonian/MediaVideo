package com.example.video.mediacodec.base;

import android.media.MediaCodec;
import android.media.MediaFormat;

import com.example.video.mediacodec.audio.AudioConfiguration;

import java.nio.ByteBuffer;

/**
 * author: hongming.wei
 * data: 2024/7/8
 */
public abstract class BaseAudioCodec {

    private MediaCodec mMediaCodec;

    protected static final String TAG = BaseAudioCodec.class.getSimpleName();

    private long mPts = 0;

    private long prevOutputPTSUs = 0;

    private AudioConfiguration mAudioConfiguration;

    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();


    public BaseAudioCodec(AudioConfiguration audioConfiguration) {
        mAudioConfiguration = audioConfiguration;
    }


    protected abstract void onAudioData(ByteBuffer buffer, MediaCodec.BufferInfo bufferInfo);

    protected abstract void onAudioOutFormat(MediaFormat outputFormat);

    public void prepareCoder() {
        mPts = 0;
        mMediaCodec = AudioMediaCodec.getAudioMediaCodec(mAudioConfiguration);
        if (mMediaCodec != null) {
            mMediaCodec.start();
        }
    }

    public synchronized void enqueueCodec(byte[] input) {
        if (mMediaCodec == null) {
            return;
        }
        int inputBufferIndex = mMediaCodec.dequeueInputBuffer(12000);

        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = mMediaCodec.getInputBuffer(inputBufferIndex);
            inputBuffer.clear();
            inputBuffer.put(input);
            mMediaCodec.queueInputBuffer(inputBufferIndex, 0, input.length, 0, 0);
        }

        int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(mBufferInfo, 12000);
        if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            onAudioOutFormat(mMediaCodec.getOutputFormat());
        }

        while (outputBufferIndex >= 0) {
            ByteBuffer outputBuffer = mMediaCodec.getOutputBuffer(outputBufferIndex);

            if (mPts == 0) {
                mPts = System.nanoTime() / 1000;
            }

            mBufferInfo.presentationTimeUs = System.nanoTime() / 1000 - mPts;

            onAudioData(outputBuffer, mBufferInfo);
            prevOutputPTSUs = mBufferInfo.presentationTimeUs;
            mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
            outputBufferIndex = mMediaCodec.dequeueOutputBuffer(mBufferInfo, 0);
        }
    }


    public synchronized void stop() {
        if (mMediaCodec != null) {
            mMediaCodec.stop();
            mMediaCodec.release();
            mMediaCodec = null;
        }
    }

    public MediaFormat getOutputFormat() {
        return mMediaCodec.getOutputFormat();
    }

    protected long getPTSUs() {
        long result = System.nanoTime() / 1000;
        if (result < prevOutputPTSUs) {
            result = prevOutputPTSUs;
        }
        return result;
    }

}
