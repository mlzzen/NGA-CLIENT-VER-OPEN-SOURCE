package sp.phone.task;

import gov.anzong.androidnga.Utils;

import nosc.utils.uxUtils.ToastUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import nosc.api.retrofit.RetrofitHelper;
import nosc.api.retrofit.Api;
import sp.phone.rxjava.BaseSubscriber;
import sp.phone.util.StringUtils;

public class BookmarkTask {

    private static final String url = Utils.getNGAHost() + "nuke.php?__lib=topic_favor&lite=js&noprefix&__act=topic_favor&action=add&tid=";

    public static void execute(int tid) {
        Api service = RetrofitHelper.getInstance().getApi();
        service.post(url + tid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseSubscriber<String>() {
                    @Override
                    public void onNext(String result) {
                        String msg = StringUtils.getStringBetween(result, 0, "{\"0\":\"", "\"},\"time\"").result;
                        if (!StringUtils.isEmpty(msg)) {
                            ToastUtils.info(msg.trim());
                        }
                    }
                });
    }

    public static void execute(String tid, String pid) {
        Api service = RetrofitHelper.getInstance().getApi();
        String postUrl = url + tid + "&pid=" + pid;
        service.post(postUrl)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseSubscriber<String>() {
                    @Override
                    public void onNext(String result) {
                        String msg = StringUtils.getStringBetween(result, 0, "{\"0\":\"", "\"},\"time\"").result;
                        if (!StringUtils.isEmpty(msg)) {
                            ToastUtils.info(msg.trim());
                        }
                    }
                });
    }

}
