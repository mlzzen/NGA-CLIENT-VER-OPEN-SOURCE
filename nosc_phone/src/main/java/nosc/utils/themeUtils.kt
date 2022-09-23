package nosc.utils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import androidx.core.content.ContextCompat
import gov.anzong.androidnga.R
import sp.phone.theme.ThemeManager
import sp.phone.util.NLog

/**
 * @author Yricky
 * @date 2022/3/22
 */

fun Activity.applyNavBarColor(){
    try {
        window.navigationBarColor = Color.TRANSPARENT

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

fun Context.backgroundColor():Int{
    return ContextCompat.getColor(this, R.color.background_color)
}

fun Context.backgroundColor2():Int{
    return ContextCompat.getColor(this, R.color.background_color2)
}