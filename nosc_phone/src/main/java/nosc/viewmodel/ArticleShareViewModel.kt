package nosc.viewmodel

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.alibaba.fastjson.JSON
import gov.anzong.androidnga.R
import nosc.api.ApiResult
import nosc.api.ERR
import nosc.api.OK
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
 * @author yangyihang
 */
class ArticleShareViewModel : ViewModel() {
    val replyCount = MutableLiveData<Int>()
    val refreshPage = MutableLiveData<Int>()
    val cachePage = MutableLiveData<Int>()
    val topicOwner = MutableLiveData<String>()
    fun setReplyCount(replyCount: Int) {
        this.replyCount.value = replyCount
    }

    fun setRefreshPage(refreshPage: Int) {
        this.refreshPage.value = refreshPage
    }

    fun setCachePage(cachePage: Int) {
        this.cachePage.value = cachePage
    }

    fun setTopicOwner(owner: String) {
        topicOwner.value = owner
    }


    private val mLikeTask: LikeTask by lazy{ LikeTask() }

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

    fun postComment(param: ArticleListParam, row: ThreadRowInfo):Pair<String, Bundle> {
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

    fun postLikeTask(tid: Int, pid: Int,action:Int, callBack: OnSimpleHttpCallBack<Int>) {
        mLikeTask.execute(
            tid, pid, action
        ) { result: ApiResult<String> ->
            when(result){
                is OK ->{
                    try {
                        val obj = JSON.parseObject(result.result).getJSONObject("data")
                        // 显示操作提示信息
                        ToastUtils.success(obj.getString("0"))
                        // 点赞/取消点赞操作
                        callBack.onResult(obj.getInteger("1"))
                    }catch (e:Throwable){
                        ToastUtils.error(result.result)
                    }

                }
                is ERR ->{
                    ToastUtils.error(result.msg)
                }
            }
        }
    }
}