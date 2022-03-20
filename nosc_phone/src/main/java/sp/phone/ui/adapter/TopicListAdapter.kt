package sp.phone.ui.adapter

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import sp.phone.ui.adapter.BaseAppendableAdapter
import sp.phone.mvp.model.entity.ThreadPageInfo
import sp.phone.ui.adapter.TopicListAdapter.TopicViewHolder
import sp.phone.common.PhoneConfiguration
import sp.phone.rxjava.RxUtils
import sp.phone.theme.ThemeManager
import sp.phone.param.TopicTitleHelper
import androidx.recyclerview.widget.RecyclerView
import gov.anzong.androidnga.R

class TopicListAdapter(context: Context) :
    BaseAppendableAdapter<ThreadPageInfo, TopicViewHolder>(context) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
        val viewHolder = TopicViewHolder(
            LayoutInflater.from(mContext).inflate(R.layout.list_topic, parent, false)
        )
        viewHolder.title.setTextSize(
            TypedValue.COMPLEX_UNIT_DIP,
            PhoneConfiguration.getInstance().topicTitleSize
        )
        RxUtils.clicks(viewHolder.itemView, mOnClickListener)
        viewHolder.itemView.setOnLongClickListener(mOnLongClickListener)
        return viewHolder
    }

    override fun setData(dataList: List<ThreadPageInfo>) {
        super.appendData(dataList)
    }

    override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
        val info = getItem(position)
        info.position = position
        holder.itemView.tag = info
        handleJsonList(holder, info)
        if (!PhoneConfiguration.getInstance().useSolidColorBackground()) {
            holder.itemView.setBackgroundResource(
                ThemeManager.getInstance().getBackgroundColor(position)
            )
        }
    }

    private fun handleJsonList(holder: TopicViewHolder, entry: ThreadPageInfo?) {
        if (entry == null) {
            return
        }
        holder.author.text = entry.author
        holder.lastReply.text = entry.lastPoster
        holder.num.text = entry.replies.toString()
        holder.num.setTextAppearance(if (entry.replies > 99) R.style.text_style_bold else R.style.text_style_normal)
        holder.title.text = TopicTitleHelper.handleTitleFormat(entry)
    }

    class TopicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var num: TextView
        var title: TextView
        var author: TextView
        var lastReply: TextView

        init {
            num = itemView.findViewById(R.id.num)
            title = itemView.findViewById(R.id.title)
            author = itemView.findViewById(R.id.author)
            lastReply = itemView.findViewById(R.id.last_reply)
        }
    }
}