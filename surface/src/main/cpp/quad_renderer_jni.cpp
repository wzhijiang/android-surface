//
// Created by 王志江 on 9/14/21.
//

#include <jni.h>
#include "quad_renderer.h"
#include "texture_handle.h"

using namespace igw;

extern "C"
JNIEXPORT jlong JNICALL
Java_io_github_wzhijiang_android_surface_QuadRenderer_nativeCreateQuadRenderer(JNIEnv *env, jobject obj) {
    QuadRenderer *ptr = new QuadRenderer();
    return (jlong)ptr;
}

extern "C"
JNIEXPORT void JNICALL
Java_io_github_wzhijiang_android_surface_QuadRenderer_nativeDeleteQuadRenderer(JNIEnv *env, jobject obj,
                                                                                  jlong handle) {
    QuadRenderer *ptr = (QuadRenderer *)handle;
    if (ptr) {
        delete ptr;
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_io_github_wzhijiang_android_surface_QuadRenderer_nativeInitGL(JNIEnv *env, jobject obj,
                                                                   jlong handle) {
    QuadRenderer *ptr = (QuadRenderer *)handle;
    if (ptr) {
        ptr->InitGL();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_io_github_wzhijiang_android_surface_QuadRenderer_nativeDraw(JNIEnv *env, jobject obj,
                                                                 jlong handle, jlong j_texture_handle_ptr) {
    QuadRenderer *ptr = (QuadRenderer *)handle;
    if (!ptr) {
        return;
    }

    TextureHandle *texture_handle = (TextureHandle *)j_texture_handle_ptr;
    if (!texture_handle) {
        return;
    }

    ptr->Draw(texture_handle);
}
