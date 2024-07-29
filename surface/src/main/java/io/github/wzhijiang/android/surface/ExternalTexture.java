package io.github.wzhijiang.android.surface;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

public class ExternalTexture implements SurfaceTexture.OnFrameAvailableListener {

    private static final String TAG = ExternalTexture.class.getSimpleName();

    private SurfaceTexture mSurfaceTexture;
    private int mSurfaceNeedsUpdate = 0;
    private boolean mHasFirstFrame = false;

    private final float[] mSTMatrix = new float[16];
    private int[] mOESTextureIds;
    private int mTextureId;
    private long mTimestampNs = -1;

    private int mWidth;
    private int mHeight;

    private boolean mOutputOESTexture;
    private GLTextureConverter mTextureConverter;

    /**
     * Construct a new ExternalTexture to stream images to a given OpenGL texture.
     *
     * @param handler The handler on which the listener should be invoked, or null
     * to use an arbitrary thread.
     *
     * In Android Q (Android 10), the handler should better be created in the GLThread in which
     * `Looper.myLooper()` should not be null.
     *
     * @param width
     * @param height
     */
    public ExternalTexture(Handler handler, int width, int height, boolean outputOESTexture) {
        mWidth = width;
        mHeight = height;
        mOutputOESTexture = outputOESTexture;

        mOESTextureIds = new int[1];
        GLES20.glGenTextures(1, mOESTextureIds, 0);

        createSurfaceTexture(mOESTextureIds[0], handler, mWidth, mHeight);

        if (!mOutputOESTexture) {
            mTextureConverter = new GLTextureConverter();
            mTextureConverter.setOutputResolution(mWidth, mHeight);
        }
    }

    public  ExternalTexture(Handler handler, int width, int height) {
        this(handler, width, height, true);
    }

    private void createSurfaceTexture(int textureId, Handler handler, int width, int height) {
        mTextureId = textureId;
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureId);
        GLHelper.checkGlError(TAG, "glBindTexture: " + mTextureId);

        GLES20.glTexParameterf(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        mSurfaceTexture = new SurfaceTexture(mTextureId);
        mSurfaceTexture.setDefaultBufferSize(width, height);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && handler != null) {
            mSurfaceTexture.setOnFrameAvailableListener(this, handler);
        } else {
            mSurfaceTexture.setOnFrameAvailableListener(this);
        }

        Log.i(TAG, "Surface texture created: " + mTextureId);
    }

    public SurfaceTexture getSurfaceTexture() {
        return mSurfaceTexture;
    }

    public int getTextureId() {
        if (mTextureConverter != null) {
            return mTextureConverter.getTextureId();
        }
        return mTextureId;
    }

    public float[] getSTMatrix() {
        return mSTMatrix;
    }

    public long getTimestampNs() {
        return mTimestampNs;
    }

    public synchronized TextureHandle getTextureHandle() {
        return new TextureHandle(mTextureId, mSTMatrix);
    }

    public synchronized boolean isPlaybackStarted() {
        return mHasFirstFrame;
    }

    public synchronized void prepareForNewPlaying() {
        mHasFirstFrame = false;
    }

    public void release() {
        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
        }
        if (mOESTextureIds != null) {
            GLES20.glDeleteTextures(mOESTextureIds.length, mOESTextureIds, 0);
            mOESTextureIds[0] = 0;
        }

        if (mTextureConverter != null) {
            mTextureConverter.release();
        }
    }

    public synchronized boolean updateTexture() {
        if (mSurfaceNeedsUpdate > 0) {
            mSurfaceTexture.updateTexImage();
            mSurfaceTexture.getTransformMatrix(mSTMatrix);
            mTimestampNs = mSurfaceTexture.getTimestamp();

            if (mTextureConverter != null) {
                mTextureConverter.drawToTexture(mOESTextureIds[0]);
            }

            mSurfaceNeedsUpdate--;
            return true;
        }
        return false;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        synchronized (this) {
            mSurfaceNeedsUpdate++;
        }
        mHasFirstFrame = true;
    }

    class FramebufferUtils {

        public void createFramebuffer(int[] textures, int[] frameBuffers, int width, int height) {
            GLES20.glGenTextures(1, textures, 0);
            GLES20.glGenFramebuffers(1, frameBuffers, 0);

            // Set up the texture
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
            GLHelper.checkGlError(TAG, "glBindTexture: " + textures[0]);
            GLES20.glTexImage2D(
                    GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA,
                    GLES20.GL_UNSIGNED_BYTE, null);
            GLHelper.checkGlError(TAG, "glTexImage2D");

            GLES20.glTexParameterf(
                    GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(
                    GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(
                    GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(
                    GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            
            // Set up the framebuffer
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffers[0]);
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                    GLES20.GL_TEXTURE_2D, textures[0], 0);
            GLHelper.checkGlError(TAG, "glFramebufferTexture2D");

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        }

        public void releaseFramebuffer(int[] textures, int[] frameBuffers) {
            GLES20.glDeleteTextures(1, textures, 0);
            GLES20.glDeleteFramebuffers(1, frameBuffers, 0);
        }
    }
}
