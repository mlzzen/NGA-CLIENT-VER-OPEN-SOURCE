package gov.anzong.androidnga.gallery;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.reactivestreams.Subscription;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

import coil.Coil;
import coil.request.ImageRequest;
import gov.anzong.androidnga.NgaClientApp;
import gov.anzong.androidnga.R;
import nosc.utils.uxUtils.ToastUtils;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import nosc.utils.ContextUtils;;
import nosc.api.callbacks.OnSimpleHttpCallBack;
import sp.phone.rxjava.BaseSubscriber;

public class SaveImageTask {

    private Context mContext;

    private int mDownloadCount;

    private static final String PATH_IMAGES = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures/nga_open_source/";

    private Subscription mSubscription;

    public SaveImageTask() {
        mContext = ContextUtils.getContext();
    }

    public static class DownloadResult {

        File file;

        String url;

        public DownloadResult(File file, String url) {
            this.file = file;
            this.url = url;
        }
    }

    public void execute(OnSimpleHttpCallBack<DownloadResult> callBack, String... urls) {

        if (isRunning()) {
            ToastUtils.info("图片正在下载，防止风怒！！");
            return;
        }

        mDownloadCount = 0;
        Observable.fromArray(urls)
                .observeOn(Schedulers.io())
                .map(url -> {

                    File file = File.createTempFile(System.currentTimeMillis()+"","");
                    IOUtils.copy(
                            new URL(url).openConnection().getInputStream(),
                            new FileOutputStream(file)
                    );
                    return new DownloadResult(file, url);
                })
                .map(result -> {
                    String url = result.url;
                    String suffix = url.substring(url.lastIndexOf('.'));
                    String path = PATH_IMAGES + System.currentTimeMillis() + suffix;
                    File target = new File(path);
                    FileUtils.copyFile(result.file, target);
                    return new DownloadResult(target, url);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseSubscriber<DownloadResult>() {
                    @Override
                    public void onNext(DownloadResult result) {
                        Uri uri = Uri.fromFile(result.file);
                        ContextUtils.getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
                        mDownloadCount++;
                        if (mDownloadCount == urls.length) {
                            if (urls.length > 1) {
                                ToastUtils.info("所有图片已保存");
                            } else {
                                ToastUtils.info(mContext.getString(R.string.file_saved) + result.file.getAbsolutePath());
                            }
                        }
                        callBack.onResult(result);
                    }

                    @Override
                    public void onComplete() {
                        mSubscription = null;
                    }

                    @Override
                    public void onSubscribe(Subscription subscription) {
                        super.onSubscribe(subscription);
                        mSubscription = subscription;
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        mSubscription = null;
                        ToastUtils.error("下载失败");
                    }

                });
    }

    private boolean isRunning() {
        return mSubscription != null;
    }


}
