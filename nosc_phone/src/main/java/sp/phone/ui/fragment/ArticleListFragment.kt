package sp.phone.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.alibaba.android.arouter.launcher.ARouter
import gov.anzong.androidnga.R
import gov.anzong.androidnga.activity.BaseActivity
import gov.anzong.androidnga.arouter.ARouterConstants
import gov.anzong.androidnga.base.util.ToastUtils
import gov.anzong.androidnga.databinding.FragmentArticleListBinding
import nosc.api.bean.ThreadData
import nosc.api.bean.ThreadRowInfo
import sp.phone.common.PhoneConfiguration
import sp.phone.common.UserManagerImpl
import sp.phone.mvp.contract.ArticleListContract
import sp.phone.mvp.presenter.ArticleListPresenter
import nosc.viewmodel.ArticleShareViewModel
import sp.phone.param.ArticleListParam
import sp.phone.param.ParamKey
import sp.phone.rxjava.RxEvent
import sp.phone.task.BookmarkTask
import sp.phone.ui.adapter.ArticleListAdapter
import sp.phone.ui.fragment.dialog.BaseDialogFragment
import sp.phone.ui.fragment.dialog.PostCommentDialogFragment
import sp.phone.util.ActivityUtils
import sp.phone.util.FunctionUtils
import sp.phone.util.NLog
import sp.phone.util.StringUtils
import sp.phone.view.RecyclerViewEx

/*
 * MD 帖子详情每一页
 */
