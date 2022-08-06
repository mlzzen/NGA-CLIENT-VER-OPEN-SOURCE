package sp.phone.ui.adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import org.apache.commons.io.FilenameUtils
import sp.phone.rxjava.RxBus
import sp.phone.rxjava.RxEvent
import sp.phone.theme.ThemeManager
import sp.phone.ui.adapter.EmoticonChildAdapter.EmoticonViewHolder
import sp.phone.util.ImageUtils
import java.io.IOException

/**
 * Created by Justwen on 2018/6/8.
 */
class EmoticonChildAdapter(private val mContext: Context, private val mHeight: Int) :
    RecyclerView.Adapter<EmoticonViewHolder>() {
    private var mImageUrls: Array<String>? = null
    private var mCategoryName: String? = null
    private val mEmoticonClickListener = View.OnClickListener { v ->
        RxBus.getInstance().post(RxEvent(RxEvent.EVENT_INSERT_EMOTICON, v.tag))
    }

    fun setData(categoryName: String?, urls: Array<String>?) {
        mImageUrls = urls
        mCategoryName = categoryName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmoticonViewHolder {
        val emoticonView = ImageView(mContext)
        val padding = 32
        emoticonView.setPadding(padding, padding, padding, padding)
        emoticonView.layoutParams =
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mHeight / 3)
//        emoticonView.background = mContext.getDrawable(R.drawable.list_selector_background)
        emoticonView.setOnClickListener(mEmoticonClickListener)
        return EmoticonViewHolder(emoticonView)
    }

    override fun onBindViewHolder(holder: EmoticonViewHolder, position: Int) {
        ImageUtils.recycleImageView(holder.mEmoticonItem)
        try {
            mContext.assets.open(getFileName(position)).use { `is` ->
                val bm = BitmapFactory.decodeStream(`is`)
                val bitmap = ImageUtils.zoomImageByHeight(bm, 130)
                val drawable = BitmapDrawable(mContext.resources, bitmap)
                if (mCategoryName!!.contains("ac") && ThemeManager.getInstance().isNightMode) {
                    drawable.setTint(Color.GRAY)
                }
                holder.mEmoticonItem.setImageDrawable(drawable)
                holder.mEmoticonItem.tag = "[img]" + mImageUrls!![position] + "[/img]"
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getFileName(position: Int): String {
        return mCategoryName + "/" + FilenameUtils.getName(mImageUrls!![position])
    }

    override fun getItemCount(): Int {
        return if (mImageUrls == null) 0 else mImageUrls!!.size
    }

    class EmoticonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mEmoticonItem: ImageView

        init {
            mEmoticonItem = itemView as ImageView
        }
    }
}