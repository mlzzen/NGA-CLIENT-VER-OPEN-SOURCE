package nosc.utils

import gov.anzong.androidnga.Utils
import nosc.api.constants.ApiConstants
import sp.phone.mvp.model.entity.Board
import sp.phone.param.ArticleListParam

/**
 * @author Yricky
 * @date 2022/3/20
 */

fun ArticleListParam.toUrl(): String {
    val builder = StringBuilder()
    builder.append(Utils.getNGAHost()).append("read.php?")
    if (pid != 0) {
        builder.append("pid=").append(pid)
    } else {
        builder.append("tid=").append(tid)
    }
    return builder.toString()
}

fun Board.iconUrl():String{
    return if (stid != 0) {
        String.format(ApiConstants.URL_BOARD_ICON_STID, stid)
    } else {
        String.format(ApiConstants.URL_BOARD_ICON, fid)
    }
}