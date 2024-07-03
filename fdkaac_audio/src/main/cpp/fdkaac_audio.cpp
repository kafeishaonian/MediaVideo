#include <jni.h>
#include <string>
#include "decode/AACDecoder.h"
#include "encode/AACEncoder.h"


AACEncoder *mAACEncode = 0;
AACDecoder *mAACDecode = 0;


static jint Java_Init(JNIEnv *env, jobject interface, jint bitRate, jint channels, jint sampleRate) {
    if (!mAACEncode) {
        mAACEncode = new AACEncoder();
    }

    AACProfile profile = LC_AAC;
    mAACEncode->init(profile, sampleRate,channels, bitRate);

    return 1;
}

static void Java_Encode(JNIEnv *env, jobject interface, jbyteArray byteArray, jint size) {
    jbyte *pcm = env->GetByteArrayElements(byteArray, 0);
    char *outBuffer = 0;
    if (mAACEncode) {
        mAACEncode->encode((Byte *) (pcm), size, &outBuffer);
    }
    env->ReleaseByteArrayElements(byteArray, pcm, 0);
}

static void Java_Destroy(JNIEnv *env, jobject interface) {
    if (mAACEncode) {
        mAACEncode->destroy();
        delete mAACEncode;
        mAACEncode = NULL;
    }

    if (mAACDecode) {
        mAACDecode->destroy();
        delete mAACDecode;
        mAACDecode = NULL;
    }
}

static const JNINativeMethod encodeMethod[] = {
        {"init", "(III)I", (void *) Java_Init},
        {"encode", "([BI)V", (void *) Java_Encode},
        {"destroy", "()V", (void *) Java_Destroy}
};




static jint Java_InitWithADFormat(JNIEnv *env, jobject interface) {
    int ret;
    if (!mAACDecode) {
        mAACDecode = new AACDecoder();
    }
    ret = mAACDecode->initWithADTSFormat();
    return ret;
}

static jint Java_InitWithRAWFormat(JNIEnv *env, jobject interface, jbyteArray specInfo, jint size) {
    int ret;
    if (!mAACDecode) {
        mAACDecode = new AACDecoder();
    }
    jbyte *spec = env->GetByteArrayElements(specInfo, 0);
    ret = mAACDecode->initWithRawFormat((byte *) (spec), size);
    env->ReleaseByteArrayElements(specInfo, spec, 0);
    return ret;
}

static jbyteArray Java_Decode(JNIEnv *env, jobject interface, jbyteArray byteArray, jint length) {
    int ret;
    jbyte *aac = env->GetByteArrayElements(byteArray, 0);
    byte *outFrame = NULL;
    jbyteArray pcmByte = NULL;
    if (mAACDecode) {
        ret = mAACDecode->decode(reinterpret_cast<byte *>(aac), length, &outFrame);
        if (outFrame) {
            pcmByte = env->NewByteArray(ret);
            env->SetByteArrayRegion(pcmByte, 0, ret, reinterpret_cast<const jbyte *>(outFrame));
            delete[] outFrame;
        }
    }
    env->ReleaseByteArrayElements(byteArray, aac, 0);
    return pcmByte;
}


static const JNINativeMethod decodeMethod[] = {
        {"initWithADFormat", "()I", (void *) Java_InitWithADFormat},
        {"initWithRAWFormat", "([BI)I", (void *) Java_InitWithRAWFormat},
        {"decode", "([BI)[B", (void *) Java_Decode},
        {"destroy", "()V", (void *) Java_Destroy}
};




extern "C"
JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv((void **)env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    jclass encodeClass = env->FindClass("com/example/fdkaac_audio/FDKAACEncode");
    if (!encodeClass) {
        return JNI_ERR;
    }

    jclass decodeClass = env->FindClass("com/example/fdkaac_audio/FDKAACDecode");
    if (!decodeClass) {
        return JNI_ERR;
    }

    if (env->RegisterNatives(encodeClass, encodeMethod, sizeof(encodeMethod) / sizeof(encodeMethod[0]))) {
        return JNI_ERR;
    }

    if (env->RegisterNatives(decodeClass, decodeMethod, sizeof(decodeMethod) / sizeof(decodeMethod[0]))) {
        return JNI_ERR;
    }

    return JNI_VERSION_1_6;

}