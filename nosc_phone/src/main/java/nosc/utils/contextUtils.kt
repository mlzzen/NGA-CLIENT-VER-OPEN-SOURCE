package nosc.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import gov.anzong.androidnga.arouter.ARouterConstants
import sp.phone.common.appConfig
import sp.phone.mvp.model.entity.Board
import sp.phone.param.ArticleListParam
import sp.phone.param.ParamKey
import sp.phone.util.ARouterUtils
import sp.phone.util.ActivityUtils

/**
 * @author Yricky
 * @date 2021/11/15
 */

fun View.getActivity():Activity?{
    return context as? Activity
}

fun Context.showTopicList(board: Board) {
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

fun Context.startArticleActivity(param:ArticleListParam) {
    val intent = Intent()
    val bundle = Bundle()
    bundle.putParcelable(ParamKey.KEY_PARAM, param)
    intent.putExtras(bundle)
    intent.setClass(this, appConfig.articleActivityClass)
    startActivity(intent)
}