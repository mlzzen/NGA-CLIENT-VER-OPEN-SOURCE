package sp.phone.mvp.model

import android.text.TextUtils
import sp.phone.mvp.contract.BoardContract
import sp.phone.mvp.model.entity.BoardCategory
import sp.phone.mvp.model.entity.Board
import sp.phone.mvp.model.entity.Board.BoardKey
import nosc.api.bean.CategoryBean
import com.alibaba.fastjson.JSON
import gov.anzong.androidnga.base.util.PreferenceUtils
import gov.anzong.androidnga.common.PreferenceKey
import sp.phone.util.StringUtils
import java.util.*

/**
 * Created by Justwen on 2019/6/23.
 */
object BoardModel : BaseModel(), BoardContract.Model {
    private val mBoardCategoryList: MutableList<BoardCategory> = ArrayList()
    private val mBookmarkCategory: BoardCategory
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

    @JvmStatic
    fun getInstance() = this

    private fun loadPreloadBoards(): List<BoardCategory> {
        val categoryJson = StringUtils.getStringFromAssets("json/category.json")
        val beans = JSON.parseArray(categoryJson, CategoryBean::class.java)
        val categories: MutableList<BoardCategory> = ArrayList()
        for (categoryBean in beans) {
            val category = BoardCategory(categoryBean.name)
            categoryBean?.groups?.forEach { group->
                val subCategory = BoardCategory(group.name)
                group.forums?.forEach {forum->
                    val boardName: String? = if (TextUtils.isEmpty(forum.nameS)) {
                        forum.name
                    } else {
                        forum.nameS
                    }
                    val board = Board(forum.fid, forum.stid, boardName)
                    board.boardHead = forum.head
                    subCategory.addBoard(board)
                }
                category.addSubCategory(subCategory)
            }
            categories.add(category)
        }
        upgradeBookmarkBoard(categories)
        return categories
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

    private fun loadBookmarkBoards(): BoardCategory {
        val category = BoardCategory("我的收藏")
        val bookmarkBoards =
            PreferenceUtils.getData(PreferenceKey.BOOKMARK_BOARD, Board::class.java)
        category.addBoards(bookmarkBoards)
        category.isBookmarkCategory = true
        return category
    }

    override fun addBookmark(board: Board) {
        if (!mBookmarkCategory.contains(board)) {
            mBookmarkCategory.addBoard(board)
            saveBookmark()
        }
    }

    override fun addBookmark(fid: Int, stid: Int, boardName: String) {
        addBookmark(BoardKey(fid, stid), boardName)
    }

    override fun addBookmark(boardKey: BoardKey, boardName: String) {
        if (!mBookmarkCategory.contains(boardKey)) {
            mBookmarkCategory.addBoard(Board(boardKey, boardName))
            saveBookmark()
        }
    }

    override fun removeBookmark(fid: Int, stid: Int) {
        if (mBookmarkCategory.removeBoard(BoardKey(fid, stid))) {
            saveBookmark()
        }
    }

    override fun removeAllBookmarks() {
        mBookmarkCategory.removeAllBoards()
        saveBookmark()
    }

    override fun isBookmark(fid: Int, stid: Int): Boolean {
        return mBookmarkCategory.contains(BoardKey(fid, stid))
    }

    override fun swapBookmark(from: Int, to: Int) {
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

    private fun saveBookmark() {
        PreferenceUtils.putData(
            PreferenceKey.BOOKMARK_BOARD,
            JSON.toJSONString(mBookmarkCategory.boardList)
        )
    }

    override fun getCategorySize(): Int {
        return mBoardCategoryList.size
    }

    override fun getBoardCategory(index: Int): BoardCategory {
        return mBoardCategoryList[index]
    }

    override fun getBoardCategories(): List<BoardCategory> {
        return mBoardCategoryList
    }

    override fun getBoardName(boardKey: BoardKey): String {
        for (boardCategory in mBoardCategoryList) {
            val board = boardCategory.getBoard(boardKey)
            if (board != null) {
                return board.name
            }
        }
        return ""
    }

    override fun getBoardName(fid: Int, stid: Int): String {
        return getBoardName(BoardKey(fid, stid))
    }

    override fun getBookmarkCategory(): BoardCategory {
        return mBookmarkCategory
    }


    private const val PRELOAD_BOARD_VERSION = 1

    init {
        mBookmarkCategory = loadBookmarkBoards()
        mBoardCategoryList.add(mBookmarkCategory)
        mBoardCategoryList.addAll(loadPreloadBoards())
    }
}