package sp.phone.mvp.model

import android.text.TextUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import nosc.api.ApiResult
import nosc.api.ERR
import nosc.api.OK
import nosc.api.bean.ThreadData
import nosc.api.retrofit.Api
import nosc.api.retrofit.RetrofitHelper
import nosc.utils.ContextUtils
import nosc.utils.ThreadUtils
import nosc.utils.uxUtils.ToastUtils
import org.apache.commons.io.FileUtils
import sp.phone.mvp.contract.ArticleListContract
import sp.phone.mvp.model.convert.ArticleConvertFactory
import sp.phone.mvp.model.convert.ErrorConvertFactory
import sp.phone.param.ArticleListParam
import sp.phone.util.ForumUtils
import java.io.File
import java.io.IOException

/**
 * 加载帖子内容
 * Created by Justwen on 2017/7/10.
 */
class ArticleListModel: ArticleListContract.Model {
    private val api: Api = RetrofitHelper.getInstance().api
    private fun getUrl(param: ArticleListParam): String {
        val page = param.page
        val tid = param.tid
        val pid = param.pid
        val authorId = param.authorId
        var url =
            ForumUtils.getApiDomain() + "/read.php?" + "&page=" + page + "&lite=js&noprefix&v2"
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

    override fun loadPage(param: ArticleListParam):Flow<ApiResult<ThreadData>> {
        return flow {
            emit(api.request(getUrl(param)))
        }.map { s ->
            val data = ArticleConvertFactory.getArticleInfo(s)
            if(data == null){
                ERR(ErrorConvertFactory.getErrorMessage(s))
            }else{
                OK(data)
            }
        }.catch { e ->
            emit(ERR(e.localizedMessage ?: ""))
        }.flowOn(Dispatchers.IO)

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

    fun loadCachePage(param: ArticleListParam):Flow<ApiResult<ThreadData>> {
        return flow {
            val cachePath =
                ContextUtils.getExternalDir("articleCache") + param.tid + "/" + param.page + ".json"
            val cacheFile = File(cachePath)
            val rawData = FileUtils.readFileToString(cacheFile)
            val threadData = ArticleConvertFactory.getArticleInfo(rawData)
            if (threadData != null) {
                emit(OK(threadData))
            } else {
                emit(ERR("读取缓存失败！"))
            }
        }.catch { e ->
            emit(ERR(e.localizedMessage ?: ""))
        }.flowOn(Dispatchers.IO)
    }
}