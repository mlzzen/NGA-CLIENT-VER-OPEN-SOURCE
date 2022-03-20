package sp.phone.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import sp.phone.mvp.model.entity.ThreadPageInfo
import androidx.recyclerview.widget.RecyclerView
import gov.anzong.androidnga.R
import sp.phone.util.StringUtils

/**
 * Created by Justwen on 2018/3/23.
 */
class ReplyListAdapter(context: Context) :
    BaseAppendableAdapter<ThreadPageInfo, ReplyListAdapter.ViewHolder>(context) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(mLayoutInflater.inflate(R.layout.list_reply_ltem, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pageInfo = getItem(position)
        val replyInfo = pageInfo.replyInfo
        holder.mContentTv.text = replyInfo.content
        holder.mSubjectTv.text = replyInfo.subject
        holder.mPostDateTv.text = StringUtils.timeStamp2Date2(replyInfo.postDate)
        holder.itemView.setOnClickListener(mOnClickListener)
        holder.itemView.tag = pageInfo
    }

    override fun setData(dataList: List<ThreadPageInfo>) {

        super.appendData(checkData(dataList))
    }

    private fun checkData(dataList: List<ThreadPageInfo>):List<ThreadPageInfo> {
        return dataList.filter {
            it.replyInfo == null
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mContentTv: TextView
        var mPostDateTv: TextView
        var mSubjectTv: TextView

        init {
            mContentTv = itemView.findViewById(R.id.tv_content)
            mPostDateTv = itemView.findViewById(R.id.tv_time)
            mSubjectTv = itemView.findViewById(R.id.tv_topic)
        }
    }
}