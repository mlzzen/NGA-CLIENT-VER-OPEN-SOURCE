package sp.phone.view.webview

import android.content.Context
import android.webkit.WebViewClient
import com.alibaba.android.arouter.launcher.ARouter
import gov.anzong.androidnga.arouter.ARouterConstants
import android.webkit.WebView
import android.content.Intent
import android.net.Uri
import android.webkit.WebResourceRequest
import gov.anzong.androidnga.activity.TopicListActivity
import gov.anzong.androidnga.gallery.ImageZoomActivity
import gov.anzong.androidnga.activity.ArticleListActivity
import gov.anzong.androidnga.R
import nosc.utils.ContextUtils
import sp.phone.util.ForumUtils
import sp.phone.util.StringUtils
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.util.ArrayList

class WebViewClientEx : WebViewClient() {
    private var mImgUrlList: MutableList<String> = ArrayList()
    private var fallbackRead = false
    fun setImgUrls(list: List<String>) {
        mImgUrlList.clear()
        mImgUrlList.addAll(list)
    }

    fun setFallbackRead(fallbackRead: Boolean) {
        this.fallbackRead = fallbackRead
    }

    private fun overrideProfileUrlLoading(context: Context, url: String): Boolean {
        for (profileStart in NGA_USER_PROFILE_START) if (url.startsWith(profileStart)) {
            var data = StringUtils.getStringBetween(
                url, 0,
                profileStart, NGA_USER_PROFILE_END
            ).result
            try {
                data = URLDecoder.decode(data, "utf-8")
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
            if (!StringUtils.isEmpty(data)) {
                ARouter.getInstance()
                    .build(ARouterConstants.ACTIVITY_PROFILE)
                    .withString("mode", "username")
                    .withString("username", data)
                    .navigation(context)
            }
            return true
        }
        return true
    }

    override fun shouldOverrideUrlLoading(view: WebView, webResourceRequest: WebResourceRequest): Boolean {
        var url = webResourceRequest.url.toString()
        val context = view.context
        if (!url.startsWith("http") && !url.startsWith("market")) {
            url = "http://$url"
        }
        for (thread in sThreadPrefix) {
            if (url.startsWith(thread, "http://".length)
                || url.startsWith(thread, "https://".length)
            ) {
                val intent = Intent()
                intent.data = Uri.parse(url)
                intent.setClass(context, TopicListActivity::class.java)
                context.startActivity(intent)
                return true
            }
        }
        for (suffix in SUFFIX_IMAGE) {
            if (url.endsWith(suffix)) {
                val intent = Intent()
                if (mImgUrlList.isEmpty()) {
                    mImgUrlList.add(url)
                }
                intent.putExtra(ImageZoomActivity.KEY_GALLERY_URLS, mImgUrlList.toTypedArray())
                intent.putExtra(ImageZoomActivity.KEY_GALLERY_INDEX, mImgUrlList.indexOf(url))
                intent.setClass(context, ImageZoomActivity::class.java)
                context.startActivity(intent)
                return true
            }
        }
        if (!overrideProfileUrlLoading(context, url)) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            val isSafeIntent = context.packageManager.queryIntentActivities(intent, 0).size > 0
            if (isSafeIntent) {
                context.startActivity(intent)
            }
            return true
        }
        if (fallbackRead) {
            return false
        } else {
            for (read in sReadPrefix) {
                if (url.startsWith(read, "http://".length)
                    || url.startsWith(read, "https://".length)
                ) {
                    val intent = Intent()
                    intent.data = Uri.parse(url)
                    intent.putExtra("fromreplyactivity", 1)
                    intent.setClass(context, ArticleListActivity::class.java)
                    context.startActivity(intent)
                    return true
                }
            }
        }
        return true
    }

    override fun onPageFinished(view: WebView, url: String) {
        view.settings.blockNetworkImage = false
        super.onPageFinished(view, url)
    }

    companion object {
        private val NGA_USER_PROFILE_START = ForumUtils.getAllDomains().flatMap {
            listOf("$it/nuke.php?func=ucp&username=","$it/nuke.php?func=ucp&username=")
        }
        private const val NGA_USER_PROFILE_END = "&"
        private val SUFFIX_IMAGE = arrayOf(".gif", ".jpg", ".png", ".jpeg", ".bmp")
        private const val NGA_READ = "/read.php?"
        private const val NGA_THREAD = "/thread.php?"
        private val sReadPrefix: List<String>
        private val sThreadPrefix: List<String>

        init {
            val domains =
                ContextUtils.getContext().resources.getStringArray(R.array.nga_domain_no_http)
            sThreadPrefix = domains.map { "$it$NGA_THREAD" }
            sReadPrefix = domains.map { "$it$NGA_READ" }
        }
    }
}