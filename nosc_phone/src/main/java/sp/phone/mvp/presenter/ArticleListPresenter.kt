package sp.phone.mvp.presenter

import android.os.Bundle
import com.alibaba.fastjson.JSON
import gov.anzong.androidnga.R
import nosc.api.bean.ThreadRowInfo
import nosc.api.callbacks.OnSimpleHttpCallBack
import nosc.utils.ContextUtils
import nosc.utils.uxUtils.ToastUtils
import sp.phone.common.UserManagerImpl
import sp.phone.param.ArticleListParam
import sp.phone.task.LikeTask
import sp.phone.util.FunctionUtils
import sp.phone.util.StringUtils

/**
 * Created by Justwen on 2017/11/22.
 */
@Deprecated("")
class ArticleListPresenter {
    private var mLikeTask: LikeTask? = null

    fun banThisSB(row: ThreadRowInfo) {
        if (row.isanonymous) {
            ToastUtils.warn(R.string.cannot_add_to_blacklist_cause_anony)
        } else {
            val um = UserManagerImpl.getInstance()
            if (row._isInBlackList) {
                row.set_IsInBlackList(false)
                um.removeFromBlackList(row.authorid.toString())
                ToastUtils.success(R.string.remove_from_blacklist_success)
            } else {
                row.set_IsInBlackList(true)
                um.addToBlackList(row.author, row.authorid.toString())
                ToastUtils.success(R.string.add_to_blacklist_success)
            }
        }
    }

    fun postComment(param: ArticleListParam, row: ThreadRowInfo):Pair<String,Bundle> {
        val quoteRegex = "\\[quote\\]([\\s\\S])*\\[/quote\\]"
        val replayRegex =
            "\\[b\\]Reply to \\[pid=\\d+,\\d+,\\d+\\]Reply\\[/pid\\] Post by .+?\\[/b\\]"
        val postPrefix = StringBuilder()
        var content: String? = row.content
            .replace(quoteRegex.toRegex(), "")
            .replace(replayRegex.toRegex(), "")
        val postTime = row.postdate
        content = FunctionUtils.checkContent(content)
        content = StringUtils.unEscapeHtml(content)
        val name = row.author
        val uid = row.authorid.toString()
        val tidStr = param.tid.toString()
        if (row.pid != 0) {
            postPrefix.append("[quote][pid=")
                .append(row.pid)
                .append(',').append(tidStr).append(",").append(param.page)
                .append("]") // Topic
                .append("Reply")
            if (row.isanonymous) { // 是匿名的人
                postPrefix.append("[/pid] [b]Post by [uid=")
                    .append("-1")
                    .append("]")
                    .append(name)
                    .append("[/uid][color=gray](")
                    .append(row.lou)
                    .append("楼)[/color] (")
            } else {
                postPrefix.append("[/pid] [b]Post by [uid=")
                    .append(uid)
                    .append("]")
                    .append(name)
                    .append("[/uid] (")
            }
            postPrefix.append(postTime)
                .append("):[/b]\n")
                .append(content)
                .append("[/quote]\n")
        }
        val bundle = Bundle()
        bundle.putInt("pid", row.pid)
        bundle.putInt("fid", row.fid)
        bundle.putInt("tid", param.tid)
        var prefix = StringUtils.removeBrTag(postPrefix.toString())
        if (!StringUtils.isEmpty(prefix)) {
            prefix = """
                $prefix
                
                """.trimIndent()
        }
        return Pair(prefix, bundle)
    }

    fun postSupportTask(tid: Int, pid: Int, callBack: OnSimpleHttpCallBack<Int>) {
        if (mLikeTask == null) {
            mLikeTask = LikeTask()
        }
        mLikeTask!!.execute(
            tid, pid, LikeTask.SUPPORT
        ) { result: String ->
            if (result.isEmpty()) {
                // 无返回数据，网络请求失败
                ContextUtils.getString(R.string.network_error)
            } else {
                val obj = JSON.parseObject(result).getJSONObject("data")
                // 显示操作提示信息
                ToastUtils.success(obj.getString("0"))
                // 点赞/取消点赞操作
                callBack.onResult(obj.getInteger("1"))
            }
        }
    }

    fun postOpposeTask(tid: Int, pid: Int) {
        if (mLikeTask == null) {
            mLikeTask = LikeTask()
        }
        mLikeTask!!.execute(tid, pid, LikeTask.OPPOSE) { text: String? -> ToastUtils.success(text) }
    }

    //    @Override
    //    public void quote(ArticleListParam param, ThreadRowInfo row) {
    //        final String quoteRegex = "\\[quote\\]([\\s\\S])*\\[/quote\\]";
    //        final String replayRegex = "\\[b\\]Reply to \\[pid=\\d+,\\d+,\\d+\\]Reply\\[/pid\\] Post by .+?\\[/b\\]";
    //        StringBuilder postPrefix = new StringBuilder();
    //        String content = row.getContent()
    //                .replaceAll(quoteRegex, "")
    //                .replaceAll(replayRegex, "");
    //        final String postTime = row.getPostdate();
    //        String mention = null;
    //        final String name = row.getAuthor();
    //        final String uid = String.valueOf(row.getAuthorid());
    //        content = FunctionUtils.checkContent(content);
    //        content = StringUtils.unEscapeHtml(content);
    //        String tidStr = String.valueOf(param.tid);
    //        if (row.getPid() != 0) {
    //            mention = name;
    //            postPrefix.append("[quote][pid=")
    //                    .append(row.getPid())
    //                    .append(',').append(tidStr).append(",").append(param.page)
    //                    .append("]")// Topic
    //                    .append("Reply");
    //            if (row.getISANONYMOUS()) {// 是匿名的人
    //                postPrefix.append("[/pid] [b]Post by [uid=")
    //                        .append("-1")
    //                        .append("]")
    //                        .append(name)
    //                        .append("[/uid][color=gray](")
    //                        .append(row.getLou())
    //                        .append("楼)[/color] (");
    //            } else {
    //                postPrefix.append("[/pid] [b]Post by [uid=")
    //                        .append(uid)
    //                        .append("]")
    //                        .append(name)
    //                        .append("[/uid] (");
    //            }
    //            postPrefix.append(postTime)
    //                    .append("):[/b]\n")
    //                    .append(content)
    //                    .append("[/quote]\n");
    //        }
    //
    //        Intent intent = new Intent();
    //        if (!StringUtils.isEmpty(mention)) {
    //            intent.putExtra("mention", mention);
    //        }
    //        intent.putExtra("prefix", StringUtils.removeBrTag(postPrefix.toString()));
    //        intent.putExtra("tid", tidStr);
    //        intent.putExtra("action", "reply");
    //        mBaseView.startPostActivity(intent);
    //    }
}