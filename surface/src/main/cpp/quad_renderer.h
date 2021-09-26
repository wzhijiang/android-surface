//
// Created by 王志江 on 9/15/21.
//

#ifndef ANDROIDSURFACE_QUAD_RENDERER_H
#define ANDROIDSURFACE_QUAD_RENDERER_H

#include "gl_helper.h"
#include "texture_handle.h"

namespace igw {

class QuadRenderer {
public:
    QuadRenderer();

    void InitGL();
    void Draw(TextureHandle *texture_handle);

private:

    GLuint program_;
    GLuint position_param_;
    GLuint texcoord_param_;
    GLuint texture_param_;
    GLuint texture_st_param_;
};

}

#endif //ANDROIDSURFACE_QUAD_RENDERER_H
