// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("packageName");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("packageName")
//      }
//    }
#include <string>
#include <cstring>
#include <jni.h>
#include <unistd.h>

extern "C" JNIEXPORT jboolean JNICALL Java_sh_tablet_bgclipboard_utils_root_Native_setegid(JNIEnv *env, jobject thiz, jint gid) {
    return setegid(static_cast<gid_t>(gid)) == 0;
}


extern "C" JNIEXPORT jboolean JNICALL Java_sh_tablet_bgclipboard_utils_root_Native_seteuid(JNIEnv *env, jobject thiz, jint uid) {
   return seteuid(static_cast<uid_t>(uid)) == 0;
}
