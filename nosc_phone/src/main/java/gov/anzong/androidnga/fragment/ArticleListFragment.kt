package gov.anzong.androidnga.fragment

import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import androidx.appcompat.widget.AppCompatTextView
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.alibaba.android.arouter.launcher.ARouter
import gov.anzong.androidnga.R
import gov.anzong.androidnga.activity.BaseActivity
import gov.anzong.androidnga.arouter.ARouterConstants
import gov.anzong.androidnga.databinding.FragmentArticleListBinding
import gov.anzong.androidnga.fragment.dialog.BaseDialogFragment
import gov.anzong.androidnga.fragment.dialog.PostCommentDialogFragment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import nosc.api.ApiResult
import nosc.api.ERR
import nosc.api.OK
import nosc.api.bean.ThreadData
import nosc.api.bean.ThreadRowInfo
import nosc.ui.view.EmptyView
import nosc.ui.view.LoadingView
import nosc.utils.toUrl
import nosc.utils.uxUtils.ToastUtils
import nosc.viewmodel.ArticleShareViewModel
import sp.phone.common.UserManagerImpl
import sp.phone.mvp.contract.ArticleListContract
import sp.phone.mvp.model.ArticleListModel
import sp.phone.mvp.presenter.ArticleListPresenter
import sp.phone.param.ArticleListParam
import sp.phone.param.ParamKey
import sp.phone.rxjava.RxEvent
import sp.phone.task.BookmarkTask
import sp.phone.ui.adapter.ArticleListAdapter
import sp.phone.util.ActivityUtils
import sp.phone.util.FunctionUtils
import sp.phone.util.NLog
import sp.phone.util.StringUtils
import sp.phone.view.RecyclerViewEx

/*
 * MD 帖子详情每一页
 */
