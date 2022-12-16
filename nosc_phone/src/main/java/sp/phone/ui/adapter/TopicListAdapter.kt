package sp.phone.ui.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.recyclerview.widget.RecyclerView
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshState
import gov.anzong.androidnga.R
import nosc.ui.NOSCTheme
import nosc.utils.forumDateStringOf
import sp.phone.common.appConfig
import sp.phone.mvp.model.entity.ThreadPageInfo
import sp.phone.param.TopicTitleHelper
import sp.phone.theme.ThemeManager

class TopicListAdapter(context: Context) :
    BasePageAppendableAdapter<ThreadPageInfo, RecyclerView.ViewHolder>(context) {

    @Deprecated("")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return Unit as RecyclerView.ViewHolder
    }

    @Deprecated("")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    }

    var onItemClick:(ThreadPageInfo)->Unit = {}
    var onItemLongClick:(ThreadPageInfo)->Unit = {}

    var onNextPage:()->Unit = {}
    var onRefresh:(()->Unit)? = null

    val scrollState = LazyListState()

    private val swipeRefreshState = SwipeRefreshState(false)
    var isRefreshing get() =swipeRefreshState.isRefreshing
        set(value){
            swipeRefreshState.isRefreshing = value
        }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun Content(){
        SwipeRefresh(state = swipeRefreshState, onRefresh = {
            onRefresh?.invoke()
        }) {
            val context = LocalContext.current
            val color1 = remember {
                context.getColor(
                    ThemeManager.getInstance().getBackgroundColorRes(1)
                )
            }
            val color2 = remember {
                context.getColor(
                    ThemeManager.getInstance().getBackgroundColorRes(0)
                )
            }
            LazyColumn(Modifier.fillMaxSize(), state = scrollState){
                itemsIndexed(mDataList){ index,it ->
                    Box(modifier = Modifier.animateItemPlacement()){
                        TopicContent(it, backGroundColor = Color(if (appConfig.useSolidColorBackground() || (index%2 == 0)) color1 else color2), onLongClick = {
                            onItemLongClick(it)
                        }) {
                            onItemClick(it)
                        }
                        if(mDataList.last() == it){
                            onNextPage()
                        }
                    }
                }
            }
        }

    }

    companion object{
        @OptIn(ExperimentalFoundationApi::class)
        @Composable
        fun TopicContent(
            entry: ThreadPageInfo = ThreadPageInfo(),
            backGroundColor:Color = Color.Transparent,
            onLongClick:()->Unit = {},
            onClick:()->Unit ={}
        ){
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
                            color = backGroundColor
                        )
                        .combinedClickable(
                            onClick = onClick,
                            onLongClick = onLongClick
                        )
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