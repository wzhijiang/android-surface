cmake_minimum_required(VERSION 3.4.1)

project(igw_surface LANGUAGES C CXX)

add_library(${PROJECT_NAME}
    SHARED
        gl_helper.cpp
        quad_renderer.cpp
        quad_renderer_jni.cpp
        texture_handle.cpp
        texture_handle_jni.cpp)

target_include_directories(${PROJECT_NAME}
    PRIVATE
        ${CMAKE_CURRENT_SOURCE_DIR}/Unity)

target_link_libraries(${PROJECT_NAME}
    PRIVATE
        android
        log
        EGL
        GLESv2)
