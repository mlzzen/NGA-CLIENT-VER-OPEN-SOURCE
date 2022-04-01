package sp.phone.mvp.model.convert

import android.text.TextUtils
import sp.phone.mvp.model.entity.TopicListInfo
import nosc.api.bean.TopicListBean
import com.alibaba.fastjson.JSON
import sp.phone.util.NLog
import sp.phone.common.UserManagerImpl
import sp.phone.common.FilterKeywordsManagerImpl
import sp.phone.common.PhoneConfiguration
import sp.phone.mvp.model.entity.SubBoard
import sp.phone.util.ForumUtils
import nosc.api.bean.TopicListBean.DataBean.TBean
import sp.phone.mvp.model.entity.ThreadPageInfo
import sp.phone.mvp.model.entity.ThreadPageInfo.ReplyInfo
import sp.phone.util.StringUtils
import java.lang.Exception
import java.lang.NullPointerException
import java.lang.StringBuilder

/**
 * Created by Justwen on 2017/11/21.
 */
object TopicConvertFactory {
    private val TAG = TopicConvertFactory::class.java.simpleName
    fun getTopicListInfo(js: String, page: Int): TopicListInfo? {
        var js = js
        if (js.startsWith("window.script_muti_get_var_store=")) {
            js = js.substring("window.script_muti_get_var_store=".length)
        }
        val topicListBean = JSON.parseObject(js, TopicListBean::class.java)
        return try {
            val listInfo = TopicListInfo()
            convertSubBoard(listInfo, topicListBean)
            convertTopic(listInfo, topicListBean, page)
            listInfo.curTime = topicListBean.time
            sort(listInfo)
            filter(listInfo)
            listInfo
        } catch (e: NullPointerException) {
            NLog.e(TAG, "can not parse :\n$js")
            null
        }
    }

    private fun filter(data: TopicListInfo) {
        val iterator = data.threadPageList.iterator()
        val blackList = UserManagerImpl.getInstance().blackList
        val filterKeywords = FilterKeywordsManagerImpl.getInstance().keywords
        while (iterator.hasNext()) {
            val pageInfo = iterator.next()
            var removed = false
            for (keyword in filterKeywords) {
                if (keyword.isEnabled && pageInfo.subject!!.contains(keyword.keyword)) {
                    iterator.remove()
                    removed = true
                    break
                }
            }
            if (removed) {
                continue
            }
            for (user in blackList) {
                if (pageInfo.author == user.nickName) {
                    iterator.remove()
                    break
                }
            }
        }
    }

    private fun sort(listInfo: TopicListInfo) {
        val list = listInfo.threadPageList
        if (PhoneConfiguration.getInstance().needSortByPostOrder()) {
            list.sortWith { o1: ThreadPageInfo, o2: ThreadPageInfo -> if (o1.postDate < o2.postDate) 1 else -1 }
        }
        val subBoards: MutableList<SubBoard> = listInfo.subBoardList
        if (subBoards.isNotEmpty()) {
            subBoards.sortWith { o1: SubBoard, o2: SubBoard -> if (o1.fid < o2.fid) 1 else -1 }
        }
    }

