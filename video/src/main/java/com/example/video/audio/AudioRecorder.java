package com.example.video.audio;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * author: hongming.wei
 * data: 2024/6/28
 */
public class AudioRecorder {

    private static final String TAG = AudioRecorder.class.getSimpleName();

    //默认录音源
    private static final int DEFAULT_SOURCE = MediaRecorder.AudioSource.MIC;
    //采样率
    private static final int DEFAULT_SAMPLE_RATE = 44100;
    //通道
    private static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    //采样格式
    private static final int DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecord mAudioRecord;
    private int mMinBufferSize = 0;
    private Thread mCaptureThread;
    private boolean mIsCaptureStarted = false;
    private volatile boolean mIsLoopExit = false;

    private Context mContext;

    public AudioRecorder(Context context) {
        mContext = context;
    }

    private OnAudioFrameCapturedListener mAudioFrameCapturedListener;


    public boolean isCaptureStarted() {
        return mIsCaptureStarted;
    }

    public boolean startCapture() {
        return startCapture(DEFAULT_SOURCE, DEFAULT_SAMPLE_RATE, DEFAULT_CHANNEL_CONFIG,
                DEFAULT_AUDIO_FORMAT);
    }

    public boolean startCapture(int audioSource, int sampleRateInHz, int channelConfig, int audioFormat) {
        if (mIsCaptureStarted) {
            return false;
        }

        mMinBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        if (mMinBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            return false;
        }

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        mAudioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, mMinBufferSize);
        if (mAudioRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
            return false;
        }

        mAudioRecord.startRecording();

        mIsLoopExit = false;
        mCaptureThread = new Thread(new AudioCaptureRunnable());
        mCaptureThread.start();
        mIsCaptureStarted = true;

        return true;
    }


    public void stopCapture() {
        if (!mIsCaptureStarted) {
            return;
        }

        mIsLoopExit = true;

        try {
            mCaptureThread.interrupt();
            mCaptureThread.join(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            mAudioRecord.stop();
        }

        mAudioRecord.release();

        mIsCaptureStarted = false;
        mAudioFrameCapturedListener = null;

    }



    private class AudioCaptureRunnable implements Runnable {

        @Override
        public void run() {
            while (!mIsLoopExit) {
                byte[] buffer = new byte[mMinBufferSize];

                int ret = mAudioRecord.read(buffer, 0, mMinBufferSize);
                if (ret == AudioRecord.ERROR_INVALID_OPERATION) {
                    Log.e(TAG , "Error ERROR_INVALID_OPERATION");
                } else if (ret == AudioRecord.ERROR_BAD_VALUE) {
                    Log.e(TAG , "Error ERROR_BAD_VALUE");
                } else {
//                    if (mAudioFrameCapturedListener != null) {
//                        mAudioFrameCapturedListener.onAudioFrameCaptured(buffer);
//                    }
                    try {
                        String filePath = Environment.getExternalStorageDirectory() + "/_test.pcm";
                        File file = new File(filePath);
                        FileOutputStream fos = null;
                        if (!file.exists()) {
                            file.createNewFile();//如果文件不存在，就创建该文件
                            fos = new FileOutputStream(file);//首次写入获取
                        } else {
                            //如果文件已存在，那么就在文件末尾追加写入
                            fos = new FileOutputStream(file, true);//这里构造方法多了一个参数true,表示在文件末尾追加写入
                        }
                        fos.write(buffer);
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }


    public void setOnAudioFrameCapturedListener(OnAudioFrameCapturedListener listener) {
        mAudioFrameCapturedListener = listener;
    }

    public interface OnAudioFrameCapturedListener {
        public void onAudioFrameCaptured(byte[] audioData);
    }
}
