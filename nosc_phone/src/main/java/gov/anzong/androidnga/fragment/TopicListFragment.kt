package gov.anzong.androidnga.fragment


import gov.anzong.androidnga.R
import com.getbase.floatingactionbutton.FloatingActionsMenu
import com.google.android.material.appbar.AppBarLayout
import android.os.Bundle
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.alibaba.android.arouter.launcher.ARouter
import gov.anzong.androidnga.arouter.ARouterConstants
import sp.phone.param.ParamKey
import sp.phone.mvp.model.entity.Board
import nosc.utils.uxUtils.ToastUtils
import android.content.Intent
import gov.anzong.androidnga.activity.LauncherSubActivity
import sp.phone.util.ActivityUtils
import android.app.Activity
import android.view.*
import androidx.appcompat.widget.Toolbar
import nosc.utils.startArticleActivity
import nosc.api.model.BoardModel
import sp.phone.common.appConfig

/**
 * Created by Justwen on 2017/11/19.
 */
class TopicListFragment : TopicSearchFragment() {
    private var mOptionMenu: Menu? = null

    var mFam: FloatingActionsMenu? = null

    var mAppBarLayout: AppBarLayout? = null

    var mToolbar: Toolbar? = null
    override fun setTitle() {
        if (mRequestParam.title != null) {
            setTitle(mRequestParam.title)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layoutId = R.layout.fragment_topic_list_board
        return inflater.inflate(layoutId, container, false).apply {
            mFam = findViewById(R.id.fab_menu)
            mAppBarLayout = findViewById(R.id.appbar)
            mToolbar = findViewById(R.id.toolbar)
            findViewById<View>(R.id.fab_post).setOnClickListener {
                startPostActivity()
            }

            findViewById<View>(R.id.fab_refresh).setOnClickListener {
                refresh()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateFloatingMenu()
    }

    private fun updateFloatingMenu() {
        if (appConfig.isLeftHandMode) {
            val lp = mFam!!.layoutParams as CoordinatorLayout.LayoutParams
            lp.gravity = Gravity.START or Gravity.BOTTOM
            mFam!!.setExpandDirection(
                FloatingActionsMenu.EXPAND_UP,
                FloatingActionsMenu.LABELS_ON_RIGHT_SIDE
            )
            mFam!!.layoutParams = lp
        }
    }

    override fun hideLoadingView() {
        val lp = mToolbar!!.layoutParams as AppBarLayout.LayoutParams
        lp.scrollFlags =
            AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
        super.hideLoadingView()
    }

    override fun scrollTo(position: Int) {
        if (position == 0) {
            mAppBarLayout!!.setExpanded(true, true)
        }
        super.scrollTo(position)
    }

    override fun onResume() {
        mFam!!.collapse()
        super.onResume()
    }

    private fun refresh() {
        mFam!!.collapse()
        viewModel.loadPage(1, mRequestParam)
    }

    fun startPostActivity() {
        ARouter.getInstance()
            .build(ARouterConstants.ACTIVITY_POST)
            .withInt(ParamKey.KEY_FID, mRequestParam.fid)
            .withString(
                ParamKey.KEY_STID,
                if (mRequestParam.stid != 0) mRequestParam.stid.toString() else null
            )
            .withString(ParamKey.KEY_ACTION, "new")
            .navigation()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        if (BoardModel.isBookmark(mRequestParam.fid, mRequestParam.stid)) {
            menu.findItem(R.id.menu_add_bookmark).isVisible = false
            menu.findItem(R.id.menu_remove_bookmark).isVisible = true
        } else {
            menu.findItem(R.id.menu_add_bookmark).isVisible = true
            menu.findItem(R.id.menu_remove_bookmark).isVisible = false
        }
        if (mTopicListInfo != null) {
            menu.findItem(R.id.menu_sub_board).isVisible = !mTopicListInfo.subBoardList.isEmpty()
        } else {
            menu.findItem(R.id.menu_sub_board).isVisible = false
        }
        if (mRequestParam.fid == 0 && mRequestParam.stid == 0) {
            menu.findItem(R.id.menu_add_bookmark).isVisible = false
            menu.findItem(R.id.menu_remove_bookmark).isVisible = false
        }
        menu.findItem(R.id.menu_board_head).isVisible = mRequestParam.boardHead != null
        super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.topic_list_menu, menu)
        mOptionMenu = menu
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_add_bookmark -> {
                val board = Board(mRequestParam.fid, mRequestParam.stid, mRequestParam.title)
                board.boardHead = mRequestParam.boardHead
                BoardModel.addBookmark(board)
                item.isVisible = false
                mOptionMenu!!.findItem(R.id.menu_remove_bookmark).isVisible = true
                ToastUtils.info(R.string.toast_add_bookmark_board)
            }
            R.id.menu_remove_bookmark -> {
                BoardModel.removeBookmark(mRequestParam.fid, mRequestParam.stid)
                item.isVisible = false
                mOptionMenu!!.findItem(R.id.menu_add_bookmark).isVisible = true
                ToastUtils.info(R.string.toast_remove_bookmark_board)
            }
            R.id.menu_sub_board -> showSubBoardList()
            R.id.menu_board_head -> requireActivity().startArticleActivity(
                mRequestParam.boardHead,
                mRequestParam.title + " - 版头"
            )
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun showSubBoardList() {
        val intent = Intent(context, LauncherSubActivity::class.java)
        intent.putExtra("fragment", BoardSubListFragment::class.java.name)
        intent.putExtra(ParamKey.KEY_TITLE, mRequestParam.title)
        intent.putExtra(ParamKey.KEY_FID, mRequestParam.fid)
        intent.putParcelableArrayListExtra("subBoard", mTopicListInfo.subBoardList)
        startActivityForResult(intent, ActivityUtils.REQUEST_CODE_SUB_BOARD)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ActivityUtils.REQUEST_CODE_SUB_BOARD && resultCode == Activity.RESULT_OK) {
            viewModel.loadPage(1, mRequestParam)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}