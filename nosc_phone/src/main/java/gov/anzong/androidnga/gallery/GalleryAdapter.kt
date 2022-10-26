package gov.anzong.androidnga.gallery

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import coil.load
import com.github.chrisbanes.photoview.PhotoView

/**
 * 浏览
 * Created by elrond on 2017/10/13.
 */
class GalleryAdapter(private val mContext: Context, private val mGalleryUrls: List<String>) :
    PagerAdapter() {
    override fun getCount(): Int {
        return mGalleryUrls.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): View {
        val photoView = PhotoView(container.context)
        photoView.maximumScale = 10.0f
        val url = mGalleryUrls[position]
        photoView.load(url){
            listener(
                onSuccess = { _,_ ->
                    callbackActivity()
                },
                onError = { _,_ ->
                    callbackActivity()
                },
                onCancel = {
                    callbackActivity()
                }
            )
        }
        container.addView(
            photoView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        return photoView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    private fun callbackActivity() {
        (mContext as ImageZoomActivity).hideLoading()
    }
}