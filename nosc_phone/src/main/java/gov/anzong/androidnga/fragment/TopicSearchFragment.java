package gov.anzong.androidnga.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSON;

import java.util.Collections;

import gov.anzong.androidnga.R;
import gov.anzong.androidnga.activity.BaseActivity;
import gov.anzong.androidnga.arouter.ARouterConstants;
import gov.anzong.androidnga.base.util.ContextUtils;
import gov.anzong.androidnga.base.widget.DividerItemDecorationEx;
import nosc.api.constants.ApiConstants;
import sp.phone.common.PhoneConfiguration;
import sp.phone.common.TopicHistoryManager;
import sp.phone.mvp.model.entity.ThreadPageInfo;
import sp.phone.mvp.model.entity.TopicListInfo;
import nosc.viewmodel.TopicListViewModel;
import sp.phone.param.ArticleListParam;
import sp.phone.param.ParamKey;
import sp.phone.param.TopicListParam;
import sp.phone.ui.adapter.BaseAppendableAdapter;
import sp.phone.ui.adapter.ReplyListAdapter;
import sp.phone.ui.adapter.TopicListAdapter;
import sp.phone.util.ARouterUtils;
import sp.phone.util.ActivityUtils;
import sp.phone.util.StringUtils;
import sp.phone.view.RecyclerViewEx;

public class TopicSearchFragment extends BaseFragment implements View.OnClickListener {

    private static final String TAG = TopicSearchFragment.class.getSimpleName();

    public static final int REQUEST_IMPORT_CACHE = 0;

    protected TopicListParam mRequestParam;

    protected BaseAppendableAdapter mAdapter;

    protected TopicListInfo mTopicListInfo;

    public SwipeRefreshLayout mSwipeRefreshLayout;

    public RecyclerViewEx mListView;

    public View mLoadingView;

