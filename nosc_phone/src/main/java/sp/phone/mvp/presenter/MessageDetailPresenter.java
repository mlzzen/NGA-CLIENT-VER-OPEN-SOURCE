package sp.phone.mvp.presenter;

import android.text.TextUtils;

import gov.anzong.androidnga.R;
import nosc.api.bean.MessageDetailInfo;
import gov.anzong.androidnga.fragment.MessageDetailFragment;
import nosc.api.callbacks.OnHttpCallBack;
import nosc.utils.uxUtils.ToastUtils;
import sp.phone.mvp.model.MessageDetailModel;
import sp.phone.mvp.contract.MessageDetailContract;

/**
 * Created by Justwen on 2017/10/11.
 */

@Deprecated
public class MessageDetailPresenter extends BasePresenter<MessageDetailFragment,MessageDetailModel> implements MessageDetailContract.IMessagePresenter {


    private OnHttpCallBack<MessageDetailInfo> mCallBack = new OnHttpCallBack<MessageDetailInfo>() {
        @Override
        public void onError(String text) {
            if (!isAttached()) {
                return;
            }
            mBaseView.setRefreshing(false);
            mBaseView.hideLoadingView();
            if (TextUtils.isEmpty(text)) {
                ToastUtils.error(R.string.network_error);
            } else {
                ToastUtils.error(text);
            }
        }

        @Override
        public void onSuccess(MessageDetailInfo data) {
            if (!isAttached()) {
                return;
            }
            mBaseView.setRefreshing(false);
            mBaseView.hideLoadingView();
            mBaseView.setData(data);
        }
    };

    @Override
    protected MessageDetailModel onCreateModel() {
        return new MessageDetailModel();
    }

    @Override
    public void loadPage(int page, int mid) {
        mBaseView.setRefreshing(true);
        mBaseModel.loadPage(page, mid, mCallBack);

    }
}