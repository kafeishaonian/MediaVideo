#include <jni.h>
#include <string>
#include "X264Encoder.h"

X264Encoder *x264Encoder = 0;


static void Java_init(JNIEnv *jniEnv, jobject interface, jstring outH264Path, jint width, jint height, jint videoBitRate,
                 jint frameRate) {

    if (!x264Encoder)
        x264Encoder = new X264Encoder();

    const char *h264Path = jniEnv->GetStringUTFChars(outH264Path, 0);

    x264Encoder->init(h264Path, width, height, videoBitRate, frameRate);

    jniEnv->ReleaseStringUTFChars(outH264Path, h264Path);

}

static void Java_encode(JNIEnv *jniEnv, jobject interface, jbyteArray jbyteArray1,jint yuvType) {
    jbyte *byte = jniEnv->GetByteArrayElements(jbyteArray1, 0);
    AVPacket *avPacket = static_cast<AVPacket *>(malloc(sizeof(AVPacket)));
    avPacket->data = reinterpret_cast<uint8_t *>(byte);
    avPacket->type = yuvType;
    if (x264Encoder)
        x264Encoder->encode(avPacket);
    free(avPacket);
    jniEnv->ReleaseByteArrayElements(jbyteArray1, byte, 0);
}

static void Java_destroy(JNIEnv *jniEnv, jobject interface) {
    if (x264Encoder)
        x264Encoder->destroy();
}



/**
 * 编码 native 动态函数
 */
static JNINativeMethod gMethods[] = {
        "init", "(Ljava/lang/String;IIII)V", (void *) Java_init,
        "encode", "([BI)V", (void *) Java_encode,
        "destroy", "()V", (void *) Java_destroy
};


/**
 * system.load 会执行该函数，然后动态注册 native
 * @param javaVM
 * @param pVoid
 * @return
 */
int JNI_OnLoad(JavaVM *javaVM, void *reserved) {
    JNIEnv *jniEnv;
    if (javaVM->GetEnv(reinterpret_cast<void **>(&jniEnv), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    jclass JVAV_CLASS = jniEnv->FindClass("com/example/x264_video_encode/NativeLib");
    jniEnv->RegisterNatives(JVAV_CLASS, gMethods, sizeof(gMethods) / sizeof(gMethods[0]));
    jniEnv->DeleteLocalRef(JVAV_CLASS);
    return JNI_VERSION_1_6;
}