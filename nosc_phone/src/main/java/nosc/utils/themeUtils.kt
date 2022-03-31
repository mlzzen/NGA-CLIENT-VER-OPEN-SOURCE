package nosc.utils

import android.app.Activity
import android.graphics.Color
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