package sp.phone.ui.adapter;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;


import java.util.List;

import sp.phone.mvp.model.entity.BoardCategory;
import gov.anzong.androidnga.fragment.BoardCategoryFragment;


/**
 * 版块分页Adapter
 */
public class BoardPagerAdapter extends FragmentStateAdapter {

    private List<BoardCategory> mBoardCategories;

    public BoardPagerAdapter(Fragment fm, List<BoardCategory> categories) {
        super(fm);
        mBoardCategories = categories;
    }

    public CharSequence getPageTitle(int position) {
        return mBoardCategories.get(position).getName();
    }


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        BoardCategoryFragment fragment = new BoardCategoryFragment();
        Bundle args = new Bundle();
        args.putParcelable("category", mBoardCategories.get(position));
        fragment.setArguments(args);
        return fragment;    }

    @Override
    public int getItemCount() {
        return mBoardCategories.size();
    }
}
