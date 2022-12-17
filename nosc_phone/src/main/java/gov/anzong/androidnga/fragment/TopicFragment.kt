package gov.anzong.androidnga.fragment

import android.content.Context
import sp.phone.param.TopicListParam
import sp.phone.ui.adapter.TopicListViewState
import sp.phone.mvp.model.entity.TopicListInfo
import androidx.compose.ui.platform.ComposeView
import nosc.viewmodel.TopicListViewModel
import android.os.Bundle
import sp.phone.param.ParamKey
import android.view.LayoutInflater
import android.view.ViewGroup
import gov.anzong.androidnga.R
import gov.anzong.androidnga.activity.BaseActivity
import nosc.utils.uxUtils.ToastUtils
import sp.phone.mvp.model.entity.ThreadPageInfo
import sp.phone.util.ARouterUtils
import gov.anzong.androidnga.arouter.ARouterConstants
import nosc.api.constants.ApiConstants
import com.alibaba.android.arouter.launcher.ARouter
import sp.phone.param.ArticleListParam
import com.alibaba.fastjson.JSON
import android.content.Intent
import android.text.TextUtils
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import sp.phone.common.PhoneConfiguration
import sp.phone.common.TopicHistoryManager
import sp.phone.util.StringUtils

open class TopicFragment : BaseFragment() {
    protected val mRequestParam: TopicListParam by lazy {
        requireArguments().getParcelable<TopicListParam>(ParamKey.KEY_PARAM) as TopicListParam
    }
    @JvmField
    protected var mAdapter: TopicListViewState? = null
    protected var mTopicListInfo: TopicListInfo? = null

    private var mListView: ComposeView? = null
    private var mLoadingView: View? = null
    @JvmField
    protected var viewModel: TopicListViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
        setTitle()
        viewModel = onCreateViewModel()
        lifecycle.addObserver(viewModel!!)
    }

    private fun onCreateViewModel(): TopicListViewModel {
        val viewModelProvider = ViewModelProvider(this)
        val topicListViewModel = viewModelProvider.get(
            TopicListViewModel::class.java
        )
        topicListViewModel.setRequestParam(mRequestParam)
        return topicListViewModel
    }

    protected open fun setTitle() {
        if (!mRequestParam.key.isNullOrEmpty()) {
            if (mRequestParam.content == 1) {
                if (!StringUtils.isEmpty(mRequestParam.fidGroup)) {
                    setTitle("搜索全站(包含正文):" + mRequestParam.key)
                } else {
                    setTitle("搜索(包含正文):" + mRequestParam.key)
                }
            } else {
                if (!StringUtils.isEmpty(mRequestParam.fidGroup)) {
                    setTitle("搜索全站:" + mRequestParam.key)
                } else {
                    setTitle("搜索:" + mRequestParam.key)
                }
            }
        } else if (!StringUtils.isEmpty(mRequestParam.author)) {
            if (mRequestParam.searchPost > 0) {
                val title = "搜索" + mRequestParam.author + "的回复"
                setTitle(title)
            } else {
                val title = "搜索" + mRequestParam.author + "的主题"
                setTitle(title)
            }
        } else if (mRequestParam.recommend == 1) {
            setTitle(mRequestParam.title + " - 精华区")
        } else if (mRequestParam.twentyfour == 1) {
            setTitle(mRequestParam.title + " - 24小时热帖")
        } else if (!TextUtils.isEmpty(mRequestParam.title)) {
            setTitle(mRequestParam.title)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layoutId = R.layout.fragment_topic_list
        return inflater.inflate(layoutId, container, false)
    }

    protected open val onItemClick:(ThreadPageInfo)->Unit =  {
        handleClickEvent(requireContext(), it, mRequestParam)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mListView = view.findViewById(R.id.list)
        mLoadingView = view.findViewById(R.id.loading_view)
        (activity as BaseActivity?)!!.setupToolbar()
        mAdapter = TopicListViewState()
        mAdapter?.onItemClick = onItemClick
        mListView?.setContent {
            mAdapter?.Content()
        }
        super.onViewCreated(view, savedInstanceState)
        viewModel!!.firstTopicList.observe(viewLifecycleOwner) { topicListInfo: TopicListInfo? ->
            scrollTo(0)
            topicListInfo?.let { setData(it) }
        }
        viewModel!!.nextTopicList.observe(viewLifecycleOwner) { result: TopicListInfo ->
            appendData(result)
        }
        viewModel!!.errorMsg.observe(viewLifecycleOwner) { res: String? ->
            setNextPageEnabled(false)
            ToastUtils.error(res ?:return@observe)
        }
        viewModel!!.isRefreshing.observe(viewLifecycleOwner) { aBoolean: Boolean ->
            mAdapter?.isRefreshing = aBoolean
            if (!aBoolean) {
                hideLoadingView()
            }
        }
    }

    open fun scrollTo(position: Int) {
        lifecycleScope.launch {
            mAdapter?.scrollState?.scrollToItem(position)
        }
    }

    fun setNextPageEnabled(enabled: Boolean) {
        mAdapter!!.setNextPageEnabled(enabled)
    }

    fun removeTopic(pageInfo: ThreadPageInfo?) {
        mAdapter?.removeItem(pageInfo?:return)
    }

    open fun hideLoadingView() {
        if (mLoadingView!!.visibility == View.VISIBLE) {
            mLoadingView!!.visibility = View.GONE
        }
    }

    open fun setData(result: TopicListInfo) {
        mTopicListInfo = result
        mAdapter!!.setData(result.threadPageList)
    }

    private fun appendData(result: TopicListInfo) {
        mTopicListInfo = result
        mAdapter!!.appendData(result.threadPageList)
    }

    companion object {
        const val REQUEST_IMPORT_CACHE = 0
        fun handleClickEvent(
            context: Context,
            info: ThreadPageInfo,
            requestParam: TopicListParam?
        ) {
            if (info.isMirrorBoard) {
                ARouterUtils.build(ARouterConstants.ACTIVITY_TOPIC_LIST)
                    .withInt(ParamKey.KEY_FID, info.fid)
                    .withString(ParamKey.KEY_TITLE, info.subject)
                    .navigation(context)
            } else if (info.type and ApiConstants.MASK_TYPE_ASSEMBLE == ApiConstants.MASK_TYPE_ASSEMBLE) {
                val param = TopicListParam()
                param.title = info.subject
                param.stid = info.tid
                ARouter.getInstance().build(ARouterConstants.ACTIVITY_TOPIC_LIST)
                    .withParcelable(ParamKey.KEY_PARAM, param)
                    .navigation()
            } else {
                val param = ArticleListParam()
                param.tid = info.tid
                param.page = info.page
                param.title = StringUtils.unEscapeHtml(info.subject)
                if (requestParam!!.searchPost != 0) {
                    param.pid = info.pid
                    param.authorId = info.authorId
                    param.searchPost = requestParam.searchPost
                }
                param.topicInfo = JSON.toJSONString(info)
                val intent = Intent()
                val bundle = Bundle()
                bundle.putParcelable(ParamKey.KEY_PARAM, param)
                intent.putExtras(bundle)
                intent.setClass(context, PhoneConfiguration.articleActivityClass)
                context.startActivity(intent)
                TopicHistoryManager.getInstance().addTopicHistory(info)
            }
        }
    }
}