open class ArticleListFragment : BaseMvpFragment<ArticleListPresenter?>(),
    ArticleListContract.View {
    private var binding: FragmentArticleListBinding? = null

    val mListView: RecyclerViewEx? get() = binding?.list

    val mLoadingView: View? get() = binding?.loading?.loadingView

    val mSwipeRefreshLayout: SwipeRefreshLayout? get() = binding?.swipeRefresh
    private var mArticleAdapter: ArticleListAdapter? = null
    @JvmField
    protected var mRequestParam: ArticleListParam? = null
    private val mMenuItemClickListener: OnTopicMenuItemClickListener =
        object : OnTopicMenuItemClickListener {
            private var mThreadRowInfo: ThreadRowInfo? = null
            override fun setThreadRowInfo(threadRowInfo: ThreadRowInfo?) {
                mThreadRowInfo = threadRowInfo
            }

            override fun onMenuItemClick(item: MenuItem): Boolean {
                if (mPresenter == null) {
                    return false
                }
                val row = mThreadRowInfo
                val pidStr = row!!.pid.toString()
                val tidStr = row.tid.toString()
                val tid = row.tid
                when (item.itemId) {
                    R.id.menu_edit -> if (FunctionUtils.isComment(row)) {
                        showToast(R.string.cannot_eidt_comment)
                    } else {
                        ARouter.getInstance()
                            .build(ARouterConstants.ACTIVITY_POST)
                            .withString(ParamKey.KEY_PID, pidStr)
                            .withString(ParamKey.KEY_TID, tidStr)
                            .withString("title", StringUtils.unEscapeHtml(row.subject))
                            .withString("action", "modify")
                            .withString(
                                "prefix", StringUtils.unEscapeHtml(
                                    StringUtils.removeBrTag(
                                        row.content
                                    )
                                )
                            )
                            .navigation(activity, ActivityUtils.REQUEST_CODE_LOGIN)
                    }
                    R.id.menu_post_comment -> mPresenter!!.postComment(mRequestParam, row)
                    R.id.menu_report -> FunctionUtils.handleReport(
                        row,
                        mRequestParam!!.tid,
                        fragmentManager
                    )
                    R.id.menu_signature -> if (row.isanonymous) {
                        ToastUtils.info("这白痴匿名了,神马都看不到")
                    } else {
                        FunctionUtils.Create_Signature_Dialog(
                            row, activity,
                            mListView
                        )
                    }
                    R.id.menu_ban_this_one -> mPresenter!!.banThisSB(row)
                    R.id.menu_show_this_person_only -> ARouter.getInstance()
                        .build(ARouterConstants.ACTIVITY_TOPIC_CONTENT)
                        .withString("tab", "1")
                        .withInt(ParamKey.KEY_TID, tid)
                        .withInt(ParamKey.KEY_AUTHOR_ID, row.authorid)
                        .withInt("fromreplyactivity", 1)
                        .navigation()
                    R.id.menu_favorite -> BookmarkTask.execute(tidStr, pidStr)
                    else -> {
                    }
                }
                return false
            }
        }
    private val mMenuTogglerListener: View.OnClickListener = object : View.OnClickListener {
        override fun onClick(view: View) {
            mMenuItemClickListener.setThreadRowInfo(view.tag as ThreadRowInfo)
            val menuId: Int = if (mRequestParam!!.pid == 0) {
                R.menu.article_list_context_menu
            } else {
                R.menu.article_list_context_menu_with_tid
            }
            val popupMenu = PopupMenu(context, view)
            popupMenu.inflate(menuId)
            onPrepareOptionsMenu(popupMenu.menu, view.tag as ThreadRowInfo)
            popupMenu.show()
            popupMenu.setOnMenuItemClickListener(mMenuItemClickListener)
        }

        private fun onPrepareOptionsMenu(menu: Menu, row: ThreadRowInfo) {
            var item = menu.findItem(R.id.menu_ban_this_one)
            item?.setTitle(if (row._isInBlackList) R.string.cancel_ban_thisone else R.string.ban_thisone)

//            item = menu.findItem(R.id.menu_vote);
//            if (item != null && StringUtils.isEmpty(row.getVote())) {
//                item.setVisible(false);
//            }
            item = menu.findItem(R.id.menu_edit)
            if (item != null) {
                val user = UserManagerImpl.getInstance().activeUser
                if (user == null || user.userId != row.authorid.toString()) {
                    item.isVisible = false
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        NLog.d(TAG, "onCreate")
        mRequestParam = requireArguments().getParcelable(ParamKey.KEY_PARAM)
        registerRxBus()
        initData()
        super.onCreate(savedInstanceState)
    }

    private fun initData() {
        val viewModel = activityViewModelProvider.get(
            ArticleShareViewModel::class.java
        )
        viewModel.refreshPage.observe(this, { page: Int ->
            if (page == mRequestParam!!.page) {
                loadPage()
            }
        })
        viewModel.cachePage.observe(this, { page: Int ->
            if (page == mRequestParam!!.page) {
                mPresenter!!.cachePage()
            }
        })
    }

    override fun accept(rxEvent: RxEvent) {
        if (rxEvent.what == RxEvent.EVENT_ARTICLE_GO_FLOOR && rxEvent.arg + 1 == mRequestParam!!.page && rxEvent.obj != null) {
            mListView!!.scrollToPosition((rxEvent.obj as Int))
        }
    }

    override fun onCreatePresenter(): ArticleListPresenter {
        return ArticleListPresenter(mRequestParam)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentArticleListBinding.inflate(layoutInflater, container, false).also {
            binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as BaseActivity?)?.setupToolbar()
        mArticleAdapter = ArticleListAdapter(requireContext(), requireActivity().supportFragmentManager)
        mArticleAdapter!!.setMenuTogglerListener(mMenuTogglerListener)
        mArticleAdapter!!.setSupportListener { v: View ->
            val row = v.tag as ThreadRowInfo
            val tid = row.tid
            mPresenter!!.postSupportTask(tid, row.pid)
        }
        mArticleAdapter!!.setOpposeListener { v: View ->
            val row = v.tag as ThreadRowInfo
            val tid = row.tid
            mPresenter!!.postOpposeTask(tid, row.pid)
        }
        mListView!!.layoutManager = LinearLayoutManager(context)
        mListView!!.setItemViewCacheSize(20)
        mListView!!.adapter = mArticleAdapter
        mListView!!.setEmptyView(view.findViewById(R.id.empty_view))
        val sayingView = mLoadingView!!.findViewById<View>(R.id.saying) as TextView
        sayingView.text = ActivityUtils.getSaying()
        mSwipeRefreshLayout!!.setOnRefreshListener { loadPage() }
        super.onViewCreated(view, savedInstanceState)
    }

    fun loadPage() {
        mPresenter!!.loadPage(mRequestParam)
    }

    override fun setData(data: ThreadData?) {
        val viewModel = activityViewModelProvider.get(
            ArticleShareViewModel::class.java
        )
        if (activity != null && data != null) {
            viewModel.setReplyCount(data.__ROWS)
        }
        if (data != null && activity != null && mRequestParam!!.title == null) {
            requireActivity().title = data.threadInfo.subject
        }
        if (data != null && data.rowList != null && !data.rowList.isEmpty()) {
            val rowInfo = data.rowList[0]
            if (rowInfo != null && rowInfo.lou == 0) {
                viewModel.setTopicOwner(rowInfo.author)
            }
        }
        if (mRequestParam!!.authorId == 0 && mRequestParam!!.searchPost == 0) {
            mArticleAdapter!!.setTopicOwner(viewModel.topicOwner.value)
        }
        mArticleAdapter!!.setData(data)
        mArticleAdapter!!.notifyDataSetChanged()
    }

    override fun startPostActivity(intent: Intent) {
        if (!StringUtils.isEmpty(UserManagerImpl.getInstance().userName)) { // 登入了才能发
            intent.setClass(requireActivity(), PhoneConfiguration.getInstance().postActivityClass)
        } else {
            intent.setClass(requireActivity(), PhoneConfiguration.getInstance().loginActivityClass)
        }
        startActivityForResult(intent, ActivityUtils.REQUEST_CODE_TOPIC_POST)
    }

    override fun showPostCommentDialog(prefix: String, bundle: Bundle) {
        val df: BaseDialogFragment = PostCommentDialogFragment()
        df.arguments = bundle
        df.show(requireActivity().supportFragmentManager)
    }

    override fun setRefreshing(refreshing: Boolean) {
        if (mSwipeRefreshLayout!!.isShown) {
            mSwipeRefreshLayout!!.isRefreshing = refreshing
        }
    }

    override fun isRefreshing(): Boolean {
        return if (mSwipeRefreshLayout!!.isShown) mSwipeRefreshLayout!!.isRefreshing else mLoadingView!!.isShown
    }

    override fun hideLoadingView() {
        mLoadingView!!.visibility = View.GONE
        mSwipeRefreshLayout!!.visibility = View.VISIBLE
    }

    internal interface OnTopicMenuItemClickListener : PopupMenu.OnMenuItemClickListener {
        fun setThreadRowInfo(threadRowInfo: ThreadRowInfo?)
    }

    companion object {
        private val TAG = ArticleListFragment::class.java.simpleName
    }
}