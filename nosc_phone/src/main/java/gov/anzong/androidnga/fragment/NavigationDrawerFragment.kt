package gov.anzong.androidnga.fragment

import android.widget.AdapterView.OnItemClickListener
import gov.anzong.androidnga.base.widget.ViewFlipperEx
import android.widget.TextView
import sp.phone.ui.adapter.BoardPagerAdapter
import android.os.Bundle
import sp.phone.rxjava.RxEvent
import sp.phone.mvp.model.entity.Board
import gov.anzong.androidnga.R
import com.google.android.material.tabs.TabLayout
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.android.material.internal.NavigationMenuView
import sp.phone.ui.adapter.FlipperUserAdapter
import sp.phone.common.UserManagerImpl
import android.content.DialogInterface
import android.content.Intent
import sp.phone.util.ActivityUtils
import android.app.Activity
import android.preference.PreferenceManager
import android.text.TextUtils
import android.view.*
import android.view.animation.AnimationUtils
import gov.anzong.androidnga.common.PreferenceKey
import android.widget.AdapterView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import gov.anzong.androidnga.base.util.ToastUtils
import nosc.utils.jumpToLogin
import nosc.utils.showTopicList
import nosc.utils.toTopicListPage
import nosc.viewmodel.BoardCategoryViewModel
import sp.phone.common.User
import nosc.api.model.BoardModel
import nosc.api.model.BoardModel.addBookmark
import java.lang.NumberFormatException

/**
 * 首页的容器
 * Created by Justwen on 2017/6/29.
 */
class NavigationDrawerFragment : BaseRxFragment(),
    OnItemClickListener {
    private var mViewPager: ViewPager2? = null
    private lateinit var mHeaderView: ViewFlipperEx
    private var mReplyCountView: TextView? = null
    private var mBoardPagerAdapter: BoardPagerAdapter? = null
    private var tabLayoutMediator:TabLayoutMediator? = null

    private val viewModel:BoardCategoryViewModel by lazy{
        ViewModelProvider(this)[BoardCategoryViewModel::class.java]
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerRxBus()
        setHasOptionsMenu(true)
        viewModel.boardCategoryList.observe(this){
            mBoardPagerAdapter =
                BoardPagerAdapter(this, it)
            mViewPager?.adapter = mBoardPagerAdapter
            tabLayoutMediator?.apply {
                if(isAttached)
                    detach()
                attach()
            }
        }

        viewModel.query()
    }

    override fun accept(rxEvent: RxEvent) {
        if (rxEvent.what == RxEvent.EVENT_SHOW_TOPIC_LIST) {
            requireActivity().showTopicList(rxEvent.obj as Board)
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
        tabLayoutMediator = TabLayoutMediator(tabLayout,mViewPager!!){ tab,position ->
            tab.text = mBoardPagerAdapter?.getPageTitle(position)
            mViewPager?.setCurrentItem(tab.position,true)
        }
        tabLayout.tabMode = TabLayout.MODE_SCROLLABLE
        super.onViewCreated(view, savedInstanceState)
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
            onOptionsItemSelected(item)
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
        mReplyCountView?.text = count.toString()
    }

     private fun updateHeaderView() {
        val adapter = FlipperUserAdapter{
            toggleUser(it)
        }
        mHeaderView.adapter = adapter
        mHeaderView.inAnimation =
            AnimationUtils.loadAnimation(context, R.anim.right_in)
        mHeaderView.outAnimation =
            AnimationUtils.loadAnimation(context, R.anim.right_out)
        mHeaderView.displayedChild = UserManagerImpl.getInstance().activeUserIndex
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_option_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_add_id -> showAddBoardDialog()
            R.id.menu_login -> requireActivity().jumpToLogin()
            R.id.menu_clear_recent -> clearFavoriteBoards()
            R.id.menu_category_refresh -> viewModel.query()
            else -> return requireActivity().onOptionsItemSelected(item)
        }
        return true
    }

    private fun clearFavoriteBoards() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("是否要清空我的收藏？")
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int -> BoardModel.removeAllBookmarks() }
            .create()
            .show()
    }

    private fun showAddBoardDialog() {
        gov.anzong.androidnga.fragment.dialog.AddBoardDialogFragment()
            .setOnAddBookmarkListener { name: String, fid: String, stid: String ->
            addBoard(
                fid,
                name,
                stid
            )
        }
            .show(childFragmentManager)
    }

    fun addBoard(fidStr: String, name: String, stidStr: String): Boolean {
        return if (name == "") {
            ToastUtils.info("请输入版面名称")
            false
        } else {
            var fid = 0
            var stid = 0
            try {
                if (!TextUtils.isEmpty(fidStr)) {
                    fid = fidStr.toInt()
                }
                if (!TextUtils.isEmpty(stidStr)) {
                    stid = stidStr.toInt()
                }
                if (!addBookmark(Board(Board.BoardKey(fid, stid), name))) {
                    ToastUtils.info("该版面已存在")
                } else {
                    ToastUtils.success("添加成功")
                }
                true
            } catch (e: NumberFormatException) {
                ToastUtils.info("请输入正确的版面ID或者合集ID")
                false
            }
        }
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

    private fun toggleUser(userList: List<User?>?) {
        val mUserManager = UserManagerImpl.getInstance()
        if (userList != null && userList.size > 1) {
            val index: Int = switchToNextUser()
            mUserManager.setActiveUser(index)
            ToastUtils.info("切换账户成功,当前账户名:" + mUserManager.activeUser.nickName)
        } else {
            requireActivity().jumpToLogin()
        }
    }

    private fun switchToNextUser(): Int {
        mHeaderView.showPrevious()
        return mHeaderView.displayedChild
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        val fidString: String
        if (parent != null) {
            fidString = parent.getItemAtPosition(position) as String
            requireActivity().toTopicListPage(position, fidString)
        } else {
            requireActivity().showTopicList(view.tag as Board)
        }
    }
}