package nosc.api.callbacks;


public interface OnHttpCallBack<T> {

    default void onError(String text) {

    }

    default void onSuccess(T data) {

    }
}
