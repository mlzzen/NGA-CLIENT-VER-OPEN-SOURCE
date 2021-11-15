package gov.anzong.androidnga.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import gov.anzong.androidnga.R
import gov.anzong.androidnga.base.util.ToastUtils
import gov.anzong.androidnga.base.widget.DividerItemDecorationEx
import sp.phone.mvp.model.entity.ThreadPageInfo
import nosc.viewmodel.TopicListViewModel
import sp.phone.param.ParamKey
import sp.phone.param.TopicListParam
import sp.phone.ui.adapter.BaseAppendableAdapter
import sp.phone.ui.adapter.TopicListAdapter
import sp.phone.ui.fragment.TopicSearchFragment.handleClickEvent
import sp.phone.view.RecyclerViewEx

/**
 * 主题列表Base实现类，提供了列表解析、下拉刷新、上拉加载更多功能
 */
open class TopicListBaseFragment : BaseFragment(R.layout.fragment_topic_list_base), View.OnClickListener {

    protected lateinit var mRefreshLayout: SwipeRefreshLayout;

    protected lateinit var mListView: RecyclerViewEx;

    protected var mRequestParam: TopicListParam? = null

    protected lateinit var viewModel: TopicListViewModel

    protected lateinit var mAdapter: BaseAppendableAdapter<ThreadPageInfo, *>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mRequestParam = requireArguments().getParcelable(ParamKey.KEY_PARAM)
        viewModel = onCreatePresenter()
        lifecycle.addObserver(viewModel)
        initState()
    }

    private fun initState() {
        viewModel.isRefreshing.observe(this, Observer {
            mRefreshLayout.isRefreshing = it
        })
        viewModel.errorMsg.observe(this, Observer {
            ToastUtils.error(it)
        })
        viewModel.firstTopicList.observe(this, Observer {
            setData(it.threadPageList, false)
        })

        viewModel.nextTopicList.observe(this, Observer {
            setData(it.threadPageList, true)
        })
    }

    private fun setData(data: MutableList<ThreadPageInfo>, append: Boolean) {
        if (!append) {
            mAdapter.clear()
        }
        mAdapter.setData(data)
        mRefreshLayout.isRefreshing = false
    }

    protected open fun onCreatePresenter(): TopicListViewModel {
        val viewModelProvider = ViewModelProvider(this)
        val topicListPresenter = viewModelProvider[TopicListViewModel::class.java]
        topicListPresenter.setRequestParam(mRequestParam)
        return topicListPresenter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRefreshLayout = view.findViewById(R.id.swipe_refresh)
        mRefreshLayout.setOnRefreshListener { viewModel.loadPage(1, mRequestParam) }

        mAdapter = createAdapter()
        mAdapter.setOnClickListener(this)
        mListView = view.findViewById(R.id.list)
        mListView.layoutManager = LinearLayoutManager(context)
        mListView.adapter = mAdapter
        val padding = resources.getDimension(R.dimen.topic_list_item_padding)
        mListView.addItemDecoration(DividerItemDecorationEx(view.context, padding.toInt(), DividerItemDecoration.VERTICAL))
        mListView.setOnNextPageLoadListener {
            mRequestParam?.let{
                if (!mRefreshLayout.isRefreshing) {
                    viewModel.loadNextPage(mAdapter.nextPage, it)
                }
            }

        }
    }

    open fun createAdapter(): BaseAppendableAdapter<ThreadPageInfo, *> {
        return TopicListAdapter(context)
    }

    override fun onClick(v: View?) {
        handleClickEvent(context, v?.tag as ThreadPageInfo?, mRequestParam)
    }


}