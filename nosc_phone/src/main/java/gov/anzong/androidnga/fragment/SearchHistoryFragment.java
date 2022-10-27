package gov.anzong.androidnga.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.List;

import gov.anzong.androidnga.R;
import sp.phone.ui.adapter.SearchHistoryAdapter;
import sp.phone.util.ActivityUtils;

/**
 * Created by Justwen on 2018/10/12.
 */
public abstract class SearchHistoryFragment extends BaseRxFragment {

    protected List<String> mKeyList;
    private SharedPreferences mPreferences;
    protected SearchHistoryAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_history, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mKeyList = JSON.parseArray(mPreferences.getString(getPreferenceKey(), ""), String.class);
        if (mKeyList == null) {
            mKeyList = new ArrayList<>();
        }
        RecyclerView recyclerView = view.findViewById(android.R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new SearchHistoryAdapter(getContext());
        recyclerView.setAdapter(mAdapter);
        mAdapter.setData(mKeyList);
        mAdapter.setOnClickListener(v -> {
            Object tag = v.getTag();
            if (tag instanceof String) {
                query((String) tag);
            } else {
                int position = Integer.parseInt(tag.toString());
                if (position < mKeyList.size()) {
                    mKeyList.remove(position);
                    mAdapter.notifyDataSetChanged();
                    saveHistory();
                }
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }

    public void query(String query) {
        if (TextUtils.isEmpty(query) || mKeyList == null) {
            return;
        } else if (!mKeyList.contains(query)) {
            addHistory(query);
        }
        ActivityUtils.getInstance().noticeSaying(getContext());
    }

    protected void addHistory(String query) {
        mKeyList.add(0, query);
        mAdapter.notifyItemInserted(0);
        saveHistory();
    }

    protected void saveHistory() {
        mPreferences.edit()
                .putString(getPreferenceKey(), JSON.toJSONString(mKeyList))
                .apply();
    }

    protected abstract String getPreferenceKey();
}