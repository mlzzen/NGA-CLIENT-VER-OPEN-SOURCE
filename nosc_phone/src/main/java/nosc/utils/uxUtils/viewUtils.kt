package nosc.utils.uxUtils

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import gov.anzong.androidnga.R
import sp.phone.view.webview.LocalWebView

/**
 * @author Yricky
 * @date 2022/4/2
 */

fun View.OnClickListener.withClickCd(cdMillis:Long):View.OnClickListener{
    val l = this
    if(cdMillis <= 0 ){
        return l
    }
    return object : View.OnClickListener {
        var lastClickTime: Long = 0
        override fun onClick(v: View) {
            val now = System.currentTimeMillis()
            if (now - lastClickTime > cdMillis) {
                lastClickTime = now
                l.onClick(v)
            }
        }
    }
}

fun (()->Unit).withClickCd(cdMillis:Long):View.OnClickListener{
    val l = this
    if(cdMillis <= 0 ){
        return View.OnClickListener { l() }
    }
    return object : View.OnClickListener {
        var lastClickTime: Long = 0
        override fun onClick(v: View) {
            val now = System.currentTimeMillis()
            if (now - lastClickTime > cdMillis) {
                lastClickTime = now
                l()
            }
        }
    }
}

fun Context.createLocalWebView(): LocalWebView {
    val localWebView = LocalWebView(this)
    val lp = FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    lp.marginStart = resources.getDimensionPixelSize(R.dimen.material_standard_half)
    lp.marginEnd = resources.getDimensionPixelSize(R.dimen.material_standard_half)
    localWebView.layoutParams = lp
    localWebView.isVerticalScrollBarEnabled = false
    localWebView.isHorizontalScrollBarEnabled = false
    return localWebView
}