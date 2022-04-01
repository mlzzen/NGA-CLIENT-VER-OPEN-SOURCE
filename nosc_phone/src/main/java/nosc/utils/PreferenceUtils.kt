package nosc.utils

import android.content.SharedPreferences
import com.alibaba.fastjson.JSON
import java.lang.NullPointerException
import java.util.ArrayList

/**
 * @author Justwen
 */
object PreferenceUtils {
    private val sPreferences: SharedPreferences by lazy{ ContextUtils.getDefaultSharedPreferences() }

    @JvmStatic
    fun putData(key: String?, value: String?) {
        sPreferences.edit().putString(key, value).apply()
    }

    @JvmStatic
    fun putData(key: String?, value: Int) {
        sPreferences.edit().putInt(key, value).apply()
    }

    @JvmStatic
    fun getData(key: String?, defValue: String?): String? {
        return sPreferences.getString(key, defValue)
    }

    @JvmStatic
    fun getData(key: String?, defValue: Boolean): Boolean {
        return sPreferences.getBoolean(key, defValue)
    }

    @JvmStatic
    fun getData(key: String?, defValue: Float): Float {
        return sPreferences.getFloat(key, defValue)
    }

    @JvmStatic
    fun getData(key: String?, defValue: Long): Long {
        return sPreferences.getLong(key, defValue)
    }

    @JvmStatic
    fun getData(key: String?, defValue: Int): Int {
        return sPreferences.getInt(key, defValue)
    }

    @JvmStatic
    fun getData(key: String?, defValue: Set<String?>?): Set<String>? {
        return sPreferences.getStringSet(key, defValue)
    }

    @JvmStatic
    fun <T> getData(key: String?, clz: Class<T>?): List<T> {
        return try {
            val value = sPreferences.getString(key, null)
            if (value == null) {
                ArrayList()
            } else {
                JSON.parseArray(value, clz)
            }
        } catch (e: NullPointerException) {
            ArrayList()
        }
    }

    fun edit(): SharedPreferences.Editor {
        return sPreferences.edit()
    }
}