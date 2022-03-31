package sp.phone.ui.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import sp.phone.view.RecyclerViewEx.IAppendableAdapter
import java.util.ArrayList

/**
 * Created by Justwen on 2018/3/23.
 */
abstract class BaseAppendableAdapter<E, T : RecyclerView.ViewHolder>(context: Context) :
    BaseAdapter<E, T>(context), IAppendableAdapter {
    private var mHaveNextPage = true
    private var mTotalPage = 0
    override fun getNextPage(): Int {
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
        val mutableDataList = mDataList.toMutableList()
        for (e in dataList) {
            if (!mutableDataList.contains(e)) {
                mutableDataList.add(0,e)
            }
        }
        mDataList = mutableDataList
        mTotalPage++
        notifyDataSetChanged()
    }

    fun setNextPageEnabled(enabled: Boolean) {
        mHaveNextPage = enabled
    }
}