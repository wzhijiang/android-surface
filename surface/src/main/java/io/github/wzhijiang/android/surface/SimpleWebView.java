package io.github.wzhijiang.android.surface;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SimpleWebView extends WebView {

    private Surface mSurface;

    public SimpleWebView(@NonNull Context context) {
        super(context);
    }

    public SimpleWebView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SimpleWebView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SimpleWebView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setSurface(Surface surface) {
        mSurface = surface;
    }

    public void drawSurface() {
        if (mSurface == null) {
            return;
        }

        Canvas glAttachedCanvas = mSurface.lockCanvas(null);
        if (glAttachedCanvas == null) {
            return;
        }

        draw(glAttachedCanvas);

        mSurface.unlockCanvasAndPost(glAttachedCanvas);
    }

    public void initSettings() {
        WebSettings settings = this.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setDatabaseEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setAppCacheEnabled(true);

        // LOAD_CACHE_ELSE_NETWORK
        //   - When we enable this setting, the website acquires cached IP Address even we switch
        //     the network to vpn.
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        settings.setDefaultTextEncodingName("UTF-8");

        // 自适应屏幕
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        // 自动缩放
        settings.setBuiltInZoomControls(true);
        settings.setSupportZoom(true);
    }

    public static SimpleWebView createWebView(Activity activity, String url) {
        SimpleWebView webView = new SimpleWebView(activity);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        webView.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);

        webView.initSettings();
        webView.loadUrl(url);
        return webView;
    }
}
