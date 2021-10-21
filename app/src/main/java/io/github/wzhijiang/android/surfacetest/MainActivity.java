package io.github.wzhijiang.android.surfacetest;

import androidx.appcompat.app.AppCompatActivity;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import io.github.wzhijiang.android.surface.ExternalTexture;
import io.github.wzhijiang.android.surface.QuadRenderer;
import io.github.wzhijiang.android.surface.SimpleWebView;
import io.github.wzhijiang.android.surface.TextureHandle;

public class MainActivity extends AppCompatActivity {

    private SimpleWebView mWebView;
    private GLSurfaceView mSurfaceView;
    private Handler mHandler;
    private ExternalTexture mExternalTexture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);

        mSurfaceView = (GLSurfaceView)findViewById(R.id.surface_view);
        mSurfaceView.setEGLContextClientVersion(2);
        mSurfaceView.setRenderer(mRenderer);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        FrameLayout rootLayout = findViewById(R.id.root_layout);
        mWebView = SimpleWebView.create(this, rootLayout, displayMetrics.widthPixels,
                displayMetrics.heightPixels, 0);
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mWebView != null) {
            return mWebView.dispatchTouchEvent((int) ev.getX(), (int) ev.getY(), ev.getAction());
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private GLSurfaceView.Renderer mRenderer = new GLSurfaceView.Renderer() {

        private QuadRenderer mQuadRenderer;

        private Surface mSurface;

        private TextureHandle mTextureHandle;

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            mQuadRenderer = new QuadRenderer();
            mQuadRenderer.initGL();
        }

        /**
         * Note that `onSurfaceChanged` will be called twice when the app launches.
         */
        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            GLES20.glViewport(0, 0, width, height);
            Log.d(BuildConfig.LOG_TAG, "Surface changed: " + width + ", " + height);

            release();

            // This is just a guess!
            //
            // In Android Q (Android 10), the setFrameAvailableHandler should better be created in
            // the GLThread in which `Looper.myLooper()` should not be null.
            //
            // However, in the GLThread attached to GLSurfaceView, `Looper.myLooper()` is null.
            //
            // TODO: Find an appropriate handler for ExternalTexture.
            Handler setFrameAvailableHandler = mHandler;

            mExternalTexture = new ExternalTexture(setFrameAvailableHandler, width, height);
            mSurface = new Surface(mExternalTexture.getSurfaceTexture());

            mHandler.post(() -> {
                mWebView.setSurface(mSurface);
                mWebView.resize(width, height);
                mWebView.loadUrl("https://www.bilibili.com/");
            });
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            mExternalTexture.updateTexture();

            if (mTextureHandle == null && mExternalTexture.isPlaybackStarted()) {
                mTextureHandle = mExternalTexture.getTextureHandle();
            }

            if (mTextureHandle != null) {
                mQuadRenderer.draw(mTextureHandle);
            }
        }

        void release() {
            if (mWebView != null) {
                mWebView.setSurface(null);
            }

            if (mSurface != null) {
                mSurface.release();
            }

            if (mExternalTexture != null) {
                mExternalTexture.release();
            }
        }
    };
}