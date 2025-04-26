#include <jni.h>
#include <string>
#include <cstdio>
#include "NativeLib.h"
#include "DownloadQueue.h"

//extern "C" JNIEXPORT jstring JNICALL
//Java_com_wy_nativelib_NativeLib_stringFromJNI(
//        JNIEnv* env,
//        jobject thiz) {
//    std::string hello = "Hello from C++";
//
//    jclass clazz = env->GetObjectClass(thiz);
//    jfieldID fieldId = env->GetFieldID(clazz, "person", "Lcom/wy/nativelib/Person;");
//
//    jobject jperson = env->GetObjectField(thiz, fieldId);
//    jclass personclazz = env->GetObjectClass(jperson);
//    jfieldID personId = env->GetFieldID(personclazz, "name", "Ljava/lang/String;");
//    jstring newName = env->NewStringUTF("Alice");
//    env->SetObjectField(jperson, personId, newName);
//
//    env->DeleteLocalRef(newName);
//
//    return env->NewStringUTF(hello.c_str());
//}

JavaVM* g_vm = nullptr; // 需在 JNI_OnLoad 中保存
static Download::DownloadQueue* queue = nullptr;

jstring getName(JNIEnv* env, jobject thiz) {
    std::string hello = "Hello getName";
    return env->NewStringUTF(hello.c_str());
}

// 全局变量保存回调对象和方法 ID
static jobject g_callback = nullptr;
static jmethodID onSuccessMethodId = nullptr;
static jmethodID onFailedMethodId = nullptr;

void setCallBack(JNIEnv* env, jobject thiz, jobject callback) {
    // 1. 删除旧的全局引用（避免内存泄漏）
    if (g_callback != nullptr) {
        env->DeleteGlobalRef(g_callback);
        g_callback = nullptr;
    }

    g_callback = env->NewGlobalRef(callback);
    // 3. 获取方法 ID（只需获取一次）
    if (onSuccessMethodId == nullptr || onFailedMethodId == nullptr) {
        jclass clazz = env->GetObjectClass(g_callback);
        onSuccessMethodId = env->GetMethodID(clazz, "onSuccess", "(Ljava/lang/String;)V");
        onFailedMethodId = env->GetMethodID(clazz, "onFailed", "(Ljava/lang/String;)V");
        env->DeleteLocalRef(clazz); // 删除局部引用
    }
}

extern "C"
JNIEXPORT void JNICALL Java_com_wy_nativelib_NativeLib_addCallBack(JNIEnv* env, jobject object, jobject callback) {
    if (queue != nullptr) {
        queue->addCallBack(env, callback);
    }
}

// 触发成功回调
void triggerSuccess(JNIEnv* env, const char* message) {
    if (g_callback == nullptr) return;

    jstring jMessage = env->NewStringUTF(message);
    env->CallVoidMethod(g_callback, onSuccessMethodId, jMessage);
    env->DeleteLocalRef(jMessage); // 删除局部引用
}

//触发失败回调
void triggerFailed(JNIEnv* env, const char* message) {

    if (g_callback == nullptr) return;

    jstring jMessage = env->NewStringUTF(message);
    env->CallVoidMethod(g_callback, onFailedMethodId, jMessage);
    env->DeleteLocalRef(jMessage);
}

void thread_function() {
    JNIEnv* env = nullptr;
    g_vm->AttachCurrentThread(&env, nullptr);
    triggerSuccess(env, "success from thread");
    g_vm->DetachCurrentThread();
}

void notifyAll(JNIEnv* env, jobject thiz) {
    if (queue != nullptr) {
        queue->notifyAll();
    }
}

static const JNINativeMethod nativeMethod[] = {
        {"stringFromJNI", "()Ljava/lang/String;", (jstring *)getName},
        {"setCallBack", "(Lcom/wy/nativelib/CallBack;)V", (void *)setCallBack},
        {"notifyCallBackAll", "()V", (void *)notifyAll},
};

extern "C" JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    queue = new Download::DownloadQueue();
    g_vm = vm;
    JNIEnv *env = NULL;
    // 初始化JNIEnv
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_FALSE;
    }
    // 找到需要动态注册的java类
    jclass jniClass = env->FindClass("com/wy/nativelib/NativeLib");
    if (nullptr == jniClass) {
        return JNI_FALSE;
    }
    // 动态注册
    if (env->RegisterNatives(jniClass, nativeMethod, sizeof(nativeMethod) / sizeof(nativeMethod[0])) != JNI_OK) {
        return JNI_FALSE;
    }
    // 返回JNI使用的版本
    return JNI_VERSION_1_6;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_wy_nativelib_NativeLib_createNativeObject(JNIEnv *env, jobject thiz) {
    jclass jclass1 = env->GetObjectClass(thiz);
    jfieldID id = env->GetFieldID(jclass1, "nativePtr", "J");
    Download::DownloadQueue* queue1 = new Download::DownloadQueue();
    env->SetLongField(thiz, id, reinterpret_cast<jlong>(queue1));
    env->DeleteLocalRef(jclass1);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_wy_nativelib_NativeLib_getNativeObjectName(JNIEnv *env, jobject thiz, jlong ptr) {
    Download::DownloadQueue* queue1 = reinterpret_cast<Download::DownloadQueue*>(ptr);
    queue1->notifyAll();
    return env->NewStringUTF("测试");
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_wy_nativelib_NativeLib_getNativeObjectName2(JNIEnv *env, jobject thiz) {
    jclass jclass1 = env->GetObjectClass(thiz);
    jfieldID jfieldId = env->GetFieldID(jclass1, "nativePtr", "J");
    jlong value = env->GetLongField(thiz, jfieldId);
    auto nativePtr = reinterpret_cast<Download::DownloadQueue*>(value);
    nativePtr->notifyAll();
    return env->NewStringUTF("测试2");
}