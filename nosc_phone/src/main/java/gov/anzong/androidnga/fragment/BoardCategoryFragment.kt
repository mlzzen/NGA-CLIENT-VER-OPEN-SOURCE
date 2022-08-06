package gov.anzong.androidnga.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import coil.compose.AsyncImage
import gov.anzong.androidnga.R
import nosc.api.model.BoardModel
import nosc.ui.NOSCTheme
import nosc.utils.iconUrl
import nosc.utils.uxUtils.showConfirmDialog
import sp.phone.mvp.model.entity.Board
import sp.phone.mvp.model.entity.BoardCategory
import sp.phone.rxjava.RxBus
import sp.phone.rxjava.RxEvent

/**
 * 版块分页
 */
class BoardCategoryFragment : Fragment() {
    private var composeView: ComposeView? = null
    private var mBoardCategory: BoardCategory? = null
    var refresh by mutableStateOf(false)
    private val boardClickable = { b:Board ->
        BoardModel.addRecentBoard(b)
        RxBus.getInstance().post(RxEvent(RxEvent.EVENT_SHOW_TOPIC_LIST, b))
        if(mBoardCategory?.isBookmarkCategory == true){
            refresh = !refresh
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mBoardCategory = requireArguments().getParcelable("category")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(inflater.context).also { cv ->
            cv.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            composeView = cv
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        composeView?.setContent {
            NOSCTheme {
                key(refresh) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(110.dp),
                        modifier = Modifier.fillMaxSize()
                    ){
                        mBoardCategory?.let { cat ->
                            items(
                                cat.boardList,
                                key = { it.name },
                                span = {GridItemSpan(1)},
                                contentType = { 0 }
                            ){ b->
                                BoardItemContent(
                                    Modifier
                                        .padding(8.dp)
                                        .fillMaxWidth()
                                        .combinedClickable(
                                            onClick = {boardClickable(b)},
                                            onLongClick = {
                                                if(mBoardCategory?.isBookmarkCategory == true){
                                                    requireActivity().showConfirmDialog("确定要删除吗？"){
                                                        BoardModel.removeBookmark(b.fid,b.stid)
                                                        refresh = !refresh
                                                    }
                                                }
                                            }
                                        ), board = b)
                            }
                            cat.subCategoryList.forEach { sCat ->
                                item(
                                    key = "c/${sCat.name}",
                                    span = { GridItemSpan(maxLineSpan) },
                                    contentType = 1
                                ){
                                    Row(
                                        Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ){
                                        Image(
                                            painter = painterResource(id = R.drawable.default_board_icon),
                                            contentDescription = "",
                                            Modifier.size(24.dp)
                                        )
                                        Text(text = sCat.name)

                                    }
                                }
                                items(
                                    sCat.boardList,
                                    key = { "c/${sCat.name}/${it.name}" },
                                    span = { GridItemSpan(1) },
                                    contentType = { 0 }
                                ){ b->
                                    BoardItemContent(
                                        Modifier
                                            .padding(8.dp)
                                            .fillMaxWidth()
                                            .combinedClickable(
                                                onClick = {boardClickable(b)},
                                                onLongClick = {
                                                    if(mBoardCategory?.isBookmarkCategory == true){
                                                        requireActivity().showConfirmDialog("确定要删除吗？"){
                                                            BoardModel.removeBookmark(b.fid,b.stid)
                                                            refresh = !refresh
                                                        }
                                                    }
                                                }
                                            ), board = b)
                                }
                            }
                        }
                    }
                }

            }

        }
    }

    @Composable
    fun BoardItemContent(modifier: Modifier = Modifier,board:Board){
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val placeholder = painterResource(id = R.drawable.default_board_icon)
            AsyncImage(
                modifier = Modifier.size(48.dp),
                model = board.iconUrl(),
                placeholder =placeholder ,
                error = placeholder,
                contentDescription = board.name
            )
            Text(text = board.name)
        }
    }
}