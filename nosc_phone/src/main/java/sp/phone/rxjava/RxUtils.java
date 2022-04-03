package sp.phone.rxjava;

import android.view.View;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import nosc.utils.uxUtils.ViewUtilsKt;

public class RxUtils {

    private RxUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static void clicks(View view, View.OnClickListener listener) {
        view.setOnClickListener(ViewUtilsKt.withClickCd(listener,800));
    }

    public static void post(Object obj) {
        RxBus.getInstance().post(obj);
    }

    public static void postDelay(int delay, BaseSubscriber<Long> subscriber) {
        Observable.timer(delay, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }
}
