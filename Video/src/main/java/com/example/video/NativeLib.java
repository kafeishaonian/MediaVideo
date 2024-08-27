package com.example.video;

public class NativeLib {

    // Used to load the 'video' library on application startup.
    static {
        System.loadLibrary("video");
    }

    /**
     * A native method that is implemented by the 'video' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}