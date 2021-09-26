//
// Created by 王志江 on 9/14/21.
//

#include "gl_helper.h"
#include "logger.h"

#define TAG "gl_helper"

namespace igw {

    GLuint LoadShader(const GLenum shaderType, const char *shaderSource) {
        int shader = glCreateShader(shaderType);
        if (shader != 0) {
            // Pass in the shader source.
            glShaderSource(shader, 1, &shaderSource, NULL);

            // Compile the shader.
            glCompileShader(shader);

            // Get the compilation status.
            int compileStatus = GL_TRUE;
            glGetShaderiv(shader, GL_COMPILE_STATUS, &compileStatus);

            // If the compilation failed, delete the shader.
            if (compileStatus == GL_FALSE) {
                GLint maxLength = 0;
                glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &maxLength);

                LOGE(TAG, "status is %d", compileStatus);
                GLchar *msg = new GLchar [maxLength];
                glGetShaderInfoLog(shader, maxLength, &maxLength, msg);

                LOGE(TAG, "Error compiling shader: %d: %s", shaderType, msg);

                delete[] msg;
                glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    GLuint CreateProgram(const char *vertexSource, const char *fragmentSource) {
        GLuint vertexShader = LoadShader(GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }

        GLuint fragmentShader = LoadShader(GL_FRAGMENT_SHADER, fragmentSource);
        if (fragmentShader == 0) {
            return 0;
        }

        int program = glCreateProgram();
        if (program != 0) {
            glAttachShader(program, vertexShader);
            CheckGLError(TAG, "glAttachShader");
            glAttachShader(program, fragmentShader);
            CheckGLError(TAG, "glAttachShader");

            glLinkProgram(program);

            int linkStatus = GL_TRUE;
            glGetProgramiv(program, GL_LINK_STATUS, &linkStatus);
            if (linkStatus == GL_FALSE) {
                GLint maxLength = 0;
                glGetProgramiv(program, GL_INFO_LOG_LENGTH, &maxLength);

                LOGE(TAG, "status is %d", linkStatus);
                GLchar *msg = new GLchar [maxLength];
                glGetProgramInfoLog(program, maxLength, &maxLength, msg);

                LOGE(TAG, "Error compiling program: %d: %s", program, msg);

                glDeleteProgram(program);
                program = 0;
            }
        }

        // Mark the shaders for deletion once the program is deleted.
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);

        return program;
    }

    int CheckGLError(const char *tag, const char *op) {
        int error = glGetError();
        if (error != GL_NO_ERROR) {
            LOGE(tag, "%s: glError 0x%x", op, error);
        }
        return error;
    }
}
