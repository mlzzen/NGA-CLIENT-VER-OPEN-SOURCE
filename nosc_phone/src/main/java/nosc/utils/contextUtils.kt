package nosc.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import gov.anzong.androidnga.arouter.ARouterConstants
import gov.anzong.androidnga.base.util.ContextUtils
import gov.anzong.androidnga.base.util.ToastUtils
import sp.phone.common.PhoneConfiguration
import sp.phone.common.User
import sp.phone.mvp.model.entity.Board
import sp.phone.param.ParamKey
import sp.phone.util.*
import java.lang.Exception

/**
 * @author Yricky
 * @date 2021/11/15
 */

fun View.getActivity():Activity?{
    return context as? Activity
}

fun Activity.showTopicList(board: Board) {
    ARouterUtils.build(ARouterConstants.ACTIVITY_TOPIC_LIST)
        .withInt(ParamKey.KEY_FID, board.fid)
        .withInt(ParamKey.KEY_STID, board.stid)
        .withString(ParamKey.KEY_TITLE, board.name)
        .withString(ParamKey.BOARD_HEAD, board.boardHead)
        .navigation(this)
}

fun Activity.jumpToLogin() {
    ARouter.getInstance().build(ARouterConstants.ACTIVITY_LOGIN).navigation(this, ActivityUtils.REQUEST_CODE_LOGIN)
}

/**
 * 跳转到对应版块
 *
 * @param position
 * @param fidString
 */
fun Activity.toTopicListPage(position: Int, fidString: String) {
    if (position != 0 && HttpUtil.HOST_PORT != "") {
        HttpUtil.HOST = HttpUtil.HOST_PORT + HttpUtil.Servlet_timer
    }
    var fid = 0
    try {
        fid = fidString.toInt()
    } catch (e: Exception) {
        val tag: String = this.javaClass.simpleName
        NLog.e(tag, NLog.getStackTraceString(e))
        NLog.e(tag, "invalid fid $fidString")
    }
    if (fid == 0) {
        val tip = fidString + "绝对不存在"
        ToastUtils.info(tip)
        return
    }
    NLog.i(this.javaClass.simpleName, "set host:" + HttpUtil.HOST)
    var url = ForumUtils.getAvailableDomain() + "/thread.php?fid=" + fidString + "&rss=1"
    val config = PhoneConfiguration.getInstance()
    if (!StringUtils.isEmpty(config.cookie)) {
        url = url + "&" + config.cookie.replace("; ", "&")
    } else if (fid < 0) {
        jumpToLogin()
        return
    }
    if (!StringUtils.isEmpty(url)) {
        val intent = Intent()
        intent.putExtra("tab", "1")
        intent.putExtra("fid", fid)
        intent.setClass(this, config.topicActivityClass)
        startActivity(intent)
    }
}


fun Activity.startUserProfile(userId: String?) {
    ARouterUtils.build(ARouterConstants.ACTIVITY_PROFILE)
        .withString("uid", userId)
        .navigation(this)
}


fun Activity.startArticleActivity(tid: String, title: String?) {
    ARouterUtils.build(ARouterConstants.ACTIVITY_TOPIC_CONTENT)
        .withInt(ParamKey.KEY_TID, tid.toInt())
        .withString(ParamKey.KEY_TITLE, title)
        .navigation(this)
}