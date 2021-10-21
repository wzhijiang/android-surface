package io.github.wzhijiang.android.surface;

public class TextureHandle {

    private long mHandle;
    private boolean mMemoryOwn;

    public TextureHandle(int textureId, float[] stMatrix) {
        mHandle = nativeCreateTextureHandle(textureId, stMatrix);
        mMemoryOwn = true;
    }

    public TextureHandle(int textureId)
    {
        float[] stMatrix = {
                1.0f,  0.0f, 0.0f, 0.0f,
                0.0f, -1.0f, 0.0f, 0.0f,
                0.0f,  0.0f, 1.0f, 0.0f,
                0.0f,  1.0f, 0.0f, 1.0f
        };

        mHandle = nativeCreateTextureHandle(textureId, stMatrix);
        mMemoryOwn = true;
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

    public native long nativeCreateTextureHandle(int textureId, float[] stMatrix);
    public native void nativeDeleteTextureHandle(long handle);

    static {
        System.loadLibrary("igw_surface");
    }
}