class ArticleListFragment : BaseRxFragment(),
    ArticleListContract.View {
    private var binding: FragmentArticleListBinding? = null

    val mListView: RecyclerViewEx? get() = binding?.list

    private val loadingView: LoadingView? get() = binding?.loadingView
    private val emptyView:EmptyView? get() = binding?.emptyView

    private val mSwipeRefreshLayout: SwipeRefreshLayout? get() = binding?.swipeRefresh
    private var mArticleAdapter: ArticleListAdapter? = null
    private val mRequestParam: ArticleListParam by lazy {
        (requireArguments().getParcelable(ParamKey.KEY_PARAM) as? ArticleListParam)!!
    }
    private val mMenuItemClickListener: OnTopicMenuItemClickListener =
        object : OnTopicMenuItemClickListener {
            private var mThreadRowInfo: ThreadRowInfo? = null
            override fun setThreadRowInfo(threadRowInfo: ThreadRowInfo?) {
                mThreadRowInfo = threadRowInfo
            }

            override fun onMenuItemClick(item: MenuItem): Boolean {
//                if (mPresenter == null) {
//                    return false
//                }
                val row = mThreadRowInfo
                val pidStr = row!!.pid.toString()
                val tidStr = row.tid.toString()
                val tid = row.tid
                when (item.itemId) {
                    R.id.menu_edit -> if (FunctionUtils.isComment(row)) {
                        ToastUtils.warn(R.string.cannot_edit_comment)
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
                    R.id.menu_post_comment -> mPresenter?.postComment(mRequestParam, row)?.apply {
                        showPostCommentDialog(first,second)
                    }
                    R.id.menu_report -> FunctionUtils.handleReport(
                        row,
                        mRequestParam.tid,
                        parentFragmentManager
                    )
                    R.id.menu_signature -> if (row.isanonymous) {
                        ToastUtils.info("这白痴匿名了,神马都看不到")
                    } else {
                        FunctionUtils.createSignatureDialog(row, activity)
                    }
                    R.id.menu_ban_this_one -> mPresenter?.banThisSB(row)
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
            val menuId: Int = R.menu.article_list_context_menu
            val popupMenu = PopupMenu(context, view)
            popupMenu.inflate(menuId)
            onPrepareOptionsMenu(popupMenu.menu, view.tag as ThreadRowInfo)
            popupMenu.show()
            popupMenu.setOnMenuItemClickListener(mMenuItemClickListener)
        }

        private fun onPrepareOptionsMenu(menu: Menu, row: ThreadRowInfo) {
            var item = menu.findItem(R.id.menu_ban_this_one)
            item?.setTitle(if (row._isInBlackList) R.string.cancel_ban_thisone else R.string.ban_thisone)

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
        registerRxBus()
        initData()
        super.onCreate(savedInstanceState)
    }

    private fun initData() {
        val viewModel = getActivityViewModelProvider()[ArticleShareViewModel::class.java]
        viewModel.refreshPage.observe(this) { page: Int ->
            if (page == mRequestParam.page) {
                loadPageFrom(requestFlow)
            }
        }
        viewModel.cachePage.observe(this) { page: Int ->
            if (page == mRequestParam.page) {
                ArticleListModel().cachePage(mRequestParam, mArticleAdapter?.getData()?.rawData!!)
            }
        }
    }

    override fun accept(rxEvent: RxEvent) {
        if (rxEvent.what == RxEvent.EVENT_ARTICLE_GO_FLOOR && rxEvent.arg + 1 == mRequestParam.page && rxEvent.obj != null) {
            mListView!!.scrollToPosition((rxEvent.obj as Int))
        }
    }

    private var mPresenter:ArticleListPresenter? = ArticleListPresenter()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
            mPresenter!!.postSupportTask(tid, row.pid) { supportNumChange ->
                // 就地修改ThreadRow数据并刷新页面
                row.score += supportNumChange
                (v.parent as View).findViewById<AppCompatTextView>(R.id.tv_score).text = row.score.toString()
            }
        }
        mArticleAdapter!!.setOpposeListener { v: View ->
            val row = v.tag as ThreadRowInfo
            val tid = row.tid
            mPresenter!!.postOpposeTask(tid, row.pid)
        }
        mListView!!.layoutManager = LinearLayoutManager(context)
        mListView!!.setItemViewCacheSize(20)
        mListView!!.adapter = mArticleAdapter
        mListView!!.setEmptyView(emptyView?.also {
            it.extraContent = {
                Button(onClick = { FunctionUtils.openUrlByDefaultBrowser(activity, mRequestParam.toUrl()) }) {
                    Text(text = "使用浏览器打开")
                }
            }
        })
        mSwipeRefreshLayout!!.setOnRefreshListener { loadPageFrom(requestFlow) }
        if (mRequestParam.loadCache) {
            loadPageFrom(cacheFlow)
        } else {
            loadPageFrom(requestFlow)
        }
        super.onViewCreated(view, savedInstanceState)
    }

    private val requestFlow by lazy{
        ArticleListModel().loadPage(mRequestParam)
    }
    private val cacheFlow by lazy{
        ArticleListModel().loadCachePage(mRequestParam)
    }

    private fun loadPageFrom(flow:Flow<ApiResult<ThreadData>>) {
        lifecycleScope.launch {
            setRefreshing(true)
            flow.collectLatest {
                when(it){
                    is OK ->{
                        setData(it.result)
                        setRefreshing(false)
                        hideLoadingView()
                    }
                    is ERR ->{
                        onError(it.msg)
                    }
                }
            }
        }
    }

    override fun setData(data: ThreadData?) {
        val viewModel = getActivityViewModelProvider()[ArticleShareViewModel::class.java]
        if (activity != null && data != null) {
            viewModel.setReplyCount(data.__ROWS)
        }
        if (data != null && activity != null && mRequestParam.title == null) {
            requireActivity().title = data.threadInfo.subject
        }
        if (data != null && data.rowList != null && !data.rowList.isEmpty()) {
            val rowInfo = data.rowList[0]
            if (rowInfo != null && rowInfo.lou == 0) {
                viewModel.setTopicOwner(rowInfo.author)
            }
        }
        if (mRequestParam.authorId == 0 && mRequestParam.searchPost == 0) {
            mArticleAdapter!!.setTopicOwner(viewModel.topicOwner.value)
        }
        mArticleAdapter!!.setData(data)
    }

    fun onError(text:String){
        hideLoadingView()
        setRefreshing(false)
        emptyView?.text = text
        //showToast(text)
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

    override fun hideLoadingView() {
        loadingView?.visibility = View.GONE
        mSwipeRefreshLayout?.visibility = View.VISIBLE
    }

    internal interface OnTopicMenuItemClickListener : PopupMenu.OnMenuItemClickListener {
        fun setThreadRowInfo(threadRowInfo: ThreadRowInfo?)
    }

    companion object {
        private val TAG = ArticleListFragment::class.java.simpleName
    }
}