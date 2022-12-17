package sp.phone.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import gov.anzong.androidnga.R
import nosc.utils.forumDateStringOf
import sp.phone.mvp.model.entity.ThreadPageInfo

/**
 * Created by Justwen on 2018/3/23.
 */
class ReplyListAdapter(context: Context) :
    BasePageAppendableAdapter<ThreadPageInfo, ReplyListAdapter.ViewHolder>(context) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(mLayoutInflater.inflate(R.layout.list_reply_ltem, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pageInfo = getItem(position)
        val replyInfo = pageInfo.replyInfo
        holder.mContentTv.text = replyInfo.content
        holder.mSubjectTv.text = replyInfo.subject
        holder.mPostDateTv.text = forumDateStringOf(replyInfo.postDate.toLong())
        holder.itemView.setOnClickListener(mOnClickListener)
        holder.itemView.tag = pageInfo
    }

    override fun setData(dataList: List<ThreadPageInfo>) {

        super.setData(dataList.filter {
            it.replyInfo != null
        })
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mContentTv: TextView = itemView.findViewById(R.id.tv_content)
        val mPostDateTv: TextView = itemView.findViewById(R.id.tv_time)
        val mSubjectTv: TextView = itemView.findViewById(R.id.tv_topic)
    }
}