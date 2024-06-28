package com.example.video;

public class NativeLib {

    // Used to load the 'video' library on application startup.
    static {
        System.loadLibrary("video");
    }

    private native void _setDataSource(String path);

    public native void _prepareAsync();

    private native void _start();

    private native void _stop();

    private native void _pause();

    private native boolean _isPlaying();

    private native void _seekTo(long seek);

    private native long _getCurrentPosition();

    private native long _getDuration();

    private native void _release();

    private native void _setVolume(float volume);

    private static native void _init();
}