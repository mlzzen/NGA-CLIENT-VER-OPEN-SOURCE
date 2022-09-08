package sp.phone.ui.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import sp.phone.view.RecyclerViewEx.IPageAdapter
import nosc.api.bean.MessageListInfo
import nosc.api.bean.MessageThreadPageInfo
import android.view.ViewGroup
import android.view.LayoutInflater
import gov.anzong.androidnga.R
import nosc.utils.uxUtils.ToastUtils
import sp.phone.theme.ThemeManager
import android.view.View
import android.widget.TextView
import nosc.utils.ContextUtils
import gov.anzong.androidnga.databinding.ListMessageBinding
import sp.phone.common.appConfig
import sp.phone.util.StringUtils
import java.util.ArrayList

/**
 * Created by Justwen on 2017/10/1.
 */
class MessageListAdapter(private val mContext: Context) :
    RecyclerView.Adapter<MessageListAdapter.MessageViewHolder>(),
    IPageAdapter {
    private val mInfoList: MutableList<MessageListInfo> = ArrayList()
    private var mPrompted = false
    private var mEndOfList = false
    private var mTotalCount = 0
    private var mClickListener: View.OnClickListener? = null
    private fun getEntry(position: Int): MessageThreadPageInfo? {
        var position = position
        for (i in mInfoList.indices) {
            if (position < mInfoList[i].__currentPage * mInfoList[i].__rowsPerPage) {
                return mInfoList[i].messageEntryList[position]
            }
            position -= mInfoList[i].__rowsPerPage
        }
        return null
    }

    override fun nextPageIndex(): Int {
        return mInfoList.size + 1
    }

    override fun hasNextPage(): Boolean {
        return !mEndOfList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return MessageViewHolder(
            ListMessageBinding.inflate(LayoutInflater.from(mContext), parent, false)
        )
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        handleJsonList(holder, position)
        if (mClickListener != null) {
            holder.itemView.setOnClickListener(mClickListener)
        }
        holder.itemView.tag = getMidString(position)
        if (position + 1 == itemCount && !hasNextPage()
            && !mPrompted
        ) {
            ToastUtils.info(R.string.last_page_prompt_message)
            mPrompted = true
        }
    }

    private fun getMidString(position: Int): String? {
        val entry = getEntry(position)
        return if (entry == null || entry.mid == 0) {
            null
        } else "mid=" + entry.mid
    }

    private fun handleJsonList(holder: MessageViewHolder, position: Int) {
        val entry = getEntry(position) ?: return
        val theme = ThemeManager.getInstance()
        var fromUser = entry.from_username
        if (StringUtils.isEmpty(fromUser)) {
            fromUser = "#SYSTEM#"
        }
        holder.author.text = fromUser
        holder.time.text = entry.time
        holder.lastTime.text = entry.lastTime
        var lastPoster = entry.last_from_username
        if (StringUtils.isEmpty(lastPoster)) {
            lastPoster = fromUser
        }
        holder.lastReply.text = lastPoster
        holder.num.text = entry.posts.toString()
        holder.title.setTextColor(ContextUtils.getColor(theme.foregroundColorRes))
        val size = appConfig.topicTitleSize
        var title = entry.subject
        if (StringUtils.isEmpty(title)) {
            title = entry.subject
            holder.title.text = StringUtils.unEscapeHtml(title)
        } else {
            holder.title.text = StringUtils.removeBrTag(
                StringUtils
                    .unEscapeHtml(title)
            )
        }
        holder.title.textSize = size.toFloat()
        val tp = holder.title.paint
        tp.isFakeBoldText = false
        val colorId = theme.getBackgroundColorRes(position)
        holder.itemView.setBackgroundResource(colorId)
    }

    fun setOnClickListener(listener: View.OnClickListener?) {
        mClickListener = listener
    }

    override fun getItemCount(): Int {
        return mTotalCount
    }

    private fun reset() {
        mTotalCount = 0
        mPrompted = false
        mInfoList.clear()
    }

    fun setData(result: MessageListInfo?) {
        if (result == null) {
            return
        } else if (result.__currentPage == 1) {
            reset()
        }
        mInfoList.add(result)
        mTotalCount += result.messageEntryList.size
        mEndOfList = result.__nextPage <= 0
        notifyDataSetChanged()
    }

    class MessageViewHolder(binding: ListMessageBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        var num: TextView = binding.num
        var title: TextView = binding.title
        var author: TextView = binding.author
        var lastReply: TextView = binding.lastReply
        var time: TextView = binding.time
        var lastTime: TextView = binding.lasttime
    }
}