package nosc.utils

import com.alibaba.fastjson.JSON
import java.lang.NullPointerException
import java.util.ArrayList

/**
 * @author Yricky
 * @date 2021/12/1
 */

fun <T> parseArray(str: String, clz: Class<T>): MutableList<T> {
    return try {
        JSON.parseArray(str, clz)
    } catch (e: NullPointerException) {
        ArrayList()
    }
}