package sp.phone.mvp.presenter

import android.Manifest
import sp.phone.mvp.model.BoardModel.isBookmark
import sp.phone.mvp.model.BoardModel.addBookmark
import sp.phone.mvp.model.BoardModel.removeBookmark
import sp.phone.mvp.model.entity.TopicListInfo
import sp.phone.param.TopicListParam
import sp.phone.mvp.model.entity.ThreadPageInfo
import sp.phone.mvp.model.TopicListModel
import nosc.api.callbacks.OnHttpCallBack
import gov.anzong.androidnga.base.util.ToastUtils
import gov.anzong.androidnga.base.util.DeviceUtils
import sp.phone.mvp.model.entity.Board
import sp.phone.util.ARouterUtils
import gov.anzong.androidnga.arouter.ARouterConstants
import sp.phone.param.ParamKey
import gov.anzong.androidnga.base.util.PermissionUtils
import android.os.Environment
import android.content.Intent
import sp.phone.ui.fragment.TopicCacheFragment
import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import gov.anzong.androidnga.BuildConfig
import gov.anzong.androidnga.base.util.ContextUtils
import gov.anzong.androidnga.common.util.FileUtils
import gov.anzong.androidnga.common.util.LogUtils
import sp.phone.rxjava.BaseSubscriber
import java.io.File
import java.lang.Exception
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author Justwen
 * @date 2017/6/3
 */
@Deprecated("Use mvvm")
class TopicListPresenter : ViewModel(), LifecycleObserver {
    // Following variables are for the 24 hour hot topic feature
    // How many pages we query for twenty four hour hot topic
    protected val twentyFourPageCount = 5

    // How many total topics we want to show
    protected val twentyFourTopicCount = 50
    protected var pageQueriedCounter = 0
    protected var twentyFourCurPos = 0
    protected var twentyFourList = TopicListInfo()
    protected var twentyFourCurList = TopicListInfo()
    private var mRequestParam: TopicListParam? = null
    val firstTopicList = MutableLiveData<TopicListInfo>()
    val nextTopicList = MutableLiveData<TopicListInfo>()
    val errorMsg = MutableLiveData<String>()
    val isRefreshing = MutableLiveData<Boolean>()
    val removedTopic = MutableLiveData<ThreadPageInfo>()
    private var mBaseModel: TopicListModel
    private val mCallBack: OnHttpCallBack<TopicListInfo> = object :
        OnHttpCallBack<TopicListInfo> {
        override fun onError(text: String) {
            errorMsg.value = text
            isRefreshing.value = false
        }

        override fun onSuccess(data: TopicListInfo) {
            isRefreshing.value = false
            firstTopicList.value = data
        }
    }
    private val mNextPageCallBack: OnHttpCallBack<TopicListInfo> =
        object : OnHttpCallBack<TopicListInfo> {
            override fun onError(text: String) {
                if ("HTTP 404 Not Found" == text) ToastUtils.warn("已无更多") else errorMsg.setValue(
                    text
                )
                isRefreshing.value = false
            }

            override fun onSuccess(data: TopicListInfo) {
                isRefreshing.value = false
                nextTopicList.value = data
            }
        }

    /* callback for the twenty four hour hot topic list */
    private val mTwentyFourCallBack: OnHttpCallBack<TopicListInfo> =
        object : OnHttpCallBack<TopicListInfo> {
            override fun onError(text: String) {
                errorMsg.value = text
                isRefreshing.value = false
            }

            override fun onSuccess(data: TopicListInfo) {
                /* Concatenate the pages */
                twentyFourList.threadPageList.addAll(data.threadPageList)
                pageQueriedCounter++
                if (pageQueriedCounter == twentyFourPageCount) {
                    twentyFourCurPos = 0
                    val threadPageList = twentyFourList.threadPageList
                    if (DeviceUtils.isGreaterEqual_7_0()) {
                        threadPageList.removeIf { item: ThreadPageInfo -> data.curTime - item.postDate > 24 * 60 * 60 }
                    } else {
                        val each = threadPageList.iterator()
                        while (each.hasNext()) {
                            val item = each.next()
                            if (data.curTime - item.postDate > 24 * 60 * 60) {
                                each.remove()
                            }
                        }
                    }
                    if (threadPageList.size > twentyFourTopicCount) {
                        threadPageList.subList(twentyFourTopicCount, threadPageList.size)
                    }
                    twentyFourList.threadPageList.sortWith(Comparator { o1: ThreadPageInfo, o2: ThreadPageInfo ->
                        o2.replies.compareTo(o1.replies)
                    })
                    // We list 20 topics each time
                    val endPos = Math.min(twentyFourCurPos + 20, twentyFourList.threadPageList.size)
                    twentyFourCurList.threadPageList =
                        twentyFourList.threadPageList.subList(0, endPos)
                    twentyFourCurPos = endPos
                    isRefreshing.setValue(false)
                    nextTopicList.setValue(twentyFourCurList)
                }
            }
        }

    fun setRequestParam(requestParam: TopicListParam?) {
        mRequestParam = requestParam
    }

    protected fun onCreateModel(): TopicListModel {
        return TopicListModel()
    }

    fun removeTopic(info: ThreadPageInfo, position: Int) {
        mBaseModel.removeTopic(info, object :
            OnHttpCallBack<String> {
            override fun onError(text: String) {
                errorMsg.value = text
            }

            override fun onSuccess(data: String) {
                ToastUtils.flat(data)
                removedTopic.value = info
            }
        })
    }

