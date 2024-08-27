package com.example.video.controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;

import androidx.annotation.NonNull;

import com.example.video.utils.ScreenUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * author: hongming.wei
 * data: 2024/7/12
 */
public class CameraController {

    private static final String TAG = CameraController.class.getSimpleName();

    private Context mContext;
    private String mCameraId = null;
    private MediaRecorder mMediaRecorder;

    private String mVideoFilePage;

    private static final int PREVIEW_SIZE_MIN = 720 * 1280;

    private int mWidth;
    private int mHeight;

    private TextureView mTextureView;
    private CameraDevice mCameraDevice;

    private CameraCaptureSession captureSession;

    public void openCamera(Context context, TextureView textureView) {
        try {
            mContext = context;
            mTextureView = textureView;
            //获取相机管理者
            CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            //获取相机ID列表
            String cameraIdList[] = cameraManager.getCameraIdList();
            for (String cameraId: cameraIdList) {
                //获取相机特征
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                //获取相机朝向--后置摄像头
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    mCameraId = cameraId;
                    break;
                }
            }
            selectedOutPutSize(cameraManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    private void selectedOutPutSize(CameraManager cameraManager) {
        try {
            //获取当前相机特征
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(mCameraId);
            //获取输出流配置
            StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            //获取输出尺寸数组
            Size[] outputSize = map.getOutputSizes(ImageReader.class);
            Size viewSize = updateCameraPreviewWithVideoMode(outputSize);
            mWidth = viewSize.getWidth();
            mHeight = viewSize.getHeight();

            cameraManager.openCamera(mCameraId, stateCallback, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public int getHeight() {
        return mHeight;
    }

    public int getWidth() {
        return mWidth;
    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
            startPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            mCameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    };



    private void setupMediaRecorder() {
        try {
            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mVideoFilePage = getVideoFilePage();
            mMediaRecorder.setOutputFile(mVideoFilePage);
            mMediaRecorder.setVideoEncodingBitRate(1000000);
            mMediaRecorder.setVideoFrameRate(30);
            mMediaRecorder.setVideoSize(mWidth, mHeight);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String getVideoFilePage() {
        final File dir = mContext.getExternalFilesDir(null);
        return (dir == null ? "" : (dir.getAbsolutePath() + "/")) + System.currentTimeMillis();
    }



    private Size updateCameraPreviewWithVideoMode(Size[] outputSize) {
        List<Size> sizes = new ArrayList<>();
        Size previewSize = null;
        //计算预览窗口高宽比
//        float ratio = ((float)ScreenUtils.getScreenHeight(mContext) / ScreenUtils.getScreenWidth(mContext));
        float ratio = ((float)mTextureView.getHeight() / mTextureView.getWidth());

        //首先选取宽高比与预览窗口宽高比一只的最大输出尺寸
        for (int i = 0; i < outputSize.length; i++) {
            if (((float)(outputSize[i].getWidth() / outputSize[i].getHeight())) == ratio) {
                sizes.add(outputSize[i]);
            }
        }

        if (sizes.size() > 0) {
            previewSize = Collections.max(sizes, new CompareSizesByArea());
            return previewSize;
        }

        //如果不存在宽高比与预览窗口宽高比一只的，选择最近的输出尺寸
        float detRationMin = Float.MAX_VALUE;
        for (int i = 0; i < outputSize.length; i++) {
            Size size = outputSize[i];
            float curRatio = ((float)(size.getWidth() / size.getHeight()));
            if (Math.abs(curRatio - ratio) < detRationMin) {
                detRationMin = curRatio;
                previewSize = size;
            }
        }


        if (previewSize.getWidth() * previewSize.getHeight() > PREVIEW_SIZE_MIN) {
            return previewSize;
        }

        //如果宽高比最接近的输出尺寸太小，则选择与预览窗口面积最接近的输出尺寸
        long area = mTextureView.getHeight() * mTextureView.getWidth();
        long detAreaMin = Long.MAX_VALUE;
        for (int i = 0; i < outputSize.length; i++) {
            Size size = outputSize[i];
            long curArea = size.getWidth() * size.getHeight();
            if (Math.abs(curArea - area) < detAreaMin) {
                detAreaMin = curArea;
                previewSize = size;
            }
        }
        return previewSize;
    }



    private void startPreview() {
//        setupMediaRecorder();
        SurfaceTexture texture = mTextureView.getSurfaceTexture();
//        texture.setDefaultBufferSize(mWidth, mHeight);
        Surface previewSurface = new Surface(texture);
//        Surface recorderSurface = mMediaRecorder.getSurface();

        try {
            final CaptureRequest.Builder previewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewBuilder.addTarget(previewSurface);
//            previewBuilder.addTarget(recorderSurface);
            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface), new CameraCaptureSession.StateCallback(){

                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    captureSession = session;
                    try {
                        captureSession.setRepeatingRequest(previewBuilder.build(), null, null);
                        mMediaRecorder.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            }, null);



        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum( (long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
        }
    }



}
