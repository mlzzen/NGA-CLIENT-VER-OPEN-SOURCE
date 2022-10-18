package gov.anzong.androidnga.gallery

import android.content.Context
import android.view.View
import androidx.viewpager.widget.PagerAdapter
import android.view.ViewGroup
import coil.load
import com.github.chrisbanes.photoview.PhotoView
import gov.anzong.androidnga.gallery.ImageZoomActivity

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
        //        Glide.with(mContext)
//                .load(url).listener(mRequestListener)
//                .apply(RequestOptions.fitCenterTransform())
//                .into(new CustomTarget<Drawable>() {
//            @Override
//            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
//                if (resource instanceof GifDrawable) {
//                    if (!((GifDrawable) resource).isRunning()) {
//                        try {
//                            ((GifDrawable) resource).startFromFirstFrame();
//                            ((GifDrawable) resource).setLoopCount(GifDrawable.LOOP_FOREVER);
//                        } catch (IllegalArgumentException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//                photoView.setImageDrawable(resource);
//            }
//
//            @Override
//            public void onLoadCleared(@Nullable Drawable placeholder) {
//
//            }
//        });
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