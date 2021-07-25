package sp.phone.util;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import gov.anzong.androidnga.base.util.ContextUtils;;
;

/**
 * Created by elrond on 2016/12/22.
 */

public class NetUtil {

    private static NetUtil mInstance;

    public static NetUtil getInstance() {
        if (mInstance == null) {
            mInstance = new NetUtil(ContextUtils.getApplication());
        }
        return mInstance;
    }

    private final Application context;

    private NetUtil(Application context) {
        this.context = context;
    }

    public boolean isInWifi() {
        ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo.State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        return wifi == NetworkInfo.State.CONNECTED;
    }
}
