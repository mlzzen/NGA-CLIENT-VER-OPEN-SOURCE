package sp.phone.ui.fragment

import sp.phone.ui.fragment.BaseMvpFragment
import sp.phone.mvp.presenter.BoardPresenter
import sp.phone.mvp.contract.BoardContract
import android.widget.AdapterView.OnItemClickListener
import androidx.viewpager.widget.ViewPager
import gov.anzong.androidnga.base.widget.ViewFlipperEx
import android.widget.TextView
import sp.phone.ui.adapter.BoardPagerAdapter
import android.os.Bundle
import sp.phone.rxjava.RxEvent
import sp.phone.mvp.model.entity.Board
import android.view.LayoutInflater
import android.view.ViewGroup
import gov.anzong.androidnga.R
import com.google.android.material.tabs.TabLayout
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.android.material.internal.NavigationMenuView
import sp.phone.ui.adapter.FlipperUserAdapter
import sp.phone.common.UserManagerImpl
import android.content.DialogInterface
import android.content.Intent
import com.alibaba.android.arouter.launcher.ARouter
import gov.anzong.androidnga.arouter.ARouterConstants
import sp.phone.ui.fragment.dialog.AddBoardDialogFragment
import sp.phone.util.ActivityUtils
import android.app.Activity
import android.preference.PreferenceManager
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import gov.anzong.androidnga.common.PreferenceKey
import android.widget.AdapterView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import nosc.viewmodel.BoardCategoryViewModel

/**
 * 首页的容器
 * Created by Justwen on 2017/6/29.
 */
class NavigationDrawerFragment : BaseMvpFragment<BoardPresenter?>(), BoardContract.View,
    OnItemClickListener {
    private var mViewPager: ViewPager? = null
    private lateinit var mHeaderView: ViewFlipperEx
    private var mReplyCountView: TextView? = null
    private var mBoardPagerAdapter: BoardPagerAdapter? = null

    private val viewModel:BoardCategoryViewModel by lazy{
        ViewModelProvider(this).get(BoardCategoryViewModel::class.java)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerRxBus()
        viewModel.boardCategoryList.observe(this){
//            if (mBoardPagerAdapter == null) {
            mBoardPagerAdapter =
                BoardPagerAdapter(childFragmentManager, it)
            mViewPager!!.adapter = mBoardPagerAdapter
//                if (mPresenter!!.bookmarkCategory.size() == 0) {
//                    mViewPager!!.currentItem = 1
//                }
//            } else {
//                mBoardPagerAdapter!!.notifyDataSetChanged()
//            }
        }

        viewModel.query()
    }

    override fun accept(rxEvent: RxEvent) {
        if (rxEvent.what == RxEvent.EVENT_SHOW_TOPIC_LIST) {
            mPresenter!!.showTopicList(rxEvent.obj as Board)
        } else {
            super.accept(rxEvent)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_navigation_drawer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        setupToolbar(toolbar)
        initDrawerLayout(view, toolbar)
        initNavigationView(view)
        mViewPager = view.findViewById(R.id.pager)
        val tabLayout: TabLayout = view.findViewById(R.id.tabs)
        tabLayout.setupWithViewPager(mViewPager)
        tabLayout.tabMode = TabLayout.MODE_SCROLLABLE
        super.onViewCreated(view, savedInstanceState)
        mPresenter!!.loadBoardInfo()
    }

    override fun onCreatePresenter(): BoardPresenter {
        return BoardPresenter()
    }

    private fun initDrawerLayout(rootView: View, toolbar: Toolbar) {
        val drawerLayout: DrawerLayout = rootView.findViewById(R.id.drawer_layout)
        val drawerToggle = ActionBarDrawerToggle(
            activity,
            drawerLayout,
            toolbar,
            R.string.app_name,
            R.string.app_name
        )
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
    }

    private fun initNavigationView(rootView: View) {
        val navigationView: NavigationView = rootView.findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { item: MenuItem ->
            onOptionsItemSelected(
                item
            )
        }
        val menuItem = navigationView.menu.findItem(R.id.menu_gun)
        val menuView = navigationView.getChildAt(0) as NavigationMenuView
        menuView.isVerticalScrollBarEnabled = false
        val actionView = layoutInflater.inflate(R.layout.nav_menu_action_view_gun, null)
        menuItem.actionView = actionView
        menuItem.expandActionView()
        mReplyCountView = actionView.findViewById(R.id.reply_count)
        mHeaderView = navigationView.getHeaderView(0).findViewById(R.id.viewFlipper)
        updateHeaderView()
    }

    private fun setReplyCount(count: Int) {
        mReplyCountView!!.text = count.toString()
    }

    override fun updateHeaderView() {
        val adapter = FlipperUserAdapter(mPresenter)
        mHeaderView.adapter = adapter
        mHeaderView.inAnimation =
            AnimationUtils.loadAnimation(context, R.anim.right_in)
        mHeaderView.outAnimation =
            AnimationUtils.loadAnimation(context, R.anim.right_out)
        mHeaderView.displayedChild = UserManagerImpl.getInstance().activeUserIndex
    }

    override fun notifyDataSetChanged() {
        mBoardPagerAdapter!!.notifyDataSetChanged()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_add_id -> showAddBoardDialog()
            R.id.menu_login -> jumpToLogin()
            R.id.menu_clear_recent -> clearFavoriteBoards()
            else -> return requireActivity().onOptionsItemSelected(item)
        }
        return true
    }

    private fun clearFavoriteBoards() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("是否要清空我的收藏？")
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int -> mPresenter!!.clearRecentBoards() }
            .create()
            .show()
    }

    override fun jumpToLogin() {
        ARouter.getInstance().build(ARouterConstants.ACTIVITY_LOGIN).navigation(activity, 1)
    }

    private fun showAddBoardDialog() {
        AddBoardDialogFragment().setOnAddBookmarkListener { name: String?, fid: String?, stid: String? ->
            mPresenter?.addBoard(
                fid,
                name,
                stid
            )
        }
            .show(childFragmentManager)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ActivityUtils.REQUEST_CODE_LOGIN && resultCode == Activity.RESULT_OK || requestCode == ActivityUtils.REQUEST_CODE_SETTING) {
            mHeaderView?.adapter?.notifyDataSetChanged()
        }
    }

    override fun onResume() {
        setReplyCount(
            PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(PreferenceKey.KEY_REPLY_COUNT, 0)
        )
        val um = UserManagerImpl.getInstance()
        if (um.userSize > 0 && um.activeUserIndex != mHeaderView.displayedChild) {
            mHeaderView.displayedChild = um.activeUserIndex
        }
        super.onResume()
    }

    override fun switchToNextUser(): Int {
        mHeaderView.showPrevious()
        return mHeaderView.displayedChild
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        val fidString: String
        if (parent != null) {
            fidString = parent.getItemAtPosition(position) as String
            mPresenter!!.toTopicListPage(position, fidString)
        } else {
            mPresenter!!.showTopicList(view.tag as Board)
        }
    }
}