package nosc.utils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import androidx.core.content.ContextCompat
import gov.anzong.androidnga.R
import gov.anzong.androidnga.base.util.ContextUtils
import sp.phone.theme.ThemeManager
import sp.phone.util.NLog

/**
 * @author Yricky
 * @date 2022/3/22
 */

fun Activity.applyNavBarColor(){
    try {
        if (ThemeManager.getInstance().isNightMode) {
            window.navigationBarColor = ContextUtils.getColor(R.color.background_color)
        } else {
            // Set Transparent as the default color to match the theme
            window.navigationBarColor = Color.TRANSPARENT
        }
    } catch (e: Exception) {
        NLog.e("set navigation bar color exception occur: $e")
    }
}

fun Context.primaryColor():Int{
    val typedValue = TypedValue()
    theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)
    return ContextCompat.getColor(this, typedValue.resourceId)
}

fun Context.accentColor():Int{
    val typedValue = TypedValue()
    theme.resolveAttribute(android.R.attr.colorAccent, typedValue, true)
    return ContextCompat.getColor(this, typedValue.resourceId)
}