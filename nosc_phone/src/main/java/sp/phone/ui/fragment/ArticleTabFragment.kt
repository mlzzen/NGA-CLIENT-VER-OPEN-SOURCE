package sp.phone.ui.fragment

import gov.anzong.androidnga.R
import androidx.viewpager.widget.ViewPager
import sp.phone.ui.adapter.ArticlePagerAdapter
import sp.phone.param.ArticleListParam
import gov.nosc.ui.PageSelector
import com.getbase.floatingactionbutton.FloatingActionsMenu
import android.os.Bundle
import sp.phone.param.ParamKey
import nosc.viewmodel.ArticleShareViewModel
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.trello.rxlifecycle2.android.FragmentEvent
import android.content.Intent
import sp.phone.common.UserManagerImpl
import sp.phone.common.PhoneConfiguration
import sp.phone.util.ActivityUtils
import sp.phone.task.BookmarkTask
import sp.phone.theme.ThemeManager
import android.content.ClipData
import sp.phone.util.FunctionUtils
import sp.phone.ui.fragment.dialog.GotoDialogFragment
import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.text.TextUtils
import android.view.*
import androidx.fragment.app.DialogFragment
import gov.anzong.androidnga.Utils
import sp.phone.rxjava.RxBus
import sp.phone.rxjava.RxEvent
import sp.phone.util.StringUtils
import sp.phone.view.behavior.ScrollAwareFamBehavior
import java.lang.StringBuilder
import kotlin.math.ceil

/**
 * 帖子详情Fragment
 * Created by Justwen on 2017/7/9.
 */
class ArticleTabFragment : BaseRxFragment() {
    var mViewPager: ViewPager? = null
    private var mPagerAdapter: ArticlePagerAdapter? = null
    private var mRequestParam: ArticleListParam? = null

    var mTabLayout: PageSelector? = null

    var mFam: FloatingActionsMenu? = null
    private var mReplyCount = 0
    private var mBehavior: ScrollAwareFamBehavior? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mRequestParam = requireArguments().getParcelable(ParamKey.KEY_PARAM)

