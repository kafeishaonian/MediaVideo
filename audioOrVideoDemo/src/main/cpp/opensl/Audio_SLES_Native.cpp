//
// Created by MOMO on 2024/7/1.
//

#include <jni.h>
#include <pthread.h>
#include "OpenSLAudioPlay.h"

/**
 * 播放 pcmFile
 */
FILE *pcmFile = 0;

OpenSLAudioPlay *slAudioPlay = NULL;

bool isPlaying = false;

void *playThreadFunc(void *arg);

void *playThreadFunc(void *arg) {
    const int bufferSize = 2048;
    short buffer[bufferSize];
    while (isPlaying && !feof(pcmFile)) {
        fread(buffer, 1, bufferSize, pcmFile);
        slAudioPlay->enqueueSample(buffer, bufferSize);
    }

    return 0;
}


static void Java_PlayPcm(JNIEnv *env, jclass interface, jstring path) {
    const char *_pcmPath = env->GetStringUTFChars(path, NULL);

    if (slAudioPlay) {
        slAudioPlay->release();
        delete slAudioPlay;
        slAudioPlay = NULL;
    }

    slAudioPlay = new OpenSLAudioPlay(44100, SAMPLE_FORMAT_16, 1);
    slAudioPlay->init();
    pcmFile = fopen(_pcmPath, "r");
    isPlaying = true;
    pthread_t playThread;
    pthread_create(&playThread, NULL, playThreadFunc, 0);

    env->ReleaseStringUTFChars(path, _pcmPath);
}

static void Java_StopPcm(JNIEnv *env, jclass interface) {
    isPlaying = false;

    if (slAudioPlay) {
        slAudioPlay->release();
        delete slAudioPlay;
        slAudioPlay = NULL;
    }

    if (pcmFile) {
        fclose(pcmFile);
        pcmFile = NULL;
    }
}


static const JNINativeMethod gMethod[] = {
        {"nativePlayPcm", "(Ljava/lang/String;)V", (void *) Java_PlayPcm},
        {"nativeStopPcm","()V", (void *) Java_StopPcm}
};


extern "C"
JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {

    JNIEnv *env = NULL;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }

    jclass clazz = env->FindClass("com/example/video/audio/AudioSLESNativeLib");
    if (!clazz) {
        return -1;
    }

    if (env->RegisterNatives(clazz, gMethod, sizeof(gMethod) / sizeof(gMethod[0]))) {
        return -1;
    }

    return JNI_VERSION_1_6;

}