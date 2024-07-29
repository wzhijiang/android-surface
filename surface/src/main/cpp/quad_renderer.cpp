//
// Created by 王志江 on 9/14/21.
//

#include <assert.h>
#include "quad_renderer.h"

#define LOG_TAG "nr_surface"

namespace igw {

    static const char *kVertexShader = "uniform mat4 uSTMatrix;\n"
                                       "attribute vec3 aPosition;\n"
                                       "attribute vec4 aTexcoord;\n"
                                       "varying vec2 vTexcoord;\n"
                                       "void main() {\n"
                                       "  vTexcoord = (uSTMatrix * aTexcoord).xy;\n"
                                       "  vTexcoord.x = 1.0 - vTexcoord.x;\n"
                                       "  gl_Position = vec4(aPosition.x, aPosition.y, aPosition.z, 1.0);\n"
                                       "}\n\0";
    static const char *kFragmentShader = "#extension GL_OES_EGL_image_external: require\n"
                                         "precision mediump float;\n"
                                         "uniform samplerExternalOES sTexture;\n"
                                         "varying vec2 vTexcoord;\n"
                                         "void main() {\n"
                                         "   gl_FragColor = texture2D(sTexture, vTexcoord);\n"
                                         "}\n\0";

    static const float kVertices[] = {
            1.0f,  1.0f, 0.0f,  // top right
            1.0f, -1.0f, 0.0f,  // bottom right
            -1.0f, -1.0f, 0.0f,  // bottom left
            -1.0f,  1.0f, 0.0f   // top left
    };
    static const short kIndices[] = {
            0, 1, 3, // first triangle
            1, 3, 2  // second triangle
    };
    static const float kTexcoords[] = {
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f
    };

    QuadRenderer::QuadRenderer() {

    }

    void QuadRenderer::InitGL() {
        program_ = CreateProgram(kVertexShader, kFragmentShader);

        glBindAttribLocation(program_, 1, "aPosition");
        glBindAttribLocation(program_, 2, "aTexcoord");

        position_param_ = glGetAttribLocation(program_, "aPosition");
        texcoord_param_ = glGetAttribLocation(program_, "aTexcoord");

        texture_st_param_ = (GLuint)glGetUniformLocation(program_, "uSTMatrix");
        assert(texture_st_param_ >= 0);

        texture_param_ = glGetUniformLocation(program_, "sTexture");
        assert(texture_param_ >= 0);
    }

    void QuadRenderer::Draw(TextureHandle *texture_handle) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        glUseProgram(program_);

        glEnableVertexAttribArray(position_param_);
        glVertexAttribPointer(position_param_, 3, GL_FLOAT, GL_FALSE, 0, kVertices);
        glEnableVertexAttribArray(texcoord_param_);
        glVertexAttribPointer(texcoord_param_, 4, GL_FLOAT, GL_FALSE, 0, kTexcoords);

        glActiveTexture(GL_TEXTURE0);
        if (texture_handle->IsOESTexture())
            glBindTexture(GL_TEXTURE_EXTERNAL_OES, texture_handle->GetTextureId());
        else
            glBindTexture(GL_TEXTURE_2D, texture_handle->GetTextureId());
        glUniform1i(texture_param_, 0);
        CheckGLError(LOG_TAG, "bind texture");

        glUniformMatrix4fv(texture_st_param_, 1, GL_FALSE, texture_handle->GetSTMatrix());
        CheckGLError(LOG_TAG, "set texture mat");

        glDrawElements(GL_TRIANGLE_STRIP, 6, GL_UNSIGNED_SHORT, kIndices);
        CheckGLError(LOG_TAG, "draw elements");

        if (texture_handle->IsOESTexture())
            glBindTexture(GL_TEXTURE_EXTERNAL_OES, 0);
        else
            glBindTexture(GL_TEXTURE_2D, 0);
        glDisableVertexAttribArray(position_param_);
        glDisableVertexAttribArray(texcoord_param_);
    }
}
