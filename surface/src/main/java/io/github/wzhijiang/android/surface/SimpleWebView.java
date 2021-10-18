package io.github.wzhijiang.android.surface;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SimpleWebView extends WebView {

    private static final boolean DEBUG = true;
    private static final boolean DEBUG_DRAW = false;

    private Surface mSurface;
    private FrameLayout mWebLayout;
    private MotionEventWrapper mEventWrapper;

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

    public void init() {
        mEventWrapper = new MotionEventWrapper();

        setLayerType(View.LAYER_TYPE_HARDWARE, null);

        initSettings();

        setWebViewClient(mWebViewClient);

        Log.i(BuildConfig.LOG_TAG, "webview inited");
    }

    private void initSettings() {
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
            if (DEBUG_DRAW) {
                Log.v(BuildConfig.LOG_TAG, "webview draw" );
            }

            glAttachedCanvas.scale(getScaleX(), getScaleY());
            glAttachedCanvas.translate(-getScrollX(), -getScrollY());

            super.draw(glAttachedCanvas);
        } else {
            super.draw(canvas);
            return;
        }

        mSurface.unlockCanvasAndPost(glAttachedCanvas);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return false;
    }

    public boolean dispatchTouchEvent(int x, int y, int action) {
        MotionEvent ev = mEventWrapper.genTouchEvent(x, y, action);

        if (DEBUG) {
            Log.d(BuildConfig.LOG_TAG, "touched: " + ev.toString());
        }

        return super.dispatchTouchEvent(ev);
    }

    public void resize(int width, int height) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mWebLayout.getLayoutParams();
        params.height = height;
        params.width = width;
        mWebLayout.setLayoutParams(params);
    }

    public WebViewClient mWebViewClient = new WebViewClient() {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            final String urlString = request.getUrl().toString();
            if (urlString == null || urlString.startsWith("http://") || urlString.startsWith("https://")) {
                return false;
            }

            return true;
        }
    };

    public static SimpleWebView create(Context context, int width, int height) {
        LayoutInflater inflater = LayoutInflater.from(context);
        FrameLayout layout = (FrameLayout)inflater.inflate(R.layout.web_layout, null, false);

        SimpleWebView webView = layout.findViewById(R.id.web_view);
        webView.setWebLayout(layout);
        webView.init();

        return webView;
    }

    public static SimpleWebView create(Context context, ViewGroup parent, int width, int height, int zorder) {
        SimpleWebView webView = create(context, width, height);
        parent.addView(webView.getWebLayout(), zorder);

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) webView.getWebLayout().getLayoutParams();
        params.height = height;
        params.width = width;
        webView.getWebLayout().setLayoutParams(params);

        return webView;
    }
}