    private fun convertSubBoard(listInfo: TopicListInfo, topicListBean: TopicListBean) {
        try {
            val subForumsStr = topicListBean.data.__F.sub_forums.toString()
            if (TextUtils.isEmpty(subForumsStr)) {
                return
            }
            val subBoardMap: Map<String, Map<String, String>> =
                JSON.parseObject<Map<String, Map<String, String>>>(subForumsStr, MutableMap::class.java)
            for (key in subBoardMap.keys) {
                val boardMap = subBoardMap[key]!!
                val board = SubBoard()
                var obj: Any? = boardMap["0"]
                if (key.startsWith("t")) {
                    board.stid = obj.toString().toInt()
                } else {
                    board.fid = obj.toString().toInt()
                }

                // 有些子版块的fid的key是3，大部分都是1
                if (boardMap.containsKey("3")) {
                    obj = boardMap["3"]
                    board.tidStr = obj.toString()
                    board.type = 1
                } else {
                    board.type = 0
                }
                board.parentFidStr = topicListBean.data.__F.fid.toString()
                board.name = boardMap["1"]
                board.description = boardMap["2"]
                if (boardMap.containsKey("4")) {
                    obj = boardMap["4"]
                    board.isChecked = ForumUtils.isBoardSubscribed(obj.toString().toInt())
                } else {
                    board.type = -1
                    board.isChecked = true
                }
                listInfo.addSubBoard(board)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun convertTopic(listInfo: TopicListInfo, topicListBean: TopicListBean, page: Int) {
        val map = topicListBean.data.__T
        var count = 0
        while (count < map.size) {
            val key = count.toString()
            val tBean = map[key]
            if (tBean == null || filterTopic(listInfo, topicListBean, tBean)) {
                count++
                continue
            }
            val pageInfo = ThreadPageInfo()
            val author = tBean.author
            if (author.startsWith("#anony_")) {
                pageInfo.setAnonymity(true)
                pageInfo.setAuthor(getAnonymityName(tBean.author))
            } else {
                pageInfo.setAuthorId(tBean.authorid.toInt())
                pageInfo.setAuthor(tBean.author)
            }
            pageInfo.setLastPoster(tBean.lastposter)
            pageInfo.setSubject(tBean.subject)
            pageInfo.setReplies(tBean.replies)
            pageInfo.setType(tBean.type)
            pageInfo.setTopicMisc(tBean.topic_misc)
            pageInfo.setTitleFont(tBean.titlefont)
            var tid = tBean.tid
            val tpcUrl = tBean.tpcurl
            if (tpcUrl != null && tpcUrl.contains("tid")) {
                tid = StringUtils.getUrlParameter(tpcUrl, "tid")
            }
            pageInfo.setTid(tid)
            pageInfo.setPage(page)
            val pBean = tBean.__P
            if (pBean != null) {
                pageInfo.setPid(pBean.pid)
                val replyInfo = ReplyInfo()
                replyInfo.authorId = pBean.authorid
                replyInfo.content = pBean.content
                replyInfo.postDate = pBean.postdate.toString()
                replyInfo.pidStr = pBean.pid.toString()
                replyInfo.tidStr = pageInfo.tid.toString()
                replyInfo.subject = pageInfo.subject
                pageInfo.setReplyInfo(replyInfo)
            }
            val parent = tBean.parent
            if (parent != null) {
                pageInfo.setBoard(parent["2"]!!)
            }
            pageInfo.setPostDate(tBean.postdate)
            val topicMiscVar = tBean.topic_misc_var
            if (topicMiscVar != null && pageInfo.isMirrorBoard) {
                val obj: Any? = topicMiscVar["3"]
                if (obj != null) {
                    pageInfo.setFid(obj.toString().toInt())
                }
            }
            listInfo.addThreadPage(pageInfo)
            count++
        }
    }

    private fun filterTopic(
        listInfo: TopicListInfo,
        topicListBean: TopicListBean,
        tBean: TBean
    ): Boolean {
        return if (topicListBean.data.__F != null && PhoneConfiguration.getInstance()
                .needFilterSubBoard()
            && topicListBean.data.__F.fid == -7 && tBean.recommend > 9
        ) {
            NLog.d("屏蔽固定的渣帖子 $tBean")
            true
        } else {
            false
        }
    }

    @JvmStatic
    fun getAnonymityName(author: String): String {
        val prefix = "甲乙丙丁戊己庚辛壬癸子丑寅卯辰巳午未申酉戌亥"
        val suffix = "王李张刘陈杨黄吴赵周徐孙马朱胡林郭何高罗郑梁谢宋唐许邓冯韩曹曾彭萧蔡潘田董袁于余叶蒋杜苏魏程吕丁沈任姚卢傅钟姜崔谭廖范汪陆金石戴贾韦夏邱方侯邹熊孟秦白江阎薛尹段雷黎史龙陶贺顾毛郝龚邵万钱严赖覃洪武莫孔汤向常温康施文牛樊葛邢安齐易乔伍庞颜倪庄聂章鲁岳翟殷詹申欧耿关兰焦俞左柳甘祝包宁尚符舒阮柯纪梅童凌毕单季裴霍涂成苗谷盛曲翁冉骆蓝路游辛靳管柴蒙鲍华喻祁蒲房滕屈饶解牟艾尤阳时穆农司卓古吉缪简车项连芦麦褚娄窦戚岑景党宫费卜冷晏席卫米柏宗瞿桂全佟应臧闵苟邬边卞姬师和仇栾隋商刁沙荣巫寇桑郎甄丛仲虞敖巩明佘池查麻苑迟邝"
        val sb = StringBuilder()
        var i = 6
        for (j in 0..5) {
            var pos: Int
            if (j == 0 || j == 3) {
                pos = Integer.valueOf(author.substring(i + 1, i + 2), 16)
                if (pos >= prefix.length) {
                    pos = prefix.length - 1
                } else if (pos < 0) {
                    pos = 0
                }
                sb.append(prefix[pos])
            } else {
                pos = Integer.valueOf(author.substring(i, i + 2), 16)
                if (pos >= suffix.length) {
                    pos = suffix.length - 1
                } else if (pos < 0) {
                    pos = 0
                }
                sb.append(suffix[pos])
            }
            i += 2
        }
        return sb.toString()
    }
}