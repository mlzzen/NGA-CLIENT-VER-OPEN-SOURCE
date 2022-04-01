package nosc.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import sp.phone.rxjava.BaseSubscriber;

/**
 * Created by Justwen on 2017/6/24.
 */
@SuppressLint("CheckResult")
public class PermissionUtils {

    private PermissionUtils() { }

    public static boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static void request(AppCompatActivity activity, @Nullable Observer<Boolean> consumer, String permission) {
        new RxPermissions(activity).request(permission).subscribe(consumer == null ? new BaseSubscriber<Boolean>() {
        } : consumer);
    }

    public static void request(Fragment fragment, @Nullable Observer<Boolean> consumer, String permission) {
        new RxPermissions(fragment).request(permission).subscribe(consumer == null ? new BaseSubscriber<Boolean>() {
        } : consumer);
    }

    public static void requestAsync(Fragment fragment, @Nullable Observer<Boolean> consumer, String permission) {
        new RxPermissions(fragment).request(permission)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(createIfNull(consumer));
    }

    private static <T> Observer<T> createIfNull(Observer<T> consumer) {
        if (consumer == null) {
            return new BaseSubscriber<T>() {
            };
        }
        return consumer;
    }
}
