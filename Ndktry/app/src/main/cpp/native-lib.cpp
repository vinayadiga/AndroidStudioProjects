#include <jni.h>
#include <string>

extern "C"
jstring Java_com_vinayvishnumurthy_ndktry_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

jint Java_com_vinayvishnumurthy_ndktry_MainActivity_addNumbers(JNIEnv *env, jobject, jint a, jint b)
{
    return (a+b);
}
