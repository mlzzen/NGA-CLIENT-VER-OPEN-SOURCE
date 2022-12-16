package nosc.utils.uxUtils

import nosc.utils.ThreadUtils
import android.widget.Toast
import androidx.annotation.StringRes
import nosc.utils.uxUtils.ToastUtils
import es.dmoral.toasty.Toasty
import nosc.utils.ContextUtils

/**
 * Created by Justwen on 2018/8/11.
 */
object ToastUtils {
    @JvmStatic
    fun flat(text: String?) {
        ThreadUtils.runOnMainThread {
            Toast.makeText(ContextUtils.getContext(), text, Toast.LENGTH_SHORT).show()
        }
    }

    @JvmStatic
    fun success(@StringRes id: Int) {
        success(ContextUtils.getString(id))
    }

    @JvmStatic
    fun error(@StringRes id: Int) {
        error(ContextUtils.getString(id))
    }

    @JvmStatic
    fun warn(@StringRes id: Int) {
        warn(ContextUtils.getString(id))
    }

    @JvmStatic
    fun info(@StringRes id: Int) {
        info(ContextUtils.getString(id))
    }

    @JvmStatic
    fun success(text: String) {
        ThreadUtils.runOnMainThread {
            Toasty.success(ContextUtils.getContext(), text).show()
        }
    }

    @JvmStatic
    fun error(text: String) {
        ThreadUtils.runOnMainThread {
            Toasty.error(ContextUtils.getContext(), text).show()
        }
    }

    @JvmStatic
    fun info(text: String) {
        ThreadUtils.runOnMainThread {
            Toasty.info(ContextUtils.getContext(), text).show()
        }
    }

    @JvmStatic
    fun warn(text: String) {
        ThreadUtils.runOnMainThread {
            Toasty.warning(ContextUtils.getContext(), text).show()
        }
    }
}