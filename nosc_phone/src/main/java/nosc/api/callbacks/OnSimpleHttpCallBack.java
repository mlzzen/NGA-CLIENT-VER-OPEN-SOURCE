package nosc.api.callbacks;

/**
 * Created by Justwen on 2017/10/10.
 */

public interface OnSimpleHttpCallBack<T> {

    void onResult(T data);
}