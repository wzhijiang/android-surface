package io.github.wzhijiang.android.surface;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class GLTextureConverter {

    private static final String TAG = GLTextureConverter.class.getSimpleName();

    private static final int FLOAT_SIZE_BYTES = 4;
//    private static final String VERTEX_SHADER = "uniform mat4 uSTMatrix;\n"
//            + "attribute vec4 aPosition;\n"
//            + "attribute vec4 aTextureCoord;\n"
//            + "varying vec2 vTextureCoord;\n"
//            + "void main() {\n"
//            + "    gl_Position = aPosition;\n"
//            + "    vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n"
//            + "}\n";
//    private static final String FRAGMENT_SHADER = "#extension GL_OES_EGL_image_external : require\n"
//            + "precision mediump float;\n"
//            + "varying vec2 vTextureCoord;\n"
//            + "uniform samplerExternalOES sTexture;\n"
//            + "void main() {\n"
//            + "    gl_FragColor = vec4(0.5, 0, 0, 1); //texture2D(sTexture, vTextureCoord);\n"
//            + "}\n";
    private static final String VERTEX_SHADER = "#version 320 es\n"
        + "layout (location = 0) in vec4 aPosition;\n"
        + "out vec4 positionHCS;\n"
        + "void main() {\n"
        + "    positionHCS = (vec4(aPosition.xyz, 1.0) + vec4(1.0,1.0,1.0,1.0)) * 0.5;\n"
        + "    gl_Position = vec4(aPosition.xyz, 1.0);\n"
        + "}\n";
    private static final String FRAGMENT_SHADER = "#version 320 es\n"
            + "precision mediump float;\n"
            + "in vec4 positionHCS;\n"
            + "out vec4 FragColor;\n"
            + "void main() {\n"
            + "    FragColor = vec4(0.3, 0.5, 0.8, 1);\n"
//            + "    FragColor = vec4(positionHCS.x, positionHCS.y, positionHCS.z, 1);\n"
            + "}\n";

    private static final int VERTICES_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    private static final int VERTICES_POSITION_OFFSET = 0;
    private static final int VERTICES_UV_OFFSET = 3;
    private static final float[] VERTICES = {
            // X, Y, Z, U, V
            -1.0f, -1.0f, 0.0f, 0.0f, 0.0f,
            1.0f, -1.0f, 0.0f, 1.0f, 0.0f,
            -1.0f, 1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 1.5f, 0.0f , 1.0f, 1.0f
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
    private int mVBO;
    private int mVAO;

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
            throw new RuntimeException("Could not get attrib location for aPosition");
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

        int[] vao = new int[1];
        GLES30.glGenVertexArrays(1, vao, 0);
        GLES30.glBindVertexArray(vao[0]);

        int[] vbo = new int[1];
        IntBuffer vboBuffer = IntBuffer.wrap(vbo);
        GLES20.glGenBuffers(1, vboBuffer);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);

        mVerticesBuffer.position(0);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, VERTICES.length * FLOAT_SIZE_BYTES, mVerticesBuffer, GLES20.GL_STATIC_DRAW);
        GLHelper.checkGlError(TAG, "glBufferData vertices");

        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false,
                VERTICES_STRIDE_BYTES, 0);
        GLHelper.checkGlError(TAG, "glVertexAttribPointer vertices");

        GLES20.glEnableVertexAttribArray(mPositionHandle);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES30.glBindVertexArray(0);

        mVBO = vbo[0];
        mVAO = vao[0];

        Log.d(TAG, "init vbo = " + mVBO + ", vao = " + mVAO);
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

        saveTexture(mTextureIds[0]);

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
        GLES20.glClearColor(0.0f, 0.5f, 0.5f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        // Use the program
        GLES20.glUseProgram(mProgram);
        GLHelper.checkGlError(TAG, "glUseProgram");

        // Set the texture
//        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
//        GLES20.glUniform1i(mTextureHandle, 0);
//        GLHelper.checkGlError(TAG, "glUniform1i mTextureHandle");

        // Set the texture uniform
        GLES20.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0);
        GLHelper.checkGlError(TAG, "glUniformMatrix4fv muSTMatrixHandle");

        // Set the vertex attributes
