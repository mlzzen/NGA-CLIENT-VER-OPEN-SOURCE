package gov.anzong.androidnga.fragment

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.ProgressBar
import gov.anzong.androidnga.R
import nosc.utils.uxUtils.ToastUtils
import sp.phone.common.UserManagerImpl
import sp.phone.util.ForumUtils
import sp.phone.util.StringUtils
import java.io.UnsupportedEncodingException
import java.net.URLDecoder

/**
 * Created by Justwen on 2017/7/5.
 */
class LoginWebFragment : BaseFragment() {
    private var mProgressBar: ProgressBar? = null
    private var mWebView: WebView? = null
    private val handler = Handler(Looper.getMainLooper())
    private val r: Runnable = object : Runnable {
        override fun run() {
            setCookies()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CookieManager.getInstance().removeAllCookies(null)
        ToastUtils.info("不支持QQ和微博登录")
    }

    private inner class LoginWebChromeClient : WebChromeClient() {
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            if (newProgress in 1 until MAX_PROGRESS) {
                mProgressBar!!.visibility = View.VISIBLE
            } else if (newProgress >= MAX_PROGRESS) {
                mProgressBar!!.visibility = View.GONE
            }
            mProgressBar!!.progress = newProgress
            super.onProgressChanged(view, newProgress)
        }
    }

    private class LoginWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            return shouldOverrideUrlLoading(view, request.url.toString())
        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (mWebView != null) {
            mWebView!!.destroy()
        }
        return inflater.inflate(R.layout.fragment_login_web, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mWebView = view.findViewById(R.id.webview)
        mWebView?.webChromeClient = LoginWebChromeClient()
        mWebView?.webViewClient = LoginWebViewClient()
        val webSettings = mWebView?.settings
        webSettings?.javaScriptEnabled = true
        webSettings?.javaScriptCanOpenWindowsAutomatically = true
        mProgressBar = view.findViewById(R.id.progressBar)
        mProgressBar?.max = MAX_PROGRESS
        mWebView?.loadUrl(URL_LOGIN)
        super.onViewCreated(view, savedInstanceState)
    }

    /**
     * Called when the fragment is visible to the user and actively running. Resumes the WebView.
     */
    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
        mWebView!!.onPause()
    }

    /**
     * Called when the fragment is no longer resumed. Pauses the WebView.
     */
    override fun onResume() {
        mWebView!!.onResume()
        handler.postDelayed(r, 100)
        super.onResume()
    }

    /**
     * Called when the fragment is no longer in use. Destroys the internal state of the WebView.
     */
    override fun onDestroy() {
        if (mWebView != null) {
            mWebView!!.destroy()
            mWebView = null
        }
        super.onDestroy()
    }

    override fun onBackPressed(): Boolean {
        return if (mWebView!!.canGoBack()) {
            mWebView!!.goBack()
            true
        } else {
            super.onBackPressed()
        }
    }

    private fun setCookies() {
        val cookieStr = CookieManager.getInstance().getCookie(
            mWebView!!.url
        )
        if (!StringUtils.isEmpty(cookieStr) && parseCookie(cookieStr)) {
            ToastUtils.success("登录成功")
            activity?.setResult(Activity.RESULT_OK)
            activity?.finish()

        }
    }

    private fun parseCookie(cookies: String): Boolean {
        if (!cookies.contains(TAG_UID)) {
            return false
        }
        var uid: String? = null
        var cid: String? = null
        var userName: String? = null
        for (cookie in cookies.split(";").toTypedArray().map {
            it.trim { it <= ' ' }
        }) {
            if (cookie.contains(TAG_UID)) {
                uid = cookie.substring(TAG_UID.length + 1)
            } else if (cookie.contains(TAG_CID)) {
                cid = cookie.substring(TAG_CID.length + 1)
            } else if (cookie.contains(TAG_USER_NAME)) {
                userName = cookie.substring(TAG_USER_NAME.length + 1)
                try {
                    userName = URLDecoder.decode(userName, "gbk")
                    userName = URLDecoder.decode(userName, "gbk")
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                }
            }
        }
        if (!StringUtils.isEmpty(cid)
            && !StringUtils.isEmpty(uid)
            && !StringUtils.isEmpty(userName)
        ) {
            saveCookie(uid, cid, userName)
            return true
        }
        return false
    }

    private fun saveCookie(uid: String?, cid: String?, userName: String?) {
        UserManagerImpl.getInstance().addUser(uid, cid, userName, "", 0)
    }

    companion object {
        val URL_LOGIN get() = "${ForumUtils.getApiDomain()}/nuke.php?__lib=login&__act=account&login"
        private const val TAG_UID = "ngaPassportUid"
        private const val TAG_CID = "ngaPassportCid"
        private const val TAG_USER_NAME = "ngaPassportUrlencodedUname"
        private const val MAX_PROGRESS = 100
    }
}