package nosc.ui.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import gov.anzong.androidnga.R
import nosc.api.model.BoardModel
import nosc.utils.iconUrl
import nosc.utils.uxUtils.showConfirmDialog
import sp.phone.mvp.model.entity.Board
import sp.phone.mvp.model.entity.BoardCategory

/**
 * @author Yricky
 * @date 2022/8/7
 */
@OptIn(ExperimentalFoundationApi::class)
fun LazyGridScope.boardCategoryList(
    cat:BoardCategory,
    boardClickable:(Board)->Unit = {},
    onListChanged:()->Unit = {},
    onScrollToThis:(String)->Unit = {}
){
    item {
        LaunchedEffect(cat){
            onScrollToThis(cat.name)
        }
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
            val context = LocalContext.current
            BoardItemContent(
                Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { boardClickable(b) },
                        onLongClick = {
                            if (cat.isBookmarkCategory) {
                                context.showConfirmDialog("确定要删除吗？") {
                                    BoardModel.removeBookmark(b.fid, b.stid)
                                    onListChanged()
                                }
                            }
                        }
                    ), board = b)
        }
    }
}

@Composable
fun BoardItemContent(modifier: Modifier = Modifier,board: Board){
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