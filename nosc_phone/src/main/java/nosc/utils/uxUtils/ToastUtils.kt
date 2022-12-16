package nosc.utils.uxUtils;

import android.widget.Toast;

import androidx.annotation.StringRes;

import es.dmoral.toasty.Toasty;
import nosc.utils.ContextUtils;
import nosc.utils.ThreadUtils;

/**
 * Created by Justwen on 2018/8/11.
 */
public class ToastUtils {

    public static void flat(String text) {
        ThreadUtils.runOnMainThread(() -> Toast.makeText(ContextUtils.getContext(), text, Toast.LENGTH_SHORT).show());
    }

    public static void success(@StringRes int id) {
        success(ContextUtils.getString(id));
    }

    public static void error(@StringRes int id) {
        error(ContextUtils.getString(id));
    }

    public static void warn(@StringRes int id) {
        warn(ContextUtils.getString(id));
    }

    public static void info(@StringRes int id) {
        info(ContextUtils.getString(id));
    }

    public static void success(String text) {
        if (!ThreadUtils.isMainThread()) {
            ThreadUtils.runOnMainThread(() -> ToastUtils.success(text));
        } else {
            Toasty.success(ContextUtils.getContext(), text).show();
        }
    }

    public static void error(String text) {
        Toasty.error(ContextUtils.getContext(), text).show();
    }

    public static void info(String text) {
        Toasty.info(ContextUtils.getContext(), text).show();
    }

    public static void warn(String text) {
        Toasty.warning(ContextUtils.getContext(), text).show();
    }

}
