//
// Created by 王志江 on 9/15/21.
//

#include "texture_handle.h"

namespace igw {

TextureHandle::TextureHandle(JNIEnv *j_env, jint j_texture_id, jfloatArray j_st_matrix, jboolean j_is_OES_texture) {
    texture_id_ = j_texture_id;
    is_OES_texture_ = j_is_OES_texture;
    j_env->GetFloatArrayRegion(j_st_matrix, 0, 16, st_matrix_);
}

GLuint TextureHandle::GetTextureId() {
    return texture_id_;
}

GLfloat * TextureHandle::GetSTMatrix() {
    return st_matrix_;
}

bool TextureHandle::IsOESTexture() {
    return is_OES_texture_;
}

}