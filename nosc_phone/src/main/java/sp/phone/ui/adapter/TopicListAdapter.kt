package sp.phone.ui.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sp.phone.ui.adapter.TopicListAdapter.TopicViewHolder
import sp.phone.rxjava.RxUtils
import sp.phone.theme.ThemeManager
import sp.phone.param.TopicTitleHelper
import androidx.recyclerview.widget.RecyclerView
import gov.anzong.androidnga.R
import nosc.ui.NOSCTheme
import nosc.utils.forumDateStringOf
import sp.phone.common.appConfig
import sp.phone.mvp.model.entity.ThreadPageInfo

class TopicListAdapter(context: Context) :
    BasePageAppendableAdapter<ThreadPageInfo, TopicViewHolder>(context) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
        val viewHolder = TopicViewHolder(
            mContext
        )
        RxUtils.clicks(viewHolder.itemView, mOnClickListener)
        viewHolder.itemView.setOnLongClickListener(mOnLongClickListener)
        return viewHolder
    }

    override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
        val info = getItem(position)
        info.position = position
        holder.handleJsonList(info)
    }

    override fun onViewRecycled(holder: TopicViewHolder) {
        // Dispose the underlying Composition of the ComposeView
        // when RecyclerView has recycled this ViewHolder
        holder.view.disposeComposition()
    }


    class TopicViewHolder(context: Context) : RecyclerView.ViewHolder(ComposeView(context)) {
        val view get() = itemView as ComposeView
        private var threadPageInfo:ThreadPageInfo by mutableStateOf(ThreadPageInfo())
        init {
            view.setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            view.setContent {
                TopicContent(threadPageInfo){
                    itemView.callOnClick()
                }
            }
        }

        fun handleJsonList(entry: ThreadPageInfo?) {
            if (entry == null) { return }
            itemView.tag = entry
            threadPageInfo = entry
        }

        companion object{
            @Composable
            private fun TopicContent(entry: ThreadPageInfo = ThreadPageInfo(), onClick:()->Unit ={}){
                NOSCTheme {
                    val author = entry.author
                    val lastReply = entry.lastPoster
                    val num = entry.replies
                    val title = TopicTitleHelper.handleTitleFormat(entry)
                    val info = TopicTitleHelper.handleInfoFormat(entry)
                    val textColor = colorResource(id = R.color.night_link_color)
                    Column(
                        Modifier
                            .background(
                                color = if(!appConfig.useSolidColorBackground()){
                                    colorResource(
                                        id = ThemeManager
                                            .getInstance()
                                            .getBackgroundColorRes(entry.position)
                                    )
                                }else Color.Transparent
                            )
                            .clickable { onClick() }
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(12.dp)) {
                        Text(
                            text = title,
                            fontSize = appConfig.topicTitleSize.sp,
                            color = MaterialTheme.colors.onBackground,
                        )
                        Text(
                            text = info,
                            fontSize = 12.sp,
                            color = MaterialTheme.colors.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            Modifier.fillMaxWidth()) {
                            Row(Modifier.wrapContentWidth()) {
                                Icon(
                                    Icons.Rounded.Person,
                                    contentDescription = "",
                                    modifier = Modifier.size(15.dp),
                                    tint = textColor
                                )
                                Text(text = author, fontSize = 12.sp, color = textColor)
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            if(num > 0){
                                Row(Modifier.wrapContentWidth()) {
                                    Text(text = lastReply, fontSize = 12.sp, color = textColor)
                                    Image(
                                        painter = painterResource(id = R.drawable.replies_icon),
                                        contentDescription = "",
                                    )
                                    Text(
                                        text = "$num", fontSize = 12.sp,
                                        color = textColor,
                                        fontWeight = if (num > 99) FontWeight.Bold else null
                                    )
                                }
                            }
                        }
                        Row(
                            Modifier
                                .fillMaxWidth()) {
                            Row(Modifier.wrapContentWidth()) {
                                Text(text = forumDateStringOf(entry.postDate), fontSize = 12.sp, color = textColor)
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Row(Modifier.wrapContentWidth()) {
                                Text(text = forumDateStringOf(entry.lastPost), fontSize = 12.sp, color = textColor)
                            }
                        }
                    }
                }

            }
        }



    }
}