package sp.phone.rxjava;

import android.view.View;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class RxUtils {

    private RxUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static void clicks(View view, View.OnClickListener listener) {
        view.setOnClickListener(new View.OnClickListener() {
            long lastClickTime = 0;
            @Override
            public void onClick(View v) {
                long now = System.currentTimeMillis();
                if(now - lastClickTime > 800){
                    lastClickTime = now;
                    listener.onClick(v);
                }
            }
        });
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
