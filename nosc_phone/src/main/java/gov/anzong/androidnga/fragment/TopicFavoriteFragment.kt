package gov.anzong.androidnga.fragment

import android.app.AlertDialog
import android.view.View.OnLongClickListener
import gov.anzong.androidnga.R
import android.os.Bundle
import nosc.utils.uxUtils.ToastUtils
import sp.phone.mvp.model.entity.ThreadPageInfo
import android.view.View

/**
 * Created by Justwen on 2017/11/19.
 */
class TopicFavoriteFragment : TopicFragment() {
    override fun setTitle() {
        setTitle(R.string.bookmark_title)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ToastUtils.info("长按可删除收藏的帖子")
        mAdapter!!.onItemLongClick = {
            val builder = AlertDialog.Builder(context)
            builder.setMessage(this.getString(R.string.delete_favo_confirm_text))
                .setPositiveButton(android.R.string.ok) { dialog, which ->
                    val info = it
                    viewModel!!.removeTopic(info)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show()
        }
        viewModel!!.removedTopic.observe(viewLifecycleOwner) { pageInfo: ThreadPageInfo? ->
            removeTopic(pageInfo)
        }
        mAdapter?.onNextPage = {
            viewModel?.loadNextPage((mAdapter?.nextPageIndex()?:0) + 1, mRequestParam)
        }
        mAdapter?.onRefresh = {
            viewModel?.loadPage(1, mRequestParam)
        }
    }

    override fun removeTopic(pageInfo: ThreadPageInfo?) {
        mAdapter?.removeItem(pageInfo?:return)
    }
}