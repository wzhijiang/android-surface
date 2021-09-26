package io.github.wzhijiang.android.surfacetest;

import androidx.appcompat.app.AppCompatActivity;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.Window;

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

        mSurfaceView = new GLSurfaceView(this);
        mSurfaceView.setEGLContextClientVersion(2);
        mSurfaceView.setRenderer(mRenderer);

        setContentView(mSurfaceView);

        mWebView = SimpleWebView.createWebView(this, "https://www.google.com/");
        mHandler = new Handler(Looper.getMainLooper());
    }

    private void drawSurface() {
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Log.d("MainActivity", "drawSurface");
                mWebView.drawSurface();
                mHandler.postDelayed(this, 100);
            }
        };

        mHandler.postDelayed(runnable, 100);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mWebView != null) {
            mWebView.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
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

            mHandler.postDelayed(() -> {
                drawSurface();
            }, 1000);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            GLES20.glViewport(0, 0, width, height);

            mExternalTexture = new ExternalTexture(mHandler, width, height);
            mSurface = new Surface(mExternalTexture.getSurfaceTexture());

            mWebView.setSurface(mSurface);
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
    };
}