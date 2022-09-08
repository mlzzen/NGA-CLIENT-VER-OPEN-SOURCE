package sp.phone.mvp.presenter;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ViewModel;

import sp.phone.mvp.model.BaseModel;
import gov.anzong.androidnga.fragment.BaseMvpFragment;

/**
 * @author Justwen
 * @date 2017/11/25
 */

@Deprecated
public abstract class BasePresenter<T extends BaseMvpFragment, E extends BaseModel> extends ViewModel
        implements LifecycleObserver {

    protected T mBaseView;

    protected E mBaseModel;

    @Deprecated
    public BasePresenter() {
        mBaseModel = onCreateModel();
    }

    protected void onCreate() {
        if (mBaseModel == null) {
            mBaseModel = onCreateModel();
        }
        if (mBaseModel != null && mBaseView != null) {
            mBaseModel.setLifecycleProvider(mBaseView.getLifecycleProvider());
        }

    }

    protected void onResume() {

    }

    public void attachView(T view) {
        mBaseView = view;
        if (mBaseModel != null) {
            mBaseModel.setLifecycleProvider(view.getLifecycleProvider());
        }
    }

    protected boolean isAttached() {
        return mBaseView != null;
    }

    public void onViewCreated() {
    }


    protected abstract E onCreateModel();
}
