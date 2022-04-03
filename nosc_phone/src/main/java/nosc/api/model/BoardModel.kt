package nosc.api.model

import android.text.TextUtils
import sp.phone.mvp.model.entity.BoardCategory
import sp.phone.mvp.model.entity.Board
import sp.phone.mvp.model.entity.Board.BoardKey
import nosc.api.bean.CategoryBean
import com.alibaba.fastjson.JSON
import nosc.utils.ContextUtils
import nosc.utils.PreferenceUtils
import nosc.utils.PreferenceKey
import nosc.config.CurrentUserData
import nosc.utils.JsonUtils

import okhttp3.*
import sp.phone.mvp.model.BaseModel
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.util.*

/**
 * Created by Justwen on 2019/6/23.
 */
object BoardModel : BaseModel() {
    private val mBoardCategoryList: MutableList<BoardCategory> = ArrayList()
    private val suggestCategory: BoardCategory = BoardCategory("推荐内容").apply {
        isBookmarkCategory = true
    }

    private val bookMarkList:BoardCategory get() = suggestCategory.getSubCategory(0)
    private val recentList:BoardCategory get() = suggestCategory.getSubCategory(1)

    fun findBoard(fid: String): Board? {
        val boardKey = BoardKey(fid.toInt(), 0)
        for (boardCategory in mBoardCategoryList) {
            val board = boardCategory.getBoard(boardKey)
            if (board != null) {
                return board
            }
        }
        return null
    }

    fun requestBoard(callback:(List<BoardCategory>)->Unit) {
        updateSuggestBoardCategory()
        OkHttpClient.Builder().build()
            .newCall(Request.Builder().url("https://bbs.nga.cn/app_api.php?&__lib=home&__act=category").build())
            .enqueue(object :Callback{
                override fun onFailure(call: Call, e: IOException) {
                    buildCategory(
                        try {
                            File(ContextUtils.getApplication().getExternalFilesDir("categoryCache"),"category.json").readText(
                                Charset.defaultCharset())
                        }catch (e:Throwable){ "" }
                    )
                    callback.invoke(mBoardCategoryList.toList())
                }

                override fun onResponse(call: Call, response: Response) {
                    try{
                        buildCategory(response.body()?.string()?.also {
                            File(ContextUtils.getApplication().getExternalFilesDir("categoryCache"),"category.json").writeText(it,
                                Charset.defaultCharset())
                        } ?: "")
                    }catch (e:Throwable){
                        e.printStackTrace()
                    }
                    callback.invoke(mBoardCategoryList.toList())
                }
        })
    }

    fun addBookmark(board: Board):Boolean {
        if (!bookMarkList.contains(board)) {
            if(recentList.contains(board)){
                recentList.removeBoard(board.boardKey)
            }
            bookMarkList.addBoard(board)
            saveBookmark()
            return true
        }
        return false
    }

    fun removeBookmark(fid: Int, stid: Int) {
        if (bookMarkList.removeBoard(BoardKey(fid, stid)) || recentList.removeBoard(BoardKey(fid, stid))) {
            saveBookmark()
        }
    }

    fun removeAllBookmarks() {
        bookMarkList.removeAllBoards()
        saveBookmark()
    }

    fun isBookmark(fid: Int, stid: Int): Boolean {
        return bookMarkList.contains(BoardKey(fid, stid))
    }

    fun getBoardName(fid: Int, stid: Int): String {
        return getBoardName(BoardKey(fid, stid))
    }

    fun addRecentBoard(board:Board){
        if(isBookmark(board.fid,board.stid))
            return
        if(recentList.contains(board)){
            recentList.removeBoard(board.boardKey)
            recentList.addBoard(0,board)
        }else{
            recentList.addBoard(0,board)
        }
        saveBookmark()
    }

    fun isBookMarkEmpty():Boolean = bookMarkList.size() == 0 && recentList.size() == 0

    private fun buildCategory(json:String){
        mBoardCategoryList.clear()
        mBoardCategoryList.add(suggestCategory)
        upgradeBookmarkBoard(mBoardCategoryList)

        val beans = try{
            JsonUtils.parseArray(
                JSON.parseObject(json)
                    .getJSONArray("result")
                    .toJSONString()
            )
        } catch (e:Throwable){ listOf<CategoryBean>() }

        for (categoryBean in beans) {
            val category = BoardCategory(categoryBean.name)
            categoryBean.groups?.forEach { group->
                val subCategory = BoardCategory(group.name)
                group.forums?.forEach {forum->
                    val boardName: String? =
                        if (TextUtils.isEmpty(forum.nameS)) { forum.name } else { forum.nameS }
                    val board = Board(forum.fid, forum.stid, boardName)
                    board.boardHead = forum.head
                    subCategory.addBoard(board)
                }
                category.addSubCategory(subCategory)
            }
            mBoardCategoryList.add(category)
        }
    }

    private fun saveBookmark() {
        CurrentUserData.favouriteBoard = bookMarkList.boardList
        CurrentUserData.recentBoard = recentList.boardList
        updateSuggestBoardCategory()
    }

    private fun getBoardName(boardKey: BoardKey): String {
        for (boardCategory in mBoardCategoryList) {
            val board = boardCategory.getBoard(boardKey)
            if (board != null) {
                return board.name
            }
        }
        return ""
    }

    private fun upgradeBookmarkBoard(preloadCategory: List<BoardCategory>) {
        val boardVersion = PreferenceUtils.getData(PreferenceKey.KEY_PRELOAD_BOARD_VERSION, 0)
        if (boardVersion < PRELOAD_BOARD_VERSION) {
            val bookmarkBoards = suggestCategory.boardList
            for (i in bookmarkBoards.indices) {
                val board = bookmarkBoards[i]
                for (category in preloadCategory) {
                    val fixBoard = category.getBoard(board.boardKey)
                    if (fixBoard != null) {
                        bookmarkBoards[i] = fixBoard
                        break
                    }
                }
            }
            saveBookmark()
            PreferenceUtils.putData(PreferenceKey.KEY_PRELOAD_BOARD_VERSION, PRELOAD_BOARD_VERSION)
        }
    }

    private const val PRELOAD_BOARD_VERSION = 1

    private fun updateSuggestBoardCategory(){
        suggestCategory.apply {
            subCategoryList?.clear()
            removeAllBoards()
            addSubCategory(BoardCategory("我的收藏").apply {
                addBoards(CurrentUserData.favouriteBoard)
            })
            addSubCategory(BoardCategory("最近浏览").apply {
                addBoards(CurrentUserData.recentBoard)
            })
        }
    }


}