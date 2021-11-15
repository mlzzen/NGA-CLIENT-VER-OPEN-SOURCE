package sp.phone.mvp.model

import android.text.TextUtils
import sp.phone.mvp.model.entity.BoardCategory
import sp.phone.mvp.model.entity.Board
import sp.phone.mvp.model.entity.Board.BoardKey
import nosc.api.bean.CategoryBean
import com.alibaba.fastjson.JSON
import gov.anzong.androidnga.base.util.ContextUtils
import gov.anzong.androidnga.base.util.PreferenceUtils
import gov.anzong.androidnga.base.util.ToastUtils
import gov.anzong.androidnga.common.PreferenceKey
import okhttp3.*
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.util.*

/**
 * Created by Justwen on 2019/6/23.
 */
object BoardModel : BaseModel() {
    private val mBoardCategoryList: MutableList<BoardCategory> = ArrayList()
    private val mBookmarkCategory: BoardCategory by lazy{
        BoardCategory("我的收藏").apply {
            val bookmarkBoards =
                PreferenceUtils.getData(PreferenceKey.BOOKMARK_BOARD, Board::class.java)
            addBoards(bookmarkBoards)
            isBookmarkCategory = true
        }
    }

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

    fun queryBoard(callback:(List<BoardCategory>)->Unit) {
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
                    }catch (e:Throwable){}
                    callback.invoke(mBoardCategoryList.toList())
                }
        })
    }

    fun addBookmark(board: Board) {
        if (!mBookmarkCategory.contains(board)) {
            mBookmarkCategory.addBoard(board)
            saveBookmark()
        }
    }

    fun addBookmark(fid: Int, stid: Int, boardName: String):Boolean {
        return if (isBookmark(fid, stid)) {
            ToastUtils.info("该版面已存在")
            false
        } else {
            addBookmark(BoardKey(fid, stid), boardName)
            ToastUtils.success("添加成功")
            true
        }

    }

    fun removeBookmark(fid: Int, stid: Int) {
        if (mBookmarkCategory.removeBoard(BoardKey(fid, stid))) {
            saveBookmark()
        }
    }

    fun removeAllBookmarks() {
        mBookmarkCategory.removeAllBoards()
        saveBookmark()
    }

    fun isBookmark(fid: Int, stid: Int): Boolean {
        return mBookmarkCategory.contains(BoardKey(fid, stid))
    }

    fun swapBookmark(from: Int, to: Int) {
        val boards = mBookmarkCategory.boardList
        if (from < to) {
            for (i in from until to) {
                Collections.swap(boards, i, i + 1)
            }
        } else {
            for (i in from downTo to + 1) {
                Collections.swap(boards, i, i - 1)
            }
        }
        saveBookmark()
    }

    fun getBoardName(fid: Int, stid: Int): String {
        return getBoardName(BoardKey(fid, stid))
    }






    private fun buildCategory(json:String){
        mBoardCategoryList.clear()
        mBoardCategoryList.add(mBookmarkCategory)
        upgradeBookmarkBoard(mBoardCategoryList)

        val beans = try{
            JSON.parseArray(
                JSON.parseObject(
                    json
                ).getJSONArray("result").toJSONString(), CategoryBean::class.java
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

    private fun addBookmark(boardKey: BoardKey, boardName: String) {
        if (!mBookmarkCategory.contains(boardKey)) {
            mBookmarkCategory.addBoard(Board(boardKey, boardName))
            saveBookmark()
        }
    }

    private fun saveBookmark() {
        PreferenceUtils.putData(
            PreferenceKey.BOOKMARK_BOARD,
            JSON.toJSONString(mBookmarkCategory.boardList)
        )
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
            val bookmarkBoards = mBookmarkCategory.boardList
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

}