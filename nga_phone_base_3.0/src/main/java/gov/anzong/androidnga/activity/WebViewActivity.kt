package gov.anzong.androidnga.activity

import gov.anzong.androidnga.activity.BaseActivity
import android.webkit.WebView
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import gov.anzong.androidnga.R
import android.webkit.WebViewClient
import sp.phone.view.webview.WebViewClientEx
import android.webkit.WebSettings
import android.webkit.WebChromeClient

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
        load()
    }

    private val path: String?
        private get() = intent.getStringExtra("path")

    private fun load() {
        val uri = path
        val settings = mWebView!!.settings
        if (uri!!.endsWith(".swf")) {
            mWebView?.webChromeClient = WebChromeClient()
            //settings.setPluginState(PluginState.ON);
            mWebView?.loadUrl(uri)
        } else { //images
            settings.setSupportZoom(true)
            settings.javaScriptEnabled = true
            //settings.builtInZoomControls = true
            settings.loadWithOverviewMode = true
            mWebView?.loadUrl(uri)
        }
    }

    override fun onStop() {
        mWebView!!.stopLoading()
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