    protected TopicListViewModel viewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mRequestParam = getArguments().getParcelable(ParamKey.KEY_PARAM);
        super.onCreate(savedInstanceState);
        setTitle();
        viewModel = onCreateViewModel();
        getLifecycle().addObserver(viewModel);
    }

    protected TopicListViewModel onCreateViewModel() {
        ViewModelProvider viewModelProvider = new ViewModelProvider(this);
        TopicListViewModel topicListViewModel = viewModelProvider.get(TopicListViewModel.class);
        topicListViewModel.setRequestParam(mRequestParam);
        return topicListViewModel;
    }

    protected void setTitle() {
        if (!StringUtils.isEmpty(mRequestParam.key)) {
            if (mRequestParam.content == 1) {
                if (!StringUtils.isEmpty(mRequestParam.fidGroup)) {
                    setTitle("搜索全站(包含正文):" + mRequestParam.key);
                } else {
                    setTitle("搜索(包含正文):" + mRequestParam.key);
                }
            } else {
                if (!StringUtils.isEmpty(mRequestParam.fidGroup)) {
                    setTitle("搜索全站:" + mRequestParam.key);
                } else {
                    setTitle("搜索:" + mRequestParam.key);
                }
            }
        } else if (!StringUtils.isEmpty(mRequestParam.author)) {
            if (mRequestParam.searchPost > 0) {
                final String title = "搜索" + mRequestParam.author + "的回复";
                setTitle(title);
            } else {
                final String title = "搜索" + mRequestParam.author + "的主题";
                setTitle(title);
            }
        } else if (mRequestParam.recommend == 1) {
            setTitle(mRequestParam.title + " - 精华区");
        } else if (mRequestParam.twentyfour == 1) {
            setTitle(mRequestParam.title + " - 24小时热帖");
        } else if (!TextUtils.isEmpty(mRequestParam.title)) {
            setTitle(mRequestParam.title);
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int layoutId = R.layout.fragment_topic_list;
        return inflater.inflate(layoutId, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mSwipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        mListView = view.findViewById(R.id.list);
        mLoadingView = view.findViewById(R.id.loading_view);
        ((BaseActivity) getActivity()).setupToolbar();

        if (mRequestParam.searchPost > 0) {
            mAdapter = new ReplyListAdapter(requireContext());
            mListView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        } else {

            mAdapter = new TopicListAdapter(requireContext());
        }

        mAdapter.setOnClickListener(this);

        mListView.setLayoutManager(new LinearLayoutManager(getContext()));
        mListView.setOnNextPageLoadListener(() -> {
            if (!isRefreshing()) {
                viewModel.loadNextPage(mAdapter.getNextPage(), mRequestParam);
            }
        });
        mListView.setEmptyView(view.findViewById(R.id.empty_view));
        mListView.setAdapter(mAdapter);
        if (PhoneConfiguration.getInstance().useSolidColorBackground()) {
            int padding = PhoneConfiguration.getInstance().useSolidColorBackground() ? ContextUtils.getDimension(R.dimen.topic_list_item_padding) : 0;
            mListView.addItemDecoration(new DividerItemDecorationEx(view.getContext(), padding, DividerItemDecoration.VERTICAL));
        }

        mSwipeRefreshLayout.setVisibility(View.GONE);
        mSwipeRefreshLayout.setOnRefreshListener(() -> viewModel.loadPage(1, mRequestParam));

        TextView sayingView = (TextView) mLoadingView.findViewById(R.id.saying);
        sayingView.setText(ActivityUtils.getSaying());

        super.onViewCreated(view, savedInstanceState);

        viewModel.getFirstTopicList().observe(getViewLifecycleOwner(), topicListInfo -> {
            scrollTo(0);
            if (topicListInfo != null) {
                setData(topicListInfo);
            }
        });

        viewModel.getNextTopicList().observe(getViewLifecycleOwner(), this::appendData);

        viewModel.getErrorMsg().observe(getViewLifecycleOwner(), res -> {
            showToast(res);
            setNextPageEnabled(false);
        });

        viewModel.isRefreshing().observe(getViewLifecycleOwner(), aBoolean -> {
            setRefreshing(aBoolean);
            if (!aBoolean) {
                hideLoadingView();
            }
        });
    }



    public void scrollTo(int position) {
        mListView.scrollToPosition(position);
    }

    public void setNextPageEnabled(boolean enabled) {
        mAdapter.setNextPageEnabled(enabled);
    }

    public void removeTopic(ThreadPageInfo pageInfo) {

    }

    public void hideLoadingView() {
        if (mLoadingView.getVisibility() == View.VISIBLE) {
            mLoadingView.setVisibility(View.GONE);
            mSwipeRefreshLayout.setVisibility(View.VISIBLE);
        }
    }

    public void setRefreshing(boolean refreshing) {
        if (mSwipeRefreshLayout.getVisibility() == View.VISIBLE) {
            mSwipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    public boolean isRefreshing() {
        return mSwipeRefreshLayout.isShown() ? mSwipeRefreshLayout.isRefreshing() : mLoadingView.isShown();
    }

    public void setData(TopicListInfo result) {
        mTopicListInfo = result;
        mAdapter.setData(result.getThreadPageList());
    }

    public void appendData(TopicListInfo result) {
        mTopicListInfo = result;
        mAdapter.appendData(result.getThreadPageList());
    }




    @Override
    public void onClick(View view) {
        ThreadPageInfo info = (ThreadPageInfo) view.getTag();
        handleClickEvent(view.getContext(), info, mRequestParam);
    }

    public static void handleClickEvent(Context context, ThreadPageInfo info, TopicListParam requestParam) {

        if (info.isMirrorBoard()) {
            ARouterUtils.build(ARouterConstants.ACTIVITY_TOPIC_LIST)
                    .withInt(ParamKey.KEY_FID, info.getFid())
                    .withString(ParamKey.KEY_TITLE, info.getSubject())
                    .navigation(context);
        } else if ((info.getType() & ApiConstants.MASK_TYPE_ASSEMBLE) == ApiConstants.MASK_TYPE_ASSEMBLE) {
            TopicListParam param = new TopicListParam();
            param.title = info.getSubject();
            param.stid = info.getTid();
            ARouter.getInstance().build(ARouterConstants.ACTIVITY_TOPIC_LIST)
                    .withParcelable(ParamKey.KEY_PARAM, param)
                    .navigation();

        } else {

            ArticleListParam param = new ArticleListParam();
            param.tid = info.getTid();
            param.page = info.getPage();
            param.title = StringUtils.unEscapeHtml(info.getSubject());
            if (requestParam.searchPost != 0) {
                param.pid = info.getPid();
                param.authorId = info.getAuthorId();
                param.searchPost = requestParam.searchPost;
            }
            param.topicInfo = JSON.toJSONString(info);

            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putParcelable(ParamKey.KEY_PARAM, param);
            intent.putExtras(bundle);
            intent.setClass(context, PhoneConfiguration.getInstance().articleActivityClass);
            context. startActivity(intent);
            TopicHistoryManager.getInstance().addTopicHistory(info);
        }
    }


}
