#include <jni.h>
#include <string>

static jint Java_Init(JNIEnv *env, jobject interface, jint bitRate, jint channels, jint sampleRate) {

}

static void Java_Encode(JNIEnv *env, jobject interface, jbyteArray byteArray, jint size) {

}

static void Java_Destroy(JNIEnv *env, jobject interface) {

}

static const JNINativeMethod encodeMethod[] = {
        {"init", "(III)I", (void *) Java_Init},
        {"encode", "([BI)V", (void *) Java_Encode},
        {"destroy", "()V", (void *) Java_Destroy}
};




static void Java_InitWithADFormat(JNIEnv *env, jobject interface) {

}

static jint Java_InitWithRAWFormat(JNIEnv *env, jobject interface, jbyteArray specInfo, jbyteArray size) {

}

static jbyteArray Java_Decode(JNIEnv *env, jobject interface, jbyteArray byteArray, jint length) {

}


static const JNINativeMethod decodeMethod[] = {
        {"initWithADFormat", "()I", (void *) Java_InitWithADFormat},
        {"initWithRAWFormat", "([B[B)I", (void *) Java_InitWithRAWFormat},
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