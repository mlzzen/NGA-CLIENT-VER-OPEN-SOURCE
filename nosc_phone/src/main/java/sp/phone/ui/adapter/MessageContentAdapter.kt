package sp.phone.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import gov.anzong.androidnga.R
import gov.anzong.androidnga.databinding.ListMessageContentBinding
import nosc.api.bean.MessageArticlePageInfo
import nosc.api.bean.MessageDetailInfo
import nosc.utils.uxUtils.ToastUtils
import sp.phone.theme.ThemeManager
import sp.phone.util.FunctionUtils
import sp.phone.view.RecyclerViewEx.IPageAdapter

/**
 * Created by Justwen on 2017/10/15.
 */
class MessageContentAdapter :RecyclerView.Adapter<MessageContentAdapter.MessageViewHolder>(),
    IPageAdapter {
    private val mInfoList: MutableList<MessageDetailInfo> = ArrayList()
    private var mPrompted = false
    private var mTotalCount = 0
    private var hasNextPage:Boolean = false
    override fun getItemCount(): Int {
        return mTotalCount
    }

    class MessageViewHolder(binding: ListMessageContentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var nickName: TextView = binding.nickName
        var floor: TextView = binding.floor
        var postTime: TextView = binding.postTime
        var content: WebView = binding.content
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return MessageViewHolder(
            ListMessageContentBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        handleJsonList(holder, position)
        if (position + 1 == itemCount && !hasNextPage()
            && !mPrompted
        ) {
            ToastUtils.info(R.string.last_page_prompt_message_detail)
            mPrompted = true
        }
    }

    private fun getEntry(position: Int): MessageArticlePageInfo {
        return mInfoList[position / 20].messageEntryList[position % 20]
    }

    fun addDetailInfo(data: MessageDetailInfo?) {
        if (data != null) {
            if (data.__currentPage == 1) {
                reset()
            }
            mInfoList.add(data)
            mTotalCount += data.messageEntryList.size
            hasNextPage = data.__nextPage > 0
            notifyDataSetChanged()
        }
    }

    override fun nextPageIndex(): Int {
        return mInfoList.size + 1
    }

    override fun hasNextPage(): Boolean =hasNextPage

    private fun reset() {
        mTotalCount = 0
        mInfoList.clear()
        mPrompted = false
        hasNextPage = true
    }

    private fun handleJsonList(holder: MessageViewHolder, position: Int) {
        val context = holder.itemView.context
        val entry = getEntry(position)
        val res = holder.itemView.context.resources
        val theme = ThemeManager.getInstance()
        holder.postTime.text = entry.time
        val floor = entry.lou.toString()
        holder.floor.text = "#$floor"
        holder.nickName.setTextColor(res.getColor(theme.foregroundColor))
        holder.postTime.setTextColor(res.getColor(theme.foregroundColor))
        holder.floor.setTextColor(res.getColor(theme.foregroundColor))
        FunctionUtils.handleNickName(
            entry,
            res.getColor(theme.foregroundColor),
            holder.nickName,
            context
        )
        val colorId = theme.getBackgroundColor(position + 1)
        val bgColor = res.getColor(colorId)
        val fgColorId = theme.foregroundColor
        val fgColor = res.getColor(fgColorId)
        (holder.itemView as CardView).setCardBackgroundColor(bgColor)
        FunctionUtils.handleContentTV(holder.content, entry, bgColor, fgColor, context)
    }
}