    fun removeCacheTopic(info: ThreadPageInfo) {
        mBaseModel.removeCacheTopic(info, object :
            OnHttpCallBack<String> {
            override fun onError(text: String) {
                errorMsg.value = "删除失败！"
            }

            override fun onSuccess(data: String) {
                ToastUtils.info("删除成功！")
                removedTopic.postValue(info)
            }
        })
    }

    fun loadPage(page: Int, requestInfo: TopicListParam?) {
        isRefreshing.value = true
        if (requestInfo!!.twentyfour == 1) {
            // preload pages
            twentyFourList.threadPageList.clear()
            pageQueriedCounter = 0
            mBaseModel.loadTwentyFourList(requestInfo, mTwentyFourCallBack, twentyFourPageCount)
        } else {
            mBaseModel.loadTopicList(page, requestInfo, mCallBack)
        }
    }

    fun loadCachePage() {
        mBaseModel.loadCache(mCallBack)
    }

    fun loadNextPage(page: Int, requestInfo: TopicListParam) {
        isRefreshing.value = true
        if (requestInfo.twentyfour == 1) {
            val endPos = Math.min(twentyFourCurPos + 20, twentyFourList.threadPageList.size)
            twentyFourCurList.threadPageList = twentyFourList.threadPageList.subList(0, endPos)
            twentyFourCurPos = endPos
            isRefreshing.value = false
            nextTopicList.setValue(twentyFourCurList)
        } else {
            mBaseModel.loadTopicList(page, requestInfo, mNextPageCallBack)
        }
    }

    fun isBookmarkBoard(fid: Int, stid: Int): Boolean {
        return isBookmark(fid, stid)
    }

    fun addBookmarkBoard(fid: Int, stid: Int, boardName: String?) {
        addBookmark(fid, stid, boardName!!)
    }

    fun addBookmarkBoard(board: Board?) {
        addBookmark(board!!)
    }

    fun removeBookmarkBoard(fid: Int, stid: Int) {
        removeBookmark(fid, stid)
    }

    fun startArticleActivity(tid: String, title: String?) {
        ARouterUtils.build(ARouterConstants.ACTIVITY_TOPIC_CONTENT)
            .withInt(ParamKey.KEY_TID, tid.toInt())
            .withString(ParamKey.KEY_TITLE, title)
            .navigation(ContextUtils.getContext())
    }

    @OnLifecycleEvent(value = Lifecycle.Event.ON_CREATE)
    fun onViewCreated() {
        if (mRequestParam != null && mRequestParam!!.loadCache) {
            loadCachePage()
        } else {
            loadPage(1, mRequestParam)
        }
    }

    fun exportCacheTopic(fragment: Fragment?) {
        PermissionUtils.requestAsync(fragment, object : BaseSubscriber<Boolean?>() {
            override fun onNext(aBoolean: Boolean) {
                if (aBoolean) {
                    val srcDir = ContextUtils.getExternalDir("articleCache")
                    val dateFormat: DateFormat =
                        SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
                    val dateStr = dateFormat.format(Date(System.currentTimeMillis()))
                    val destDir =
                        (Environment.getExternalStorageDirectory().toString() + File.separator
                                + BuildConfig.APPLICATION_ID + File.separator + "cache/cache_" + dateStr + ".zip")
                    if (FileUtils.zipFiles(srcDir, destDir)) {
                        ToastUtils.success("导出成功至$destDir")
                    } else {
                        ToastUtils.error("导出失败")
                    }
                } else {
                    ToastUtils.warn("无存储权限，无法导出！")
                }
            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    fun showFileChooser(fragment: Fragment) {
        PermissionUtils.request(fragment, object : BaseSubscriber<Boolean?>() {
            override fun onNext(aBoolean: Boolean) {
                if (aBoolean) {
                    try {
                        val intent = Intent(Intent.ACTION_GET_CONTENT)
                        intent.addCategory(Intent.CATEGORY_OPENABLE)
                        intent.type = "*/*"
                        fragment.startActivityForResult(
                            intent,
                            TopicCacheFragment.REQUEST_IMPORT_CACHE
                        )
                    } catch (e: ActivityNotFoundException) {
                        ToastUtils.warn("系统不支持导入")
                    }
                } else {
                    ToastUtils.warn("无存储权限，无法导入！")
                }
            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    fun importCacheTopic(uri: Uri) {
        val context = ContextUtils.getContext()
        if (!checkCacheZipFile(context, uri)) {
            ToastUtils.error("选择非法文件")
            return
        }
        val cr = context.contentResolver
        val destDir = context.filesDir.absolutePath
        val tempZipFile = File(destDir, "temp.zip")
        try {
            cr.openInputStream(uri).use { `is` ->
                if (`is` == null) {
                    return
                }
                org.apache.commons.io.FileUtils.copyInputStreamToFile(`is`, tempZipFile)
                FileUtils.unzip(tempZipFile.absolutePath, destDir)
                loadCachePage()
                ToastUtils.success("导入成功！！")
            }
        } catch (e: Exception) {
            LogUtils.print(e)
        }
        tempZipFile.delete()
    }

    private fun checkCacheZipFile(context: Context, uri: Uri): Boolean {
        val cr = context.contentResolver
        val contentType = cr.getType(uri)
        return contentType != null && contentType.contains("zip")
    }

    init {
        mBaseModel = TopicListModel()
        mBaseModel = onCreateModel()
    }
}