package com.example.video.mediacodec.audio;

import android.media.MediaCodec;
import android.media.MediaFormat;

import com.example.video.mediacodec.base.BaseAudioCodec;

import java.nio.ByteBuffer;

/**
 * author: hongming.wei
 * data: 2024/7/8
 */
public class AACEncoder extends BaseAudioCodec {

    public AACEncoder(AudioConfiguration audioConfiguration) {
        super(audioConfiguration);
    }

    private OnAudioEncodeListener mListener;

    public void setOnAudioEncodeListener(OnAudioEncodeListener listener) {
        mListener = listener;
    }

    @Override
    protected void onAudioData(ByteBuffer buffer, MediaCodec.BufferInfo bufferInfo) {
        if (mListener != null) {
            mListener.onAudioEncode(buffer, bufferInfo);
        }
    }

    @Override
    protected void onAudioOutFormat(MediaFormat outputFormat) {
        if (mListener != null) {
            mListener.onAudioOutFormat(outputFormat);
        }
    }

    public interface OnAudioEncodeListener {
        void onAudioEncode(ByteBuffer buffer, MediaCodec.BufferInfo bufferInfo);
        void onAudioOutFormat(MediaFormat outputFormat);
    }
}
