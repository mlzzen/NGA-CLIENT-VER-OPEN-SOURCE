package gov.anzong.androidnga.gallery

import android.content.Context
import android.os.Environment
import gov.anzong.androidnga.R
import nosc.api.callbacks.OnSimpleHttpCallBack
import nosc.api.retrofit.RetrofitHelper
import nosc.utils.ContextUtils
import nosc.utils.ThreadUtils
import nosc.utils.uxUtils.ToastUtils
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.apache.commons.io.IOUtils
import org.reactivestreams.Subscription
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class SaveImageTask {
    private val mContext: Context = ContextUtils.getContext()
    private var mDownloadCount = 0
    private var mSubscription: Subscription? = null

    class DownloadResult(var file: File, var url: String)

    fun execute(callBack: OnSimpleHttpCallBack<DownloadResult>, urls: List<String>) {
        if (isRunning) {
            ToastUtils.info("图片正在下载，防止风怒！！")
            return
        }
        mDownloadCount = 0
        val client = RetrofitHelper.getInstance().createOkHttpClientBuilder().followRedirects(true).build()
        urls.forEach { url ->
            val request = Request.Builder().url(url).build()
            client.newCall(request).enqueue(object :Callback{
                override fun onFailure(call: Call, e: IOException) {
                    ToastUtils.error("下载失败")
                }

                override fun onResponse(call: Call, response: Response) {
                    val suffix = url.substring(url.lastIndexOf('.'))
                    response.body?.byteStream()?.use {

                        val path = PATH_IMAGES + System.currentTimeMillis() + suffix
                        val target = File(path)
                        IOUtils.copy(
                            it,
                            FileOutputStream(target)
                        )
                        mDownloadCount++
                        if (mDownloadCount == urls.size) {
                            if (urls.size > 1) {
                                ThreadUtils.postOnMainThread {
                                    ToastUtils.info("所有图片已保存")
                                }
                            } else {
                                ThreadUtils.postOnMainThread {
                                    ToastUtils.info(mContext.getString(R.string.file_saved) + target.absolutePath)
                                }
                            }
                        }
                        callBack.onResult(DownloadResult(target,url))
                    }
                }
            })
        }
    }

    private val isRunning: Boolean get() = mSubscription != null

    companion object {
        private val PATH_IMAGES =
            Environment.getExternalStorageDirectory().absolutePath + "/Pictures/nga_open_source/"
    }
}