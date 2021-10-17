package sp.phone.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;

import gov.anzong.androidnga.R;
import gov.anzong.androidnga.base.util.ToastUtils;
import sp.phone.mvp.presenter.LoginPresenter;
import sp.phone.util.StringUtils;

/**
 * Created by Justwen on 2017/7/5.
 */

public class LoginWebFragment extends BaseFragment {

    private static final String URL_LOGIN = "https://ngabbs.com/nuke.php?__lib=login&__act=account&login";

    private static final int MAX_PROGRESS = 100;

    private ProgressBar mProgressBar;

    private WebView mWebView;

    private LoginPresenter mLoginPresenter;

    private final Handler handler = new Handler(Looper.getMainLooper());


    private final Runnable r = new Runnable() {
        @Override
        public void run() {
            setCookies();
            handler.postDelayed(r,1000);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mLoginPresenter = new LoginPresenter();
        super.onCreate(savedInstanceState);
        CookieManager.getInstance().removeAllCookies(null);
        ToastUtils.info("不支持QQ和微博登录");
    }

    private class LoginWebChromeClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress > 0 && newProgress < MAX_PROGRESS) {
                mProgressBar.setVisibility(View.VISIBLE);
            } else if (newProgress >= MAX_PROGRESS) {
                mProgressBar.setVisibility(View.GONE);
            }
            mProgressBar.setProgress(newProgress);
            super.onProgressChanged(view, newProgress);
        }
    }

    private static class LoginWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return shouldOverrideUrlLoading(view, request.getUrl().toString());
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mWebView != null) {
            mWebView.destroy();
        }
        return inflater.inflate(R.layout.fragment_login_web, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mWebView = view.findViewById(R.id.webview);
        mWebView.setWebChromeClient(new LoginWebChromeClient());
        mWebView.setWebViewClient(new LoginWebViewClient());
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        mProgressBar = view.findViewById(R.id.progressBar);
        mProgressBar.setMax(MAX_PROGRESS);
        mWebView.loadUrl(URL_LOGIN);
        super.onViewCreated(view, savedInstanceState);
    }


    /**
     * Called when the fragment is visible to the user and actively running. Resumes the WebView.
     */
    @Override
    public void onPause() {
        //setCookies();
        super.onPause();
        handler.removeCallbacksAndMessages(null);
        mWebView.onPause();
    }

    /**
     * Called when the fragment is no longer resumed. Pauses the WebView.
     */
    @Override
    public void onResume() {
        mWebView.onResume();
        handler.postDelayed(r,100);
        super.onResume();
    }

    /**
     * Called when the fragment is no longer in use. Destroys the internal state of the WebView.
     */
    @Override
    public void onDestroy() {
        if (mWebView != null) {
            mWebView.destroy();
            mWebView = null;
        }
        super.onDestroy();
    }

    @Override
    public boolean onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        } else {
            return super.onBackPressed();
        }
    }

    private void setCookies() {
        String cookieStr = CookieManager.getInstance().getCookie(mWebView.getUrl());
        if (!StringUtils.isEmpty(cookieStr) && mLoginPresenter.parseCookie(cookieStr)) {
            ToastUtils.success("登录成功");
            if (mActivity != null) {
                mActivity.setResult(Activity.RESULT_OK);
                mActivity.finish();
            }
        }
    }
}