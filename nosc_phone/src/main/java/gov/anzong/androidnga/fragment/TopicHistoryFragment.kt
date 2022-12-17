package gov.anzong.androidnga.fragment

import android.os.Bundle
import android.view.*
import androidx.compose.ui.platform.ComposeView
import com.alibaba.fastjson.JSON
import gov.anzong.androidnga.R
import nosc.utils.startArticleActivity
import nosc.utils.uxUtils.showConfirmDialog
import sp.phone.common.TopicHistoryManager
import sp.phone.mvp.model.entity.ThreadPageInfo
import sp.phone.mvp.model.entity.TopicListInfo
import sp.phone.param.ArticleListParam
import sp.phone.ui.adapter.TopicListViewState

/**
 * Created by Justwen on 2018/1/17.
 */
class TopicHistoryFragment : BaseFragment() {
    private var mTopicListViewState: TopicListViewState? = null
    private val mTopicHistoryManager: TopicHistoryManager = TopicHistoryManager.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        setTitle(R.string.label_activity_topic_history)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(inflater.context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mTopicListViewState = TopicListViewState()
        mTopicListViewState?.onItemClick = { info ->
            val param = ArticleListParam()
            param.tid = info.tid
            param.page = info.page
            param.title = info.subject
            param.topicInfo = JSON.toJSONString(info)
            context?.startArticleActivity(param)
        }
        super.onViewCreated(view, savedInstanceState)
        (view as ComposeView).setContent {
            mTopicListViewState?.Content()
        }
        mTopicListViewState?.onItemLongClick = {
            mTopicHistoryManager.removeTopicHistory(it)
            setData(mTopicHistoryManager.topicHistoryList)
        }
        setData(mTopicHistoryManager.topicHistoryList)
    }

    private fun setData(topicLIst: List<ThreadPageInfo>) {
        val listInfo = TopicListInfo()
        listInfo.threadPageList = topicLIst
        mTopicListViewState?.setData(listInfo.threadPageList)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.settings_black_list_option_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.menu_delete_all) {
            requireActivity().showConfirmDialog( "确认删除所有浏览历史吗") {
                mTopicHistoryManager.removeAllTopicHistory()
                mTopicListViewState?.setData(mTopicHistoryManager.topicHistoryList)
            }
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}