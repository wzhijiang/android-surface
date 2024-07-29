package io.github.wzhijiang.android.surface;

public class TextureHandle {

    private long mHandle;
    private boolean mMemoryOwn;

    private static final float[] ST_MATRIX_DEFAULT = {
            1.0f,  0.0f, 0.0f, 0.0f,
            0.0f, -1.0f, 0.0f, 0.0f,
            0.0f,  0.0f, 1.0f, 0.0f,
            0.0f,  1.0f, 0.0f, 1.0f
    };

    public TextureHandle(int textureId, float[] stMatrix, boolean isOESTexture) {
        mHandle = nativeCreateTextureHandle(textureId, stMatrix, isOESTexture);
        mMemoryOwn = true;
    }

    public TextureHandle(int textureId, float[] stMatrix) {
        this(textureId, stMatrix, true);
    }

    public TextureHandle(int textureId) {
        this(textureId, ST_MATRIX_DEFAULT, true);
    }

    @SuppressWarnings("deprecation")
    protected void finalize() {
        delete();
    }

    public synchronized void delete() {
        if (mHandle != 0) {
            if (mMemoryOwn) {
                mMemoryOwn = false;
                nativeDeleteTextureHandle(mHandle);
            }
            mHandle = 0;
        }
    }

    public long getNativePtr() {
        return mHandle;
    }

    public native long nativeCreateTextureHandle(int textureId, float[] stMatrix, boolean isOESTexture);
    public native void nativeDeleteTextureHandle(long handle);

    static {
        System.loadLibrary("igw_surface");
    }
}
