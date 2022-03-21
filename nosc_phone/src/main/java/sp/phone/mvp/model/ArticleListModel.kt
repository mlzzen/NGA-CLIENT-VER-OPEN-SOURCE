package sp.phone.mvp.model

import android.text.TextUtils
import sp.phone.mvp.model.BaseModel
import sp.phone.mvp.contract.ArticleListContract
import nosc.api.retrofit.RetrofitService
import sp.phone.param.ArticleListParam
import sp.phone.util.ForumUtils
import nosc.api.callbacks.OnHttpCallBack
import nosc.api.bean.ThreadData
import io.reactivex.schedulers.Schedulers
import com.trello.rxlifecycle2.android.FragmentEvent
import gov.anzong.androidnga.base.util.ContextUtils
import kotlin.Throws
import sp.phone.mvp.model.convert.ArticleConvertFactory
import sp.phone.util.NLog
import sp.phone.mvp.model.ArticleListModel
import sp.phone.mvp.model.convert.ErrorConvertFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import sp.phone.common.UserManagerImpl
import gov.anzong.androidnga.base.util.ToastUtils
import gov.anzong.androidnga.base.util.ThreadUtils
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.ObservableEmitter
import nosc.api.retrofit.RetrofitHelper
import org.apache.commons.io.FileUtils
import sp.phone.rxjava.BaseSubscriber
import java.io.File
import java.io.IOException
import java.lang.Exception

/**
 * 加载帖子内容
 * Created by Justwen on 2017/7/10.
 */
class ArticleListModel : BaseModel(), ArticleListContract.Model {
    private val mService: RetrofitService = RetrofitHelper.getInstance().getService(RetrofitService::class.java) as RetrofitService
    private fun getUrl(param: ArticleListParam): String {
        val page = param.page
        val tid = param.tid
        val pid = param.pid
        val authorId = param.authorId
        var url =
            ForumUtils.getAvailableDomain() + "/read.php?" + "&page=" + page + "&lite=js&noprefix&v2"
        if (tid != 0) {
            url = "$url&tid=$tid"
        }
        if (pid != 0) {
            url = "$url&pid=$pid"
        }
        if (authorId != 0) {
            url = "$url&authorid=$authorId"
        }
        return url
    }

    override fun loadPage(param: ArticleListParam, callBack: OnHttpCallBack<ThreadData>) {
        val url = getUrl(param)
        mService[url]
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.newThread())
            .compose(lifecycleProvider.bindUntilEvent(FragmentEvent.DETACH))
            .map { s ->
                val time = System.currentTimeMillis()
                val data = ArticleConvertFactory.getArticleInfo(s)
                NLog.e(TAG, "time = " + (System.currentTimeMillis() - time))
                data ?: throw Exception(ErrorConvertFactory.getErrorMessage(s))
            }
            .observeOn(AndroidSchedulers.mainThread())
            .compose(lifecycleProvider.bindUntilEvent(FragmentEvent.DETACH))
            .subscribe(object : BaseSubscriber<ThreadData?>() {
                override fun onNext(threadData: ThreadData) {
                    callBack.onSuccess(threadData)
                    UserManagerImpl.getInstance().putAvatarUrl(threadData)
                }

                override fun onError(throwable: Throwable) {
                    callBack.onError(ErrorConvertFactory.getErrorMessage(throwable))
                }
            })
    }

    override fun cachePage(param: ArticleListParam, rawData: String) {
        if (TextUtils.isEmpty(param.topicInfo)) {
            ToastUtils.error("缓存失败！")
            return
        }
        ThreadUtils.postOnSubThread {
            try {
                val path = ContextUtils.getExternalDir("articleCache") + param.tid
                val describeFile = File(path, param.tid.toString() + ".json")
                FileUtils.write(describeFile, param.topicInfo)
                val rawDataFile = File(path, param.page.toString() + ".json")
                FileUtils.write(rawDataFile, rawData)
                ToastUtils.success("缓存成功！")
            } catch (e: IOException) {
                ToastUtils.error("缓存失败！")
                e.printStackTrace()
            }
        }
    }

    override fun loadCachePage(param: ArticleListParam, callBack: OnHttpCallBack<ThreadData>) {
        Observable.create { emitter: ObservableEmitter<ThreadData?> ->
            val cachePath =
                ContextUtils.getExternalDir("articleCache") + param.tid + "/" + param.page + ".json"
            val cacheFile = File(cachePath)
            val rawData = FileUtils.readFileToString(cacheFile)
            val threadData = ArticleConvertFactory.getArticleInfo(rawData)
            if (threadData != null) {
                emitter.onNext(threadData)
            } else {
                emitter.onError(Exception())
            }
            emitter.onComplete()
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : BaseSubscriber<ThreadData?>() {
                override fun onNext(threadData: ThreadData) {
                    callBack.onSuccess(threadData)
                }

                override fun onError(throwable: Throwable) {
                    callBack.onError("读取缓存失败！")
                }
            })
    }

    companion object {
        private val TAG = ArticleListModel::class.java.simpleName
    }

}