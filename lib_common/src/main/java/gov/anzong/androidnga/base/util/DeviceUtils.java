package gov.anzong.androidnga.base.util;

import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.util.DisplayMetrics;

/**
 * Created by Justwen on 2017/7/16.
 */

public class DeviceUtils {



    public static boolean isGreaterEqual_7_0() {
        return android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }


    public static boolean isGreaterEqual_8_0() {
        return android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    public static boolean isWifiConnected(Context context) {
        try {
            ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (conMan != null) {
                NetworkCapabilities capabilities = conMan.getNetworkCapabilities(conMan.getActiveNetwork());
                if (capabilities != null) {
                    return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isFullScreenDevice() {
        DisplayMetrics dm = ContextUtils.getResources().getDisplayMetrics();
        float width = dm.widthPixels;
        float height = dm.heightPixels;
        return height / width >= 1.97f;
    }
}
