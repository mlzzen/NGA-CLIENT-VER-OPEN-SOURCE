package sp.phone.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Justwen on 2018/3/23.
 */
abstract class BaseAdapter<E, T : RecyclerView.ViewHolder>(protected val mContext: Context) :
    RecyclerView.Adapter<T>() {
    @JvmField
    protected var mDataList: List<E> = emptyList()
    @JvmField
    protected var mOnClickListener: View.OnClickListener? = null
    protected var mOnLongClickListener: View.OnLongClickListener? = null
    @JvmField
    protected var mLayoutInflater: LayoutInflater = LayoutInflater.from(mContext)

    fun getItem(position: Int): E {
        return mDataList[position]
    }

    open fun setData(dataList: List<E>) {
        mDataList = dataList
        notifyDataSetChanged()
    }

    fun removeItemAt(position: Int) {
        mDataList.getOrNull(position)?.let{
            mDataList = mDataList.minus(it)
        }
        notifyDataSetChanged()
    }

    fun removeItem(data: E) {
        mDataList = mDataList.minus(data)
    }

    fun clear() {
        mDataList = emptyList()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = mDataList.size

    fun setOnClickListener(onClickListener: View.OnClickListener?) {
        mOnClickListener = onClickListener
    }

    fun setOnLongClickListener(onLongClickListener: View.OnLongClickListener?) {
        mOnLongClickListener = onLongClickListener
    }

}