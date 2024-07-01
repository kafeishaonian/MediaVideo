package com.example.video.audio;

/**
 * author: hongming.wei
 * data: 2024/7/1
 */
public class AudioSLESNativeLib {


    static {
        System.loadLibrary("video");
    }

    private native static void nativePlayPcm(String pcmPath);
    private native static void nativeStopPcm();

}