        val viewModel = activityViewModel
        viewModel.replyCount.observe(this, { replyCount: Int ->
            mReplyCount = replyCount
            val count = ceil((mReplyCount / 20.0f).toDouble()).toInt()
            if (count != mPagerAdapter!!.count) {
                mPagerAdapter!!.count = count
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return if (mConfig.isShowBottomTab) {
            inflater.inflate(R.layout.fragment_article_tab_bottom, container, false)
        } else {
            inflater.inflate(R.layout.fragment_article_tab, container, false)
        }.apply {
            mViewPager = findViewById(R.id.pager)
            mTabLayout = findViewById(R.id.tabs)
            mFam = findViewById(R.id.fab_menu)
            findViewById<View>(R.id.fab_post).setOnClickListener {
                reply()
            }

            findViewById<View>(R.id.fab_refresh).setOnClickListener {
                refresh()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        updateFloatingMenu()
        mPagerAdapter = ArticlePagerAdapter(childFragmentManager, mRequestParam!!)
        mViewPager?.adapter = mPagerAdapter
        mViewPager?.addOnPageChangeListener(object : SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                mBehavior!!.animateIn(mFam)
                super.onPageSelected(position)
            }
        })
        mTabLayout!!.bindViewPager(mViewPager)
        mTabLayout!!.setOnClickListener { v: View? -> createGotoDialog() }
        mFam!!.addFloatingActionButton.setOnLongClickListener { v: View? ->
            mBehavior!!.animateOut(mFam)
            true
        }
        super.onViewCreated(view, savedInstanceState)
    }

    private fun updateFloatingMenu() {
        val lp = mFam!!.layoutParams as CoordinatorLayout.LayoutParams
        mBehavior = lp.behavior as ScrollAwareFamBehavior?
        if (mConfig.isLeftHandMode) {
            lp.gravity = Gravity.START or Gravity.BOTTOM
            mFam!!.setExpandDirection(
                FloatingActionsMenu.EXPAND_UP,
                FloatingActionsMenu.LABELS_ON_RIGHT_SIDE
            )
            mFam!!.layoutParams = lp
        }
    }

    override fun onResume() {
        if (mFam != null) {
            mFam!!.collapse()
        }
        registerRxBus(FragmentEvent.PAUSE)
        super.onResume()
    }

    fun reply() {
        val intent = Intent()
        val tid = mRequestParam!!.tid.toString()
        intent.putExtra("prefix", "")
        intent.putExtra("tid", tid)
        intent.putExtra("action", "reply")
        if (!StringUtils.isEmpty(UserManagerImpl.getInstance().userName)) { // 登入了才能发
            intent.setClass(
                requireContext(),
                PhoneConfiguration.getInstance().postActivityClass
            )
        } else {
            intent.setClass(
                requireContext(),
                PhoneConfiguration.getInstance().loginActivityClass
            )
        }
        requireActivity().startActivityForResult(intent, ActivityUtils.REQUEST_CODE_TOPIC_POST)
    }

    fun refresh() {
        activityViewModel.setRefreshPage(mViewPager!!.currentItem + 1)
        mRequestParam!!.page = mViewPager!!.currentItem + 1
        mFam!!.collapse()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_add_bookmark -> BookmarkTask.execute(mRequestParam!!.tid)
            R.id.menu_share -> share()
            R.id.menu_copy_url -> copyUrl()
            R.id.menu_open_by_browser -> openByBrowser()
            R.id.menu_nightmode -> ThemeManager.getInstance().isNightMode = true
            R.id.menu_daymode -> ThemeManager.getInstance().isNightMode = false
            R.id.menu_download -> {
                mRequestParam!!.page = mViewPager!!.currentItem + 1
                activityViewModel.setCachePage(mRequestParam!!.page)
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private val activityViewModel: ArticleShareViewModel
        get() = activityViewModelProvider[ArticleShareViewModel::class.java]
    val url: String
        get() {
            val builder = StringBuilder()
            builder.append(Utils.getNGAHost()).append("read.php?")
            if (mRequestParam!!.pid != 0) {
                builder.append("pid=").append(mRequestParam!!.pid)
            } else {
                builder.append("tid=").append(mRequestParam!!.tid)
            }
            return builder.toString()
        }

    private fun copyUrl() {
        val clipboardManager =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (clipboardManager != null) {
            val clipData = ClipData.newPlainText("text", url)
            clipboardManager.setPrimaryClip(clipData)
            showToast("已经复制至粘贴板")
        }
    }

    private fun openByBrowser() {
        /*
        Intent intent = new Intent(getActivity(), WebViewActivity.class);
        intent.putExtra("path", getUrl());
        intent.putExtra("title",getActivity().getTitle());
        intent.putExtra("needJump",false);
        startActivity(intent);

         */
        FunctionUtils.openUrlByDefaultBrowser(mActivity, url)
    }

    private fun share() {
        val title = getString(R.string.share)
        val builder = StringBuilder()
        if (!TextUtils.isEmpty(requireActivity().title)) {
            builder.append("《").append(requireActivity().title).append("》 - 艾泽拉斯国家地理论坛，地址：")
        }
        builder.append(Utils.getNGAHost()).append("read.php?")
        if (mRequestParam!!.pid != 0) {
            builder.append("pid=").append(mRequestParam!!.pid).append(" (分享自NGA安卓客户端开源版)")
        } else {
            builder.append("tid=").append(mRequestParam!!.tid).append(" (分享自NGA安卓客户端开源版)")
        }
        FunctionUtils.share(context, title, builder.toString())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.article_list_option_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        if (ThemeManager.getInstance().isNightModeFollowSystem) {
            menu.findItem(R.id.menu_nightmode).isVisible = false
            menu.findItem(R.id.menu_daymode).isVisible = false
        } else if (ThemeManager.getInstance().isNightMode) {
            menu.findItem(R.id.menu_nightmode).isVisible = false
            menu.findItem(R.id.menu_daymode).isVisible = true
        } else {
            menu.findItem(R.id.menu_nightmode).isVisible = true
            menu.findItem(R.id.menu_daymode).isVisible = false
        }
        if (mRequestParam!!.pid != 0 || mRequestParam!!.topicInfo == null) {
            menu.findItem(R.id.menu_download).isVisible = false
        }
        super.onPrepareOptionsMenu(menu)
    }

    private fun createGotoDialog() {
        val args = Bundle()
        args.putInt("page", mPagerAdapter!!.count)
        args.putInt("floor", mReplyCount)
        args.putInt("currPage", mViewPager!!.currentItem)
        val df: DialogFragment = GotoDialogFragment()
        df.arguments = args
        df.setTargetFragment(this, ActivityUtils.REQUEST_CODE_JUMP_PAGE)
        val fm = requireActivity().supportFragmentManager
        val prev = fm.findFragmentByTag(GOTO_TAG)
        if (prev != null) {
            fm.beginTransaction().remove(prev).commit()
        }
        df.show(fm, GOTO_TAG)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ActivityUtils.REQUEST_CODE_TOPIC_POST && resultCode == Activity.RESULT_OK) {
            activityViewModel.setRefreshPage(mViewPager!!.currentItem + 1)
        } else if (requestCode == ActivityUtils.REQUEST_CODE_JUMP_PAGE) {
            if (data!!.hasExtra("page")) {
                mViewPager!!.currentItem = data.getIntExtra("page", 0)
            } else {
                val floor = data.getIntExtra("floor", 0)
                mViewPager!!.currentItem = floor / 20
                RxBus.getInstance().post(
                    RxEvent(
                        RxEvent.EVENT_ARTICLE_GO_FLOOR,
                        mViewPager!!.currentItem,
                        floor % 20
                    )
                )
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {
        private const val GOTO_TAG = "goto"
    }
}