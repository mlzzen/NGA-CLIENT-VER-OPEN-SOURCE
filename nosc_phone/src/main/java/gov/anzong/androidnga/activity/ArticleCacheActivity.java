package gov.anzong.androidnga.activity;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import android.view.View;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gov.anzong.androidnga.R;
import nosc.utils.ContextUtils;
import nosc.utils.uxUtils.ToastUtils;
import nosc.ui.view.PageSelector;
import sp.phone.common.PhoneConfiguration;
import sp.phone.param.ArticleListParam;
import sp.phone.param.ParamKey;
import sp.phone.ui.adapter.ArticlePagerAdapter;

/**
 * @author yangyihang
 */
public class ArticleCacheActivity extends BaseActivity {

    private final List<String> mCachePageList = new ArrayList<>();

    private ArticleListParam mRequestParam;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setToolbarEnabled(true);
        mRequestParam = getIntent().getParcelableExtra(ParamKey.KEY_PARAM);
        setTitle(mRequestParam.title);
        super.onCreate(savedInstanceState);
        loadCachePageList(String.valueOf(mRequestParam.tid));
        initViews();
    }

    private void initViews() {
        if (mCachePageList.isEmpty()) {
            ToastUtils.error("加载失败!");
            return;
        }

        if (PhoneConfiguration.INSTANCE.isShowBottomTab()) {
            setContentView(R.layout.fragment_article_tab_bottom);
        } else {
            setContentView(R.layout.fragment_article_tab);
        }

        ArticlePagerAdapter mPagerAdapter = new ArticlePagerAdapter(getSupportFragmentManager(), mRequestParam);
        mPagerAdapter.setPageIndexList(mCachePageList);

        findViewById(R.id.fab_menu).setVisibility(View.GONE);
        ViewPager viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(mPagerAdapter);

        PageSelector tabLayout = findViewById(R.id.tabs);
        tabLayout.bindViewPager(viewPager);
    }

    private void loadCachePageList(String tid) {
        mCachePageList.clear();
        String path = ContextUtils.getExternalDir("articleCache") + tid;
        File[] cacheFiles = new File(path).listFiles();
        if (cacheFiles != null) {
            for (File cacheFile : cacheFiles) {
                if (!cacheFile.getName().contains(tid)) {
                    String page = cacheFile.getName();
                    mCachePageList.add(page.replace(".json", ""));
                }
            }
            Collections.sort(mCachePageList);
        }

    }


}
