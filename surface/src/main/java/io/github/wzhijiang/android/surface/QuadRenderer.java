package io.github.wzhijiang.android.surface;

public class QuadRenderer {

    private long mHandle;
    private boolean mMemoryOwn;

    public QuadRenderer() {
        mHandle = nativeCreateQuadRenderer();
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
                nativeDeleteQuadRenderer(mHandle);
            }
            mHandle = 0;
        }
    }

    public void initGL() {
        nativeInitGL(mHandle);
    }

    public void draw(TextureHandle textureHandle) {
        nativeDraw(mHandle, textureHandle.getNativePtr());
    }

    public native long nativeCreateQuadRenderer();
    public native void nativeDeleteQuadRenderer(long handle);
    public native void nativeInitGL(long handle);
    public native void nativeDraw(long handle, long textureHandlePtr);

    static {
        System.loadLibrary("igw_surface");
    }
}
