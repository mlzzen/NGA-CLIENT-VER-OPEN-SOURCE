package sp.phone.task;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import nosc.api.callbacks.OnSimpleHttpCallBack;
import nosc.api.retrofit.RetrofitHelper;
import nosc.api.retrofit.Api;
import sp.phone.rxjava.BaseSubscriber;

/**
 * 赞或者踩
 * Created by elrond on 2017/9/1.
 */

public class LikeTask {

    public static final int SUPPORT = 1;

    public static final int OPPOSE = -1;

    private Api mService;

    private Map<String, String> mParamMap;

    public LikeTask() {
        mService = RetrofitHelper.getInstance().getApi();
        mParamMap = new HashMap<>();
        mParamMap.put("__lib", "topic_recommend");
        mParamMap.put("__act", "add");
        mParamMap.put("raw", "3");
        mParamMap.put("pid", "0");
        mParamMap.put("__output", "8");
    }

    public void execute(int tid, int pid, int like, OnSimpleHttpCallBack<String> callBack) {
        Map<String, String> map = new HashMap<>(mParamMap);
        map.put("value", String.valueOf(like));
        map.put("tid", String.valueOf(tid));
        map.put("pid", String.valueOf(pid));
        mService.post(map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseSubscriber<String>() {
                    @Override
                    public void onNext(String s) {
                        try {
                            // 请求成功
                            callBack.onResult(s);
                        } catch (Exception e) {
                            // 失败返回空
                            callBack.onResult("");
                        }
                    }
                });

    }
}
