package nosc.activity.gallery

import android.os.Environment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import nosc.api.retrofit.RetrofitHelper
import nosc.utils.uxUtils.ToastUtils
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class GalleryViewModel:ViewModel(){
    var images: List<GalleryImageState> by mutableStateOf(emptyList())
    private set

    var currPageIndex = MutableStateFlow(0)

    fun setImgList(galleryUrls: List<String>){
        images = galleryUrls.map {
            GalleryImageState(it)
        }
    }

    fun saveImage(index:Int,then:suspend (GalleryImageState)->Unit = {}){
        val imgInfo = images.getOrNull(index) ?: return
        if(imgInfo.isDownloaded){
            viewModelScope.launch { then(imgInfo) }
            return
        }
        val client = RetrofitHelper.getInstance().createOkHttpClientBuilder().followRedirects(true).build()
        val url = imgInfo.url
        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                ToastUtils.error("下载失败")
            }
            override fun onResponse(call: Call, response: Response) {
                val suffix = url.substring(url.lastIndexOf('.'))
                viewModelScope.launch(Dispatchers.IO) {
                    response.body?.byteStream()?.use {
                        val target = File(PATH_IMAGES,"${System.currentTimeMillis()}$suffix")
                        IOUtils.copy(
                            it,
                            FileOutputStream(target)
                        )
                        imgInfo.file = target
                        imgInfo.isDownloaded = true
                    }
                    then(imgInfo)
                }
            }
        })
    }

    class GalleryImageState(
        val url:String
    ){
        var file:File? = null
        var isDownloaded by mutableStateOf(false)
    }

    companion object {
        private val PATH_IMAGES by lazy {
            File(Environment.getExternalStorageDirectory().absolutePath + "/Pictures/nga_open_source/").also {
                if(!it.isDirectory){
                    it.deleteRecursively()
                    it.mkdirs()
                }
            }
        }

    }
}