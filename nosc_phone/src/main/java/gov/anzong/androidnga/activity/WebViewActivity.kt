package gov.anzong.androidnga.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.*
import gov.anzong.androidnga.R
import sp.phone.common.appConfig
import sp.phone.view.webview.WebViewClientEx

open class WebViewActivity : BaseActivity() {
    private var mWebView: WebView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview_layout)
        setupActionBar()
        mWebView = findViewById(R.id.webview)
        val client: WebViewClient = WebViewClientEx(this)
        mWebView?.webViewClient = client
        title = intent.getStringExtra("title")
        CookieManager.getInstance().apply {
            setCookie(path, appConfig.cookie)
        }
        load()
    }

    private val path: String get() = intent.getStringExtra("path") ?: "about:blank"

    private fun load() {
        val uri = path
        val settings = mWebView?.settings
        settings?.setSupportZoom(true)
        settings?.javaScriptEnabled = true
        settings?.loadWithOverviewMode = true
        mWebView?.loadUrl(uri)
    }

    override fun onStop() {
        mWebView?.stopLoading()
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menuInflater.inflate(R.menu.webview_option_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.item_refresh) {
            load()
        }
        return super.onOptionsItemSelected(item)
    }
}