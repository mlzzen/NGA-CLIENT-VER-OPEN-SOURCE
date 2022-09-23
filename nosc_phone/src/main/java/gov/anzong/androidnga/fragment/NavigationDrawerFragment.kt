package gov.anzong.androidnga.fragment

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.TextUtils
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.internal.NavigationMenuView
import com.google.android.material.tabs.TabLayoutMediator
import gov.anzong.androidnga.R
import gov.anzong.androidnga.databinding.FragmentNavigationDrawerBinding
import nosc.api.model.BoardModel
import nosc.api.model.BoardModel.addBookmark
import nosc.ui.NOSCTheme
import nosc.ui.view.BoardItemContent
import nosc.ui.view.RecyclerViewFlipper
import nosc.utils.PreferenceKey
import nosc.utils.jumpToLogin
import nosc.utils.showTopicList
import nosc.utils.toTopicListPage
import nosc.utils.uxUtils.ToastUtils
import nosc.utils.uxUtils.showConfirmDialog
import nosc.viewmodel.BoardCategoryViewModel
import sp.phone.common.User
import sp.phone.common.UserManagerImpl
import sp.phone.mvp.model.entity.Board
import sp.phone.rxjava.RxBus
import sp.phone.rxjava.RxEvent
import sp.phone.ui.adapter.FlipperUserAdapter
import sp.phone.util.ActivityUtils

/**
 * 首页的容器
 * Created by Justwen on 2017/6/29.
 */
class NavigationDrawerFragment : BaseRxFragment(),
    OnItemClickListener {
    private var binding:FragmentNavigationDrawerBinding? = null
    private var mHeaderView: RecyclerViewFlipper<FlipperUserAdapter.UserViewHolder>? = null
    private var mReplyCountView: TextView? = null
    private var tabLayoutMediator:TabLayoutMediator? =   null

    private val viewModel:BoardCategoryViewModel by lazy{
        ViewModelProvider(this)[BoardCategoryViewModel::class.java]
    }


    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerRxBus()
        setHasOptionsMenu(true)
        viewModel.boardCategoryList.observe(this){
            binding?.apply {
                container.setContent {
                    NOSCTheme {
                        var refresh by remember {
                            mutableStateOf(false)
                        }
                        var filter by remember {
                            mutableStateOf("")
                        }

                        val onChange = { refresh = !refresh }
                        key(refresh) {
                            LazyVerticalGrid(columns = GridCells.Adaptive(110.dp)){
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    OutlinedTextField(
                                        value = filter,
                                        onValueChange = {filter = it.trim()},
                                        maxLines = 1,
                                        label = { Text("快速检索版面") },
                                        modifier = Modifier.padding(4.dp).fillMaxWidth()
                                    )
                                }
                                it.forEach{ cat ->
                                    cat.subCategoryList.forEach { sCat ->
                                        item(
                                            key = "c/${sCat.name}",
                                            span = { GridItemSpan(maxLineSpan) },
                                            contentType = 1
                                        ){
                                            Row(
                                                Modifier.animateContentSize().padding(8.dp),
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
                                            sCat.boardList.filter { it.name.contains(filter) },
                                            key = { "c/${sCat.name}/${it.name}" },
                                            span = { GridItemSpan(1) },
                                            contentType = { 0 }
                                        ){ b->
                                            val context = LocalContext.current
                                            BoardItemContent(
                                                Modifier
                                                    .animateContentSize()
                                                    .padding(8.dp)
                                                    .fillMaxWidth()
                                                    .combinedClickable(
                                                        onClick = {
                                                            BoardModel.addRecentBoard(b)
                                                            RxBus.getInstance().post(RxEvent(RxEvent.EVENT_SHOW_TOPIC_LIST, b))
                                                            if(cat.isBookmarkCategory){
                                                                onChange()
                                                            }
                                                        },
                                                        onLongClick = {
                                                            if (cat.isBookmarkCategory) {
                                                                context.showConfirmDialog("确定要删除吗？") {
                                                                    BoardModel.removeBookmark(b.fid, b.stid)
                                                                    onChange()
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
                tabLayoutMediator?.apply {
                    if(isAttached)
                        detach()
                    attach()
                }
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
    ): View {
        return FragmentNavigationDrawerBinding.inflate(inflater,container,false).let{
            binding = it
            it.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding?.apply {
            setupToolbar(toolbar)
            initDrawerLayout(view, toolbar)
            navView.apply{
                setNavigationItemSelectedListener { item: MenuItem ->
                    onOptionsItemSelected(item)
                }
                menu.findItem(R.id.menu_gun).apply {
                    actionView = layoutInflater.inflate(R.layout.nav_menu_action_view_gun, null)
                    mReplyCountView = actionView.findViewById(R.id.reply_count)
                }

                (getChildAt(0) as NavigationMenuView).apply{
                    isVerticalScrollBarEnabled = false
                }

                mHeaderView = getHeaderView(0).findViewById(R.id.viewFlipper)
            }

            updateHeaderView()
        }

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

    private fun setReplyCount(count: Int) {
        mReplyCountView?.text = count.toString()
    }

     private fun updateHeaderView() {
        mHeaderView?.apply {
            adapter = FlipperUserAdapter{
                toggleUser(it)
            }
            inAnimation =
                AnimationUtils.loadAnimation(context, R.anim.right_in)
            outAnimation =
                AnimationUtils.loadAnimation(context, R.anim.right_out)
            displayedChild = UserManagerImpl.getInstance().activeUserIndex
        }
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
        }.show(childFragmentManager)
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
        if (um.userSize > 0 && um.activeUserIndex != mHeaderView?.displayedChild) {
            mHeaderView?.displayedChild = um.activeUserIndex
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
        mHeaderView?.showPrevious()
        return mHeaderView?.displayedChild ?: 0
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