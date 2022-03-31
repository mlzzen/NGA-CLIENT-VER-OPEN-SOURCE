package sp.phone.mvp.contract;

import android.os.Bundle;

import nosc.api.bean.ThreadData;
import nosc.api.bean.ThreadRowInfo;
import sp.phone.param.ArticleListParam;
import nosc.api.callbacks.OnHttpCallBack;

/**
 *
 * @author Justwen
 * @date 2017/11/22
 */

public interface ArticleListContract {

    interface Presenter {

        void loadPage(ArticleListParam param);

        void banThisSB(ThreadRowInfo row);

        void postComment(ArticleListParam param, ThreadRowInfo row);

        void postSupportTask(int tid, int pid);

        void postOpposeTask(int tid, int pid);

        void cachePage();
    }

    interface View {

        void setRefreshing(boolean refreshing);

        void hideLoadingView();

        void setData(ThreadData data);

        void showPostCommentDialog(String prefix, Bundle bundle);

    }

    interface Model {

        void loadPage(ArticleListParam param, OnHttpCallBack<ThreadData> callBack);

        void cachePage(ArticleListParam param, String rawData);

        void loadCachePage(ArticleListParam param, OnHttpCallBack<ThreadData> callBack);
    }
}
