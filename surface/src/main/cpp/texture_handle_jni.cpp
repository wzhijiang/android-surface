//
// Created by 王志江 on 9/15/21.
//

#include <jni.h>
#include "texture_handle.h"

using namespace igw;

extern "C"
JNIEXPORT jlong JNICALL
Java_io_github_wzhijiang_android_surface_TextureHandle_nativeCreateTextureHandle(JNIEnv *env, jobject j_obj,
        jint texture_id, jfloatArray j_st_matrix, jboolean j_is_OES_texture) {
    TextureHandle *obj = new TextureHandle(env, texture_id, j_st_matrix, j_is_OES_texture);
    return (jlong)obj;
}

extern "C"
JNIEXPORT void JNICALL
Java_io_github_wzhijiang_android_surface_TextureHandle_nativeDeleteTextureHandle(JNIEnv *env, jobject j_obj,
        jlong handle) {
    TextureHandle *obj = (TextureHandle *)handle;
    if (!obj) return;

    delete obj;
}
