package sp.phone.ui.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import sp.phone.view.RecyclerViewEx.IPageAdapter

/**
 * Created by Justwen on 2018/3/23.
 */
abstract class BasePageAppendableAdapter<E, T : RecyclerView.ViewHolder>(context: Context) :
    BaseAdapter<E, T>(context), IPageAdapter {
    private var mHaveNextPage = true
    private var mTotalPage = 0
    override fun nextPageIndex(): Int {
        return mTotalPage + 1
    }

    override fun hasNextPage(): Boolean {
        return mHaveNextPage
    }

    override fun setData(dataList: List<E>) {
        mTotalPage = 0
        mHaveNextPage = true
        super.setData(dataList)
    }

    fun appendData(dataList: List<E>) {
        val preAppendCount = mDataList.size
        mDataList.addAll(dataList)
        mTotalPage++
        notifyItemRangeInserted(preAppendCount,dataList.size)
    }

    fun setNextPageEnabled(enabled: Boolean) {
        mHaveNextPage = enabled
    }
}