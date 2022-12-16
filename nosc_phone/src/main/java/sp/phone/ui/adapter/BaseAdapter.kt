package sp.phone.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.compose.runtime.mutableStateListOf
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Justwen on 2018/3/23.
 */
abstract class BaseAdapter<E, T : RecyclerView.ViewHolder>(protected val mContext: Context) :
    RecyclerView.Adapter<T>() {
    @JvmField
    protected val mDataList: MutableList<E> = mutableStateListOf()
    @JvmField
    protected var mOnClickListener: View.OnClickListener? = null
    protected var mOnLongClickListener: View.OnLongClickListener? = null
    @JvmField
    protected var mLayoutInflater: LayoutInflater = LayoutInflater.from(mContext)

    fun getItem(position: Int): E {
        return mDataList[position]
    }

    open fun setData(dataList: List<E>) {
        val preCount = mDataList.size
        mDataList.clear()
        mDataList.addAll(dataList)
        val count = dataList.size
        if(preCount>count){
            notifyItemRangeRemoved(count,preCount-count)
        }else if(preCount < count){
            notifyItemRangeInserted(preCount,count-preCount)
        }
        notifyItemRangeChanged(0,count)
    }

    fun removeItemAt(position: Int) {
        mDataList.removeAt(position)
        notifyItemRemoved(position)
    }

    fun removeItem(data: E) {
        removeItemAt(mDataList.indexOf(data))
    }

    fun clear() {
        val preCount = mDataList.size
        mDataList.clear()
        notifyItemRangeRemoved(0,preCount)
    }

    override fun getItemCount(): Int = mDataList.size

    fun setOnClickListener(onClickListener: View.OnClickListener?) {
        mOnClickListener = onClickListener
    }

    fun setOnLongClickListener(onLongClickListener: View.OnLongClickListener?) {
        mOnLongClickListener = onLongClickListener
    }

}