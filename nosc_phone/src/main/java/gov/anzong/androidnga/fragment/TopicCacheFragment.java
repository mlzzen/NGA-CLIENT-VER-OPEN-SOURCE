package gov.anzong.androidnga.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import gov.anzong.androidnga.R;
import gov.anzong.androidnga.activity.ArticleCacheActivity;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import nosc.utils.uxUtils.ToastUtils;
import sp.phone.mvp.model.entity.ThreadPageInfo;
import sp.phone.mvp.model.entity.TopicListInfo;
import sp.phone.param.ArticleListParam;
import sp.phone.param.ParamKey;
import sp.phone.util.StringUtils;

/**
 * @author Justwen
 */
public class TopicCacheFragment extends TopicFragment {

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ToastUtils.success("长按可删除缓存的帖子");
        mAdapter.setOnItemLongClick((it) ->{
            final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage(this.getString(R.string.delete_favo_confirm_text))
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        viewModel.removeCacheTopic(it);
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create()
                    .show();
            return Unit.INSTANCE;
        });
        viewModel.getRemovedTopic().observe(getViewLifecycleOwner(), this::removeTopic);
    }

    @Override
    public void setData(TopicListInfo result) {
        super.setData(result);
        mAdapter.setNextPageEnabled(false);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_cache_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_cache_export:
                viewModel.exportCacheTopic(this);
                break;
            case R.id.menu_cache_import:
                viewModel.showFileChooser(this);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @NonNull
    @Override
    protected Function1<ThreadPageInfo, Unit> getOnItemClick() {
        return (info)->{
            ArticleListParam param = new ArticleListParam();
            param.tid = info.getTid();
            param.loadCache = true;
            param.title = StringUtils.unEscapeHtml(info.getSubject());
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putParcelable(ParamKey.KEY_PARAM, param);
            intent.putExtras(bundle);
            intent.setClass(getContext(), ArticleCacheActivity.class);
            startActivity(intent);
            return Unit.INSTANCE;
        };
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_IMPORT_CACHE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                return;
            }
            viewModel.importCacheTopic(data.getData());
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}
