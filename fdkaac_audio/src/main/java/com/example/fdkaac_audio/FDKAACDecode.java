package com.example.fdkaac_audio;

/**
 * author: hongming.wei
 * data: 2024/7/1
 */
public class FDKAACDecode{


    static {
        System.loadLibrary("fdkaac_audio");
    }


    public native int initWithADFormat();


    public native int initWithRAWFormat(byte[] specInfo, byte[] size);

    public native byte[] decode(byte[] byteArray, int length);


    public native void destroy();
}
