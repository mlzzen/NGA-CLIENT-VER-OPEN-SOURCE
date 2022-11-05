package nosc.activity.gallery

import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.viewpager.widget.PagerAdapter
import coil.load
import com.github.chrisbanes.photoview.PhotoView

/**
 * 浏览
 * Created by elrond on 2017/10/13.
 */
class GalleryAdapter(private val images: List<GalleryViewModel.GalleryImageState>) : PagerAdapter() {

    override fun getCount(): Int {
        return images.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): View {
        val view = ComposeView(container.context)
        view.setContent {
            val imgInfo  = remember {
                images[position]
            }
            Box {
                AndroidView(factory = {
                    val photoView = PhotoView(it)
                    photoView.maximumScale = 10.0f
                    val url = imgInfo.url
                    photoView.load(url)
                    photoView
                }, modifier = Modifier.fillMaxSize())
                if(imgInfo.isDownloaded){
//                    Text(
//                        text = stringResource(id = R.string.file_saved)+imgInfo.file?.path,
//                        Modifier.align(Alignment.BottomCenter)
//                    )
                }
            }
        }
        container.addView(
            view,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }
}