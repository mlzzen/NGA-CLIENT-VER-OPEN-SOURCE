package nosc.utils

import com.alibaba.fastjson.JSON
import java.lang.NullPointerException
import java.util.ArrayList

/**
 * @author Yricky
 * @date 2021/12/1
 */

object JsonUtils{
    inline fun <reified T> parseArray(str: String): MutableList<T> {
        return try {
            JSON.parseArray(str, T::class.java)
        } catch (e: NullPointerException) {
            ArrayList()
        }
    }

    inline fun <reified T> parse(str: String): T? {
        return try {
            JSON.parseObject(str,T::class.java)
        } catch (e: NullPointerException) {
            null
        }
    }
}

