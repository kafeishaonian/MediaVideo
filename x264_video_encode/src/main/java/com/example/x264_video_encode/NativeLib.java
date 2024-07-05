package com.example.x264_video_encode;

public class NativeLib {

    // Used to load the 'x264_video_encode' library on application startup.
    static {
        System.loadLibrary("x264_video_encode");
    }

    public native void init(String h264Path, int width, int height, int videoBitRate, int frameRate) ;

    public native void encode(byte[] byteArray, int yuvType) ;

    public native void destroy() ;
}