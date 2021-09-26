//
// Created by 王志江 on 9/14/21.
//

#ifndef ANDROIDSURFACE_GL_HELPER_H
#define ANDROIDSURFACE_GL_HELPER_H

#include <GLES2/gl2.h>

#define GL_TEXTURE_EXTERNAL_OES 0x8D65

namespace igw {

GLuint LoadShader(const GLenum shaderType, const char *shaderSource);

GLuint CreateProgram(const char *vertexSource, const char *fragmentSource);

int CheckGLError(const char *tag, const char *op);

}

#endif //ANDROIDSURFACE_GL_HELPER_H
