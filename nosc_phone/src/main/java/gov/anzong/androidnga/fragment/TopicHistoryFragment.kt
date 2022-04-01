package gov.anzong.androidnga.fragment

import android.content.Intent
import android.os.Bundle
import android.view.*
import sp.phone.ui.adapter.TopicListAdapter
import sp.phone.view.RecyclerViewEx
import sp.phone.common.TopicHistoryManager
import androidx.recyclerview.widget.LinearLayoutManager
import nosc.ui.view.DividerItemDecorationEx
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import sp.phone.mvp.model.entity.TopicListInfo
import sp.phone.param.ArticleListParam
import com.alibaba.fastjson.JSON
import gov.anzong.androidnga.R
import nosc.utils.ContextUtils
import nosc.utils.uxUtils.showConfirmDialog
import sp.phone.param.ParamKey
import sp.phone.common.PhoneConfiguration
import sp.phone.mvp.model.entity.ThreadPageInfo

/**
 * Created by Justwen on 2018/1/17.
 */
class TopicHistoryFragment : BaseFragment(), View.OnClickListener {
    private var mTopicListAdapter: TopicListAdapter? = null
    private var mListView: RecyclerViewEx? = null
    private var mTopicHistoryManager: TopicHistoryManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        mTopicHistoryManager = TopicHistoryManager.getInstance()
        setTitle(R.string.label_activity_topic_history)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mTopicListAdapter = TopicListAdapter(requireContext())
        mTopicListAdapter!!.setOnClickListener(this)
        mListView = view.findViewById(R.id.list)
        mListView?.layoutManager = LinearLayoutManager(context)
        mListView?.setEmptyView(view.findViewById(R.id.empty_view))
        mListView?.adapter = mTopicListAdapter
        mListView?.addItemDecoration(
            DividerItemDecorationEx(
                view.context, ContextUtils.getDimension(
                    R.dimen.topic_list_item_padding
                ), DividerItemDecoration.VERTICAL
            )
        )
        super.onViewCreated(view, savedInstanceState)
        val touchHelper =
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition
                    if (position >= 0) {
                        mTopicHistoryManager!!.removeTopicHistory(position)
                        mTopicListAdapter?.removeItemAt(position)
                    }
                }
            })
        //将recycleView和ItemTouchHelper绑定
        touchHelper.attachToRecyclerView(mListView)
        setData(mTopicHistoryManager!!.topicHistoryList)
    }

    private fun setData(topicLIst: List<ThreadPageInfo>) {
        val listInfo = TopicListInfo()
        listInfo.threadPageList = topicLIst
        mTopicListAdapter?.setData(listInfo.threadPageList)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.settings_black_list_option_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.menu_delete_all) {
            showConfirmDialog(requireActivity(), "确认删除所有浏览历史吗") {
                mTopicHistoryManager?.removeAllTopicHistory()
                mTopicListAdapter?.clear()
            }
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onClick(view: View) {
        val info = view.tag as ThreadPageInfo
        val param = ArticleListParam()
        param.tid = info.tid
        param.page = info.page
        param.title = info.subject
        param.topicInfo = JSON.toJSONString(info)
        val intent = Intent()
        val bundle = Bundle()
        bundle.putParcelable(ParamKey.KEY_PARAM, param)
        intent.putExtras(bundle)
        intent.setClass(requireContext(), PhoneConfiguration.getInstance().articleActivityClass)
        startActivity(intent)
    }
}