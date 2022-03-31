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
        holder.handleJsonList(info)
        if (!PhoneConfiguration.getInstance().useSolidColorBackground()) {
            holder.itemView.setBackgroundResource(
                ThemeManager.getInstance().getBackgroundColor(position)
            )
        }
    }

    class TopicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val num: TextView = itemView.findViewById(R.id.num)
        private val title: TextView = itemView.findViewById(R.id.title)
        private val author: TextView = itemView.findViewById(R.id.author)
        private val lastReply: TextView = itemView.findViewById(R.id.last_reply)
        init {
            title.setTextSize(
                TypedValue.COMPLEX_UNIT_DIP,
                PhoneConfiguration.getInstance().topicTitleSize
            )
        }
        fun handleJsonList(entry: ThreadPageInfo?) {
            itemView.tag = entry
            if (entry == null) {
                return
            }
            author.text = entry.author
            lastReply.text = entry.lastPoster
            num.text = entry.replies.toString()
            num.setTextAppearance(if (entry.replies > 99) R.style.text_style_bold else R.style.text_style_normal)
            title.text = TopicTitleHelper.handleTitleFormat(entry)
        }
    }
}