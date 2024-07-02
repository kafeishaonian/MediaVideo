package com.example.fdkaac_audio;

/**
 * author: hongming.wei
 * data: 2024/7/1
 */
public class FDKAACEncode {

    static {
        System.loadLibrary("fdkaac_audio");
    }

    public native int init(int bitRate, int channel, int sampleRate);

    public native void encode(byte[] byteArray, int bufferSize);

    public native void destroy();
}
