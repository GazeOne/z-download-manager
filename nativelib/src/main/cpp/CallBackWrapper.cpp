//
// Created by 11988 on 2025/4/17.
//
#include "jni.h"
#include "NativeLib.h"

namespace Download {
    class CallBackWrapper {

    public:
        CallBackWrapper(JNIEnv* env, jobject callBack) {
            if (callBack != nullptr) {
                jcallback = env->NewGlobalRef(callBack);
                jclass jclass1 = env->GetObjectClass(callBack);
                successMethod = env->GetMethodID(jclass1, "onSuccess", "(Ljava/lang/String;)V");
                failedMethod = env->GetMethodID(jclass1, "onFailed", "(Ljava/lang/String;)V");
                env->DeleteLocalRef(jclass1);
            }
        }

        void triggerSuccess(char* message) {
            if (jcallback != nullptr) {
                JNIEnv* env = getJNIEnv();
                jstring jMessage = env->NewStringUTF(message);
                env->CallVoidMethod(jcallback, successMethod, jMessage);
                env->DeleteLocalRef(jMessage);
            }
        }

        void triggerFailed(char* message) {
            if (jcallback != nullptr) {
                JNIEnv* env = getJNIEnv();
                jstring  jmessage = env->NewStringUTF(message);
                env->CallVoidMethod(jcallback, failedMethod, jmessage);
                env->DeleteLocalRef(jmessage);
            }
        }

        ~CallBackWrapper() {
            if (jcallback != nullptr) {
                JNIEnv* env = getJNIEnv();
                env->DeleteGlobalRef(jcallback);
                jcallback = nullptr;
            }
        }

    private:

        JNIEnv* getJNIEnv() {
            // 跨线程安全获取 JNIEnv
            JNIEnv* env;
            g_vm->AttachCurrentThread(&env, nullptr);
            return env;
        }

        jobject jcallback;
        jmethodID successMethod;
        jmethodID failedMethod;
    };
}
