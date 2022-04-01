package sp.phone.mvp.model

import com.alibaba.fastjson.JSON
import nosc.utils.ContextUtils
import nosc.utils.ThreadUtils
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import nosc.api.callbacks.OnHttpCallBack
import nosc.api.retrofit.RetrofitHelper
import nosc.api.retrofit.RetrofitService
import org.apache.commons.io.FileUtils
import sp.phone.mvp.model.convert.ErrorConvertFactory
import sp.phone.mvp.model.convert.TopicConvertFactory
import sp.phone.mvp.model.entity.ThreadPageInfo
import sp.phone.mvp.model.entity.TopicListInfo
import sp.phone.param.TopicListParam
import sp.phone.rxjava.BaseSubscriber
import sp.phone.util.ForumUtils
import sp.phone.util.NLog
import sp.phone.util.StringUtils
import java.io.File
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*

class TopicListModel : BaseModel() {
    private val mService: RetrofitService = RetrofitHelper.getInstance().getService(RetrofitService::class.java) as RetrofitService
    private var mFieldMap: MutableMap<String, String>? = null
    private fun initFieldMap() {
        if (mFieldMap == null) {
            mFieldMap = HashMap<String,String>().also{
                it["__lib"] = "topic_favor"
                it["__act"] = "topic_favor"
                it["__output"] = "8"
                it["action"] = "del"
            }
        }
    }

    fun loadCache(callBack: OnHttpCallBack<TopicListInfo>) {
        Observable.create { emitter: ObservableEmitter<TopicListInfo?> ->
            val path = ContextUtils.getExternalDir("articleCache")
            val cacheDirs = File(path).listFiles()
            if (cacheDirs == null) {
                emitter.onError(Exception())
            } else {
                val listInfo = TopicListInfo()
                for (dir in cacheDirs) {
                    val infoFile = File(dir, dir.name + ".json")
                    if (!infoFile.exists()) {
                        continue
                    }
                    val rawData = FileUtils.readFileToString(infoFile)
                    val pageInfo = JSON.parseObject(rawData, ThreadPageInfo::class.java)
                    if (pageInfo == null) {
                        //CloudServerManager.putCrashData(ContextUtils.getContext(),"rawData", rawData);
                    } else {
                        listInfo.addThreadPage(
                            JSON.parseObject(
                                rawData,
                                ThreadPageInfo::class.java
                            )
                        )
                    }
                }
                emitter.onNext(listInfo)
            }
            emitter.onComplete()
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : BaseSubscriber<TopicListInfo?>() {
                override fun onNext(topicListInfo: TopicListInfo) {
                    callBack.onSuccess(topicListInfo)
                }

                override fun onError(throwable: Throwable) {
                    callBack.onError("读取缓存失败！")
                }
            })
    }

    fun removeTopic(info: ThreadPageInfo, callBack: OnHttpCallBack<String>) {
        initFieldMap()
        mFieldMap!!["page"] = info.page.toString()
        var tidArray = info.tid.toString()
        if (info.pid != 0) {
            tidArray = tidArray + "_" + info.pid
        }
        mFieldMap!!["tidarray"] = tidArray
        mService.post(mFieldMap)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : BaseSubscriber<String?>() {
                override fun onNext(s: String) {
                    if (s.contains("操作成功")) {
                        callBack.onSuccess("操作成功！")
                    } else {
                        callBack.onError("操作失败!")
                    }
                }
            })
    }

    fun loadTopicList(
        page: Int,
        param: TopicListParam,
        callBack: OnHttpCallBack<TopicListInfo>
    ) {
        val url = getUrl(page, param)
        mService[url]
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .map { js -> //NLog.d(js);
                val result = TopicConvertFactory.getTopicListInfo(js, page)
                result ?: throw Exception(ErrorConvertFactory.getErrorMessage(js))
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : BaseSubscriber<TopicListInfo?>() {
                override fun onNext(topicListInfo: TopicListInfo) {
                    callBack.onSuccess(topicListInfo)
                }

                override fun onError(throwable: Throwable) {
                    callBack.onError(ErrorConvertFactory.getErrorMessage(throwable))
                }
            })
    }

    fun loadTwentyFourList(
        param: TopicListParam,
        callBack: OnHttpCallBack<TopicListInfo>,
        totalPage: Int
    ) {
        val obsList: MutableList<Observable<String>> = ArrayList()
        for (i in 1..totalPage) {
            obsList.add(mService[getUrl(i, param)])
        }
        Observable.concat(obsList).subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .map { js ->
                NLog.d(js)
                val result = TopicConvertFactory.getTopicListInfo(js, 0)
                result ?: throw Exception(ErrorConvertFactory.getErrorMessage(js))
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : BaseSubscriber<TopicListInfo?>() {
                override fun onNext(topicListInfo: TopicListInfo) {
                    callBack.onSuccess(topicListInfo)
                }

                override fun onError(throwable: Throwable) {
                    callBack.onError(ErrorConvertFactory.getErrorMessage(throwable))
                }
            })
    }

    fun removeCacheTopic(info: ThreadPageInfo, callBack: OnHttpCallBack<String>) {
        ThreadUtils.postOnSubThread {
            val path = ContextUtils.getExternalDir("articleCache")
            val cacheDirs = File(path).listFiles()
            if (cacheDirs == null) {
                callBack.onError(null)
                return@postOnSubThread
            }
            try {
                for (dir in cacheDirs) {
                    if (dir.name == info.tid.toString()) {
                        FileUtils.deleteDirectory(dir)
                        callBack.onSuccess(null)
                        return@postOnSubThread
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            callBack.onError(null)
        }
    }

    private fun getUrl(page: Int, requestInfo: TopicListParam): String {
        val urlStr = StringBuilder(ForumUtils.getAvailableDomain() + "/thread.php?")
        if (0 != requestInfo.authorId) {
            urlStr.append("authorid=").append(requestInfo.authorId).append("&")
        }
        if (requestInfo.searchPost != 0) {
            urlStr.append("searchpost=").append(requestInfo.searchPost).append("&")
        }
        if (requestInfo.favor != 0) {
            urlStr.append("favor=").append(requestInfo.favor).append("&")
        }
        if (requestInfo.content != 0) {
            urlStr.append("content=").append(requestInfo.content).append("&")
        }
        if (!StringUtils.isEmpty(requestInfo.author)) {
            try {
                if (requestInfo.author.endsWith("&searchpost=1")) {
                    urlStr.append("author=").append(
                        URLEncoder.encode(
                            requestInfo.author.substring(0, requestInfo.author.length - 13),
                            "GBK"
                        )
                    ).append("&searchpost=1&")
                } else {
                    urlStr.append("author=").append(URLEncoder.encode(requestInfo.author, "GBK"))
                        .append("&")
                }
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
        } else {
            if (requestInfo.stid != 0) {
                urlStr.append("stid=").append(requestInfo.stid).append("&")
            } else if (0 != requestInfo.fid) {
                urlStr.append("fid=").append(requestInfo.fid).append("&")
            }
            if (!StringUtils.isEmpty(requestInfo.key)) {
                urlStr.append("key=").append(StringUtils.encodeUrl(requestInfo.key, "UTF-8"))
                    .append("&")
            }
            if (!StringUtils.isEmpty(requestInfo.fidGroup)) {
                urlStr.append("fidgroup=").append(requestInfo.fidGroup).append("&")
            }
        }
        urlStr.append("page=").append(page).append("&lite=js&noprefix")
        if (requestInfo.recommend == 1) {
            urlStr.append("&recommend=1&order_by=postdatedesc&user=1")
        }
        return urlStr.toString()
    }

}