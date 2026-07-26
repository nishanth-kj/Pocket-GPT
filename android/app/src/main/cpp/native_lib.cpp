#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_pocketgpt_app_utils_NativeEngine_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++ (Pocket GPT Native Engine)";
    return env->NewStringUTF(hello.c_str());
}
