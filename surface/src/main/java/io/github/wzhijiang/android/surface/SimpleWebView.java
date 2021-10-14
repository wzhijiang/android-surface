package io.github.wzhijiang.android.surface;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SimpleWebView extends WebView {

    private Surface mSurface;
    private FrameLayout mWebLayout;

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

    private void setWebLayout(FrameLayout layout) {
        mWebLayout = layout;
    }

    public FrameLayout getWebLayout() {
        return mWebLayout;
    }

    public void setSurface(Surface surface) {
        mSurface = surface;
    }

    @Override
    public void draw(Canvas canvas) {
        if (mSurface == null) {
            super.draw(canvas);
            return;
        }

        Canvas glAttachedCanvas = mSurface.lockCanvas(null);
        if (glAttachedCanvas != null) {
            super.draw(glAttachedCanvas);
        } else {
            super.draw(canvas);
            return;
        }

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

    public static SimpleWebView create(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        FrameLayout layout = (FrameLayout)inflater.inflate(R.layout.web_layout, null, false);

        SimpleWebView webView = layout.findViewById(R.id.web_view);
        webView.setWebLayout(layout);
        webView.initSettings();

        return webView;
    }

    public static SimpleWebView create(Context context, ViewGroup parent, int width, int height, int zorder) {
        SimpleWebView webView = create(context);
        parent.addView(webView.getWebLayout(), zorder);

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) webView.getWebLayout().getLayoutParams();
        params.height = height;
        params.width = width;
        webView.getWebLayout().setLayoutParams(params);

        return webView;
    }
}
