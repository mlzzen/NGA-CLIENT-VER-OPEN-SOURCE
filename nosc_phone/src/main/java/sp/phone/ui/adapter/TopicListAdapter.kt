package sp.phone.ui.adapter

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.toSpannable
import sp.phone.ui.adapter.BaseAppendableAdapter
import sp.phone.mvp.model.entity.ThreadPageInfo
import sp.phone.ui.adapter.TopicListAdapter.TopicViewHolder
import sp.phone.common.PhoneConfiguration
import sp.phone.rxjava.RxUtils
import sp.phone.theme.ThemeManager
import sp.phone.param.TopicTitleHelper
import androidx.recyclerview.widget.RecyclerView
import gov.anzong.androidnga.R
import nosc.ui.NOSCTheme

class TopicListAdapter(context: Context) :
    BaseAppendableAdapter<ThreadPageInfo, TopicViewHolder>(context) {
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
        private var title by mutableStateOf(AnnotatedString(""))

        private var num by mutableStateOf(0)
        private var author by mutableStateOf("")
        private var lastReply by mutableStateOf("")
//        private var isBoardMirror by mutableStateOf(false)
        init {
            view.setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            view.setContent {
                TopicContent()
            }
        }
        fun handleJsonList(entry: ThreadPageInfo?) {
            if (entry == null) { return }
            itemView.tag = entry
            author = entry.author
            lastReply = entry.lastPoster
            num = entry.replies
            title = TopicTitleHelper.handleTitleFormat(entry)
        }

        @Composable
        private fun TopicContent(){
            val context = LocalContext.current
            NOSCTheme {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp)) {
                    Text(text = title, fontSize = PhoneConfiguration.getInstance().topicTitleSize.sp, color = MaterialTheme.colors.onBackground)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(0.dp, 12.dp, 0.dp, 0.dp)) {
                        Row(Modifier.wrapContentWidth()) {
                            Icon(
                                Icons.Rounded.Person,
                                contentDescription = "",
                                modifier = Modifier.size(15.dp),
                                tint = MaterialTheme.colors.onBackground
                            )
                            Text(text = author, fontSize = 12.sp, color = Color(ContextCompat.getColor(context,R.color.night_link_color)))
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Row(Modifier.wrapContentWidth()) {
                            Text(text = lastReply, fontSize = 12.sp, color = Color(ContextCompat.getColor(context,R.color.night_link_color)))
                            Image(
                                painter = painterResource(id = R.drawable.replies_icon),
                                contentDescription = "",
                            )
                            Text(
                                text = "$num", fontSize = 12.sp,
                                color = Color(ContextCompat.getColor(context,R.color.night_link_color)),
                                fontWeight = if (num > 99) FontWeight.Bold else null
                            )
                        }

                    }
                }
            }

        }

    }
}