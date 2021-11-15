package sp.phone.ui.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import sp.phone.param.ArticleListParam
import androidx.fragment.app.FragmentStatePagerAdapter
import sp.phone.ui.fragment.ArticleListFragment
import sp.phone.param.ParamKey

/**
 * 帖子详情分页Adapter
 */
class ArticlePagerAdapter(fm: FragmentManager, private val mRequestParam: ArticleListParam) :
    FragmentStatePagerAdapter(fm,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private var mCount = 1
    private var mPageIndexList: List<String>? = null
    override fun getItem(position: Int): Fragment {
        val fragment: Fragment = ArticleListFragment()
        val bundle = Bundle()
        bundle.putParcelable(ParamKey.KEY_PARAM, getRequestParam(position))
        fragment.arguments = bundle
        return fragment
    }

    private fun getRequestParam(position: Int): ArticleListParam {
        val param = mRequestParam.clone() as ArticleListParam
        if (mPageIndexList != null) {
            param.page = mPageIndexList!![position].toInt()
        } else {
            param.page = position + 1
        }
        return param
    }

    override fun getCount(): Int {
        return mCount
    }

    fun setCount(count: Int) {
        if (mCount != count) {
            mCount = count
            notifyDataSetChanged()
        }
    }

    fun setPageIndexList(pageIndexList: List<String>) {
        mPageIndexList = pageIndexList
        count = pageIndexList.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return if (mPageIndexList == null) (position + 1).toString() else mPageIndexList!![position]
    }
}