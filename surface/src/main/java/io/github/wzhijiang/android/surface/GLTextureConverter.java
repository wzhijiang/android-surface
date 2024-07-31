package io.github.wzhijiang.android.surface;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class GLTextureConverter {

    private static final String TAG = GLTextureConverter.class.getSimpleName();

    private static final int FLOAT_SIZE_BYTES = 4;
    private static final String VERTEX_SHADER = "uniform mat4 uSTMatrix;\n"
            + "attribute vec4 aPosition;\n"
            + "attribute vec4 aTextureCoord;\n"
            + "varying vec2 vTextureCoord;\n"
            + "void main() {\n"
            + "    gl_Position = aPosition;\n"
            + "    vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n"
            + "}\n";
    private static final String FRAGMENT_SHADER = "#extension GL_OES_EGL_image_external : require\n"
            + "precision mediump float;\n"
            + "varying vec2 vTextureCoord;\n"
            + "uniform samplerExternalOES sTexture;\n"
            + "void main() {\n"
            + "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n"
            + "}\n";

    private static final int VERTICES_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    private static final int VERTICES_POSITION_OFFSET = 0;
    private static final int VERTICES_UV_OFFSET = 3;
    private static final float[] VERTICES = {
            // X, Y, Z, U, V
            -1.0f, -1.0f, 0.0f, 0.0f, 0.0f,
            1.0f, -1.0f, 0.0f, 1.0f, 0.0f,
            -1.0f, 1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f , 1.0f, 1.0f
    };

    private int mOutputWidth;
    private int mOutputHeight;
    private float[] mSTMatrix = new float[16];
    private FloatBuffer mVerticesBuffer;

    private int mProgram;
    private int[] mTextureIds;
    private int[] mFramebufferIds;
    private int muSTMatrixHandle;
    private int mPositionHandle;
    private int mTextureCoordHandle;
    private int mTextureHandle;

    public GLTextureConverter() {
        mTextureIds = new int[] { 0 };
        mFramebufferIds = new int[] { 0 };

        mVerticesBuffer = ByteBuffer.allocateDirect(VERTICES.length * FLOAT_SIZE_BYTES)
                        .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVerticesBuffer.put(VERTICES).position(0);

        init();
    }

    private void init() {
        // Create programe and acquire uniform locations
        mProgram = GLHelper.createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        if (mProgram == 0) {
            throw new RuntimeException("Failed to create program");
        }

        muSTMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uSTMatrix");
        GLHelper.checkGlError(TAG, "glGetUniformLocation uSTMatrix");
        if (muSTMatrixHandle == -1) {
//            throw new RuntimeException("Could not get uniform location for uSTMatrix");
        }

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        GLHelper.checkGlError(TAG, "glGetAttribLocation aPosition");
        if (mPositionHandle == -1) {
//            throw new RuntimeException("Could not get attrib location for aPosition");
        }

        mTextureCoordHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        GLHelper.checkGlError(TAG, "glGetAttribLocation aTextureCoord");
        if (mTextureCoordHandle == -1) {
//            throw new RuntimeException("Could not get attrib location for aTextureCoord");
        }

        mTextureHandle = GLES20.glGetUniformLocation(mProgram, "sTexture");
        GLHelper.checkGlError(TAG, "glGetAttribLocation sTexture");
        if (mTextureHandle == -1) {
//            throw new RuntimeException("Could not get uniform location for sTexture");
        }
    }

    public void setOutputResolution(int width, int height) {
        mOutputWidth = width;
        mOutputHeight = height;

        // Destroy framebuffer
        destroyFramebuffer();

        // Create framebuffer
        GLHelper.createFramebuffer(mTextureIds, mFramebufferIds, mOutputWidth, mOutputHeight);
    }

    public void setSTMatrix(float[] stMatrix) {
        System.arraycopy(stMatrix, 0, mSTMatrix, 0, 16);
    }

    public int getTextureId() {
        return mTextureIds[0];
    }

    public int drawToTexture(int textureId) {
        if (mFramebufferIds[0] == 0) {
            throw new RuntimeException("Framebuffer not created");
        }

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebufferIds[0]);
        draw(textureId);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        return mTextureIds[0];
    }

    public void release() {
        destroyFramebuffer();
        if (mProgram != 0) {
            GLES20.glDeleteProgram(mProgram);
            mProgram = 0;
        }
    }

    private void draw(int textureId) {
        GLES20.glViewport(0, 0, mOutputWidth, mOutputHeight);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // Use the program
        GLES20.glUseProgram(mProgram);
        GLHelper.checkGlError(TAG, "glUseProgram");

        // Set the texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glUniform1i(mTextureHandle, 0);
        GLHelper.checkGlError(TAG, "glUniform1i mTextureHandle");

        // Set the texture uniform
        GLES20.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0);
        GLHelper.checkGlError(TAG, "glUniformMatrix4fv muSTMatrixHandle");

        // Set the vertex attributes
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLHelper.checkGlError(TAG, "glEnableVertexAttribArray mPositionHandle");
        mVerticesBuffer.position(VERTICES_POSITION_OFFSET);
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false,
                VERTICES_STRIDE_BYTES, mVerticesBuffer);
        GLHelper.checkGlError(TAG, "glVertexAttribPointer aPosition");

        GLES20.glEnableVertexAttribArray(mTextureCoordHandle);
        GLHelper.checkGlError(TAG, "glEnableVertexAttribArray mTextureCoordHandle");
        mVerticesBuffer.position(VERTICES_UV_OFFSET);
        GLES20.glVertexAttribPointer(mTextureCoordHandle, 2, GLES20.GL_FLOAT, false,
                VERTICES_STRIDE_BYTES, mVerticesBuffer);
        GLHelper.checkGlError(TAG, "glVertexAttribPointer aTextureCoord");

        // Draw
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLHelper.checkGlError(TAG, "glDrawArrays");

        // Clean up
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordHandle);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
    }

    private void destroyFramebuffer() {
        if (mFramebufferIds[0] != 0) {
            GLES20.glDeleteFramebuffers(1, mFramebufferIds, 0);
            mFramebufferIds[0] = 0;
        }
        if (mTextureIds[0] != 0) {
            GLES20.glDeleteTextures(1, mTextureIds, 0);
            mTextureIds[0] = 0;
        }
    }
}
