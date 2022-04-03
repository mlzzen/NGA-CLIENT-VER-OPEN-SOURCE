package sp.phone.ui.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.compose.foundation.Image
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import sp.phone.ui.adapter.TopicListAdapter.TopicViewHolder
import sp.phone.common.PhoneConfiguration
import sp.phone.rxjava.RxUtils
import sp.phone.theme.ThemeManager
import sp.phone.param.TopicTitleHelper
import androidx.recyclerview.widget.RecyclerView
import gov.anzong.androidnga.R
import nosc.ui.NOSCTheme
import nosc.utils.dateStringOf
import sp.phone.mvp.model.entity.ThreadPageInfo
import java.text.SimpleDateFormat
import java.util.*

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
        if (!PhoneConfiguration.getInstance().useSolidColorBackground()) {
            holder.itemView.setBackgroundResource(
                ThemeManager.getInstance().getBackgroundColor(position)
            )
        }
    }

    override fun onViewRecycled(holder: TopicViewHolder) {
        // Dispose the underlying Composition of the ComposeView
        // when RecyclerView has recycled this ViewHolder
        holder.view.disposeComposition()
    }


    class TopicViewHolder(context: Context) : RecyclerView.ViewHolder(ComposeView(context)) {
        val view get() = itemView as ComposeView
        private var threadPageInfo:ThreadPageInfo by mutableStateOf(ThreadPageInfo())
//        private var isBoardMirror by mutableStateOf(false)
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
                    val context = LocalContext.current
                    val author = entry.author
                    val lastReply = entry.lastPoster
                    val num = entry.replies
                    val title = TopicTitleHelper.handleTitleFormat(entry)
                    val textColor = Color(ContextCompat.getColor(context,R.color.night_link_color))
                    Column(
                        Modifier
                            .clickable { onClick() }
                            .fillMaxWidth()
                            .padding(12.dp)) {
                        Text(text = title, fontSize = PhoneConfiguration.getInstance().topicTitleSize.sp, color = MaterialTheme.colors.onBackground)
                        Row(
                            Modifier
                                .padding(0.dp, 12.dp, 0.dp, 0.dp)
                                .fillMaxWidth()) {
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
                        Row(
                            Modifier
                                .fillMaxWidth()) {
                            Row(Modifier.wrapContentWidth()) {
                                Text(text = dateStringOf(entry.postDate.toLong()), fontSize = 12.sp, color = textColor)
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Row(Modifier.wrapContentWidth()) {
                                Text(text = dateStringOf(entry.lastPost.toLong()), fontSize = 12.sp, color = textColor)
                            }
                        }
                    }
                }

            }
        }



    }
}