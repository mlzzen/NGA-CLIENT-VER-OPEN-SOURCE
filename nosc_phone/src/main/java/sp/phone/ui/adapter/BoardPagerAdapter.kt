package sp.phone.ui.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import sp.phone.mvp.model.entity.BoardCategory
import androidx.viewpager2.adapter.FragmentStateAdapter
import gov.anzong.androidnga.fragment.BoardCategoryFragment

/**
 * 版块分页Adapter
 */
class BoardPagerAdapter(fm: Fragment, private val mBoardCategories: List<BoardCategory>) : FragmentStateAdapter(fm) {
    fun getPageTitle(position: Int): CharSequence {
        return mBoardCategories[position].name
    }

    override fun createFragment(position: Int): Fragment {
        val fragment = BoardCategoryFragment()
        val args = Bundle()
        args.putParcelable("category", mBoardCategories[position])
        fragment.arguments = args
        return fragment
    }

    override fun getItemCount(): Int {
        return mBoardCategories.size
    }



}