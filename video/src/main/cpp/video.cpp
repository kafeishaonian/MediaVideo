#include <jni.h>
#include <string>





static void Java_setDataSource(JNIEnv *env, jobject interface, jstring path) {


}

static void Java_prepareAsync(JNIEnv *env, jobject interface) {

}

static void Java_start(JNIEnv *env, jobject interface) {

}

static void Java_stop(JNIEnv *env, jobject interface) {

}

static void Java_pause(JNIEnv *env, jobject interface) {

}

static jboolean Java_isPlaying(JNIEnv *env, jobject interface) {

}


static void Java_seekTo(JNIEnv *env, jobject interface, jlong seekTo) {

}


static jlong Java_getCurrentPosition(JNIEnv *env, jobject interface) {

}

static jlong Java_getDuration(JNIEnv *env, jobject interface) {

}

static void Java_release(JNIEnv *env, jobject interface) {

}

static void Java_setVolume(JNIEnv *env, jobject interface, jfloat volume) {

}

static void Java_init(JNIEnv *env, jclass interface) {

}



static const JNINativeMethod gMethod[] = {
        {"_setDataSource",      "(Ljava/lang/String;)V", (void *) Java_setDataSource},
        {"_prepareAsync",       "()V",                   (void *) Java_prepareAsync},
        {"_start",              "()V",                   (void *) Java_start},
        {"_stop",               "()V",                   (void *) Java_stop},
        {"_pause",              "()V",                   (void *) Java_pause},
        {"_isPlaying",          "()Z",                   (void *) Java_isPlaying},
        {"_seekTo",             "(J)V",                  (void *) Java_seekTo},
        {"_getCurrentPosition", "()J",                   (void *) Java_getCurrentPosition},
        {"_getDuration",        "()J",                   (void *) Java_getDuration},
        {"_release",            "()V",                   (void *) Java_release},
        {"_setVolume",          "(F)V",                  (void *) Java_setVolume},
        {"_init",               "()V",                   (void *) Java_init}
};

extern "C"
JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = NULL;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }

    jclass clazz = env->FindClass("com/example/video/NativeLib");
    if (!clazz) {
        return -1;
    }

    if (env->RegisterNatives(clazz, gMethod, sizeof(gMethod) / sizeof(gMethod[0]))) {
        return -1;
    }
    return JNI_VERSION_1_4;
}