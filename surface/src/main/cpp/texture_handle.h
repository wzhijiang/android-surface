//
// Created by 王志江 on 9/15/21.
//

#ifndef ANDROIDSURFACE_TEXTURE_HANDLE_H
#define ANDROIDSURFACE_TEXTURE_HANDLE_H

#include <jni.h>
#include "gl_helper.h"

namespace igw {

class TextureHandle {
public:
    TextureHandle(JNIEnv *j_env, jint j_texture_id, jfloatArray j_st_matrix);

    GLuint GetTextureId();
    GLfloat *GetSTMatrix();

private:
    GLuint texture_id_;
    float st_matrix_[16];
};

}

#endif //ANDROIDSURFACE_TEXTURE_HANDLE_H
