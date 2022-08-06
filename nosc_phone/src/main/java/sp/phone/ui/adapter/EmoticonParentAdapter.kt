package sp.phone.ui.adapter

import android.content.Context
import android.view.View
import androidx.viewpager.widget.PagerAdapter
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.GridLayoutManager
import nosc.utils.EmoticonUtils

/**
 * Created by Justwen on 2018/6/8.
 */
class EmoticonParentAdapter(private val mContext: Context, private val mHeight: Int) :
    PagerAdapter() {
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val recyclerView = RecyclerView(mContext)
        recyclerView.layoutManager =
            GridLayoutManager(mContext, COLUMN_COUNT)
        recyclerView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        val adapter = EmoticonChildAdapter(mContext, mHeight)
        adapter.setData(
            EmoticonUtils.EMOTICON_LABEL[position][0],
            EmoticonUtils.EMOTICON_URL[position]
        )
        recyclerView.adapter = adapter
        container.addView(recyclerView)
        return recyclerView
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return EmoticonUtils.EMOTICON_LABEL[position][1]
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getCount(): Int {
        return EmoticonUtils.EMOTICON_LABEL.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    companion object {
        private const val COLUMN_COUNT = 4
    }
}