package sp.phone.mvp.contract;

import android.os.Bundle;

import kotlin.Pair;
import kotlinx.coroutines.flow.Flow;
import nosc.api.ApiResult;
import nosc.api.bean.ThreadData;
import nosc.api.bean.ThreadRowInfo;
import nosc.api.callbacks.OnSimpleHttpCallBack;
import sp.phone.param.ArticleListParam;
import nosc.api.callbacks.OnHttpCallBack;

/**
 *
 * @author Justwen
 * @date 2017/11/22
 */

public interface ArticleListContract {

    interface View {

        void setRefreshing(boolean refreshing);

        void hideLoadingView();

        void setData(ThreadData data);

        void showPostCommentDialog(String prefix, Bundle bundle);

    }

    interface Model {

        Flow<ApiResult<ThreadData>> loadPage(ArticleListParam param);

        void cachePage(ArticleListParam param, String rawData);

    }
}
