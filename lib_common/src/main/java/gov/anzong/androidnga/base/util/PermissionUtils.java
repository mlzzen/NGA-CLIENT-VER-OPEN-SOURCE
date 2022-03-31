package gov.anzong.androidnga.base.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import gov.anzong.androidnga.rxjava.DefaultSubsriber;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

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
        new RxPermissions(activity).request(permission).subscribe(consumer == null ? new DefaultSubsriber<>() : consumer);
    }

    public static void request(Fragment fragment, @Nullable Observer<Boolean> consumer, String permission) {
        new RxPermissions(fragment).request(permission).subscribe(consumer == null ? new DefaultSubsriber<>() : consumer);
    }

    public static void requestAsync(Fragment fragment, @Nullable Observer<Boolean> consumer, String permission) {
        new RxPermissions(fragment).request(permission)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(createIfNull(consumer));
    }

    private static <T> Observer<T> createIfNull(Observer<T> consumer) {
        if (consumer == null) {
            return new DefaultSubsriber<>();
        }
        return consumer;
    }
}