//        GLES20.glEnableVertexAttribArray(mPositionHandle);
//        GLHelper.checkGlError(TAG, "glEnableVertexAttribArray mPositionHandle");
//        mVerticesBuffer.position(VERTICES_POSITION_OFFSET);
//        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, VERTICES.length * 4, mVerticesBuffer, GLES20.GL_STATIC_DRAW);
//        GLHelper.checkGlError(TAG, "glBufferData");
//        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false,
//                VERTICES_STRIDE_BYTES, mVerticesBuffer);
//        GLHelper.checkGlError(TAG, "glVertexAttribPointer aPosition");

        GLES30.glBindVertexArray(mVAO);

//        GLES20.glEnableVertexAttribArray(mTextureCoordHandle);
//        GLHelper.checkGlError(TAG, "glEnableVertexAttribArray mTextureCoordHandle");
//        mVerticesBuffer.position(VERTICES_UV_OFFSET);
//        GLES20.glVertexAttribPointer(mTextureCoordHandle, 2, GLES20.GL_FLOAT, false,
//                VERTICES_STRIDE_BYTES, mVerticesBuffer);
//        GLHelper.checkGlError(TAG, "glVertexAttribPointer aTextureCoord");

        // Draw
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);
        GLHelper.checkGlError(TAG, "glDrawArrays");

        // Clean up
//        GLES20.glDisableVertexAttribArray(mPositionHandle);
//        GLES20.glDisableVertexAttribArray(mTextureCoordHandle);
//        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        GLES30.glBindVertexArray(0);

        //GLES20.glReadPixels();
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

    private int[] mTexturePixels = null;
    private Context mContext;

    public void setContext(Context c) {
        mContext = c;
    }

    private void saveTexture(int textureId) {
        int width = mOutputWidth;
        int height = mOutputHeight;

        if (mTexturePixels == null) {
            mTexturePixels = new int[width * height];
        }

        // 假设textureId是你要保存的纹理ID，width和height是纹理的尺寸
        //int[] pixels = new int[width * height];

        // 读取像素数据（这里省略了FBO和渲染到纹理的步骤）
        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, IntBuffer.wrap(mTexturePixels));
        GLHelper.checkGlError(TAG, "glReadPixels");

//        for (int i = 0; i < mTexturePixels.length; i++) {
//            int col = mTexturePixels[i];
//            int r = (col & 0x000000FF) >> 0;
//            int g = (col & 0x0000FF00) >> 8;
//            int b = (col & 0x00FF0000) >> 16;
//            int a = (col & 0xFF000000) >> 24;
//            int newCol = 0;
//        }

        // 将像素数据转换为Bitmap
        Bitmap bitmap = Bitmap.createBitmap(mTexturePixels, width, height, Bitmap.Config.ARGB_8888);

        String path = "/sdcard/Android/data/com.DefaultCompany.androidsurfaceunitybulitin/files/webview2.png";
        if (mContext != null) {
            File externalFileDir = mContext.getExternalFilesDir(null);
            if (externalFileDir != null) {
                path = externalFileDir.getPath() + "/webview2.png";
            }
        }

        // 保存Bitmap为PNG
        try (FileOutputStream fos = new FileOutputStream(path)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 清理资源
        bitmap.recycle();

        String version = GLES20.glGetString(GLES20.GL_VERSION);

        Log.d(TAG, "save texture to @2 " + path + ", " + mTexturePixels[(int)(width * 0.5 + height * 0.5)]
            + ", version = " + version + ", mPositionHandle = " + mPositionHandle);
    }
}
