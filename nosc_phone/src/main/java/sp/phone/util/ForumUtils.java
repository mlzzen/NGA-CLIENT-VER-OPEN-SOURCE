package sp.phone.util;

import android.content.Context;
import android.content.SharedPreferences;

import gov.anzong.androidnga.R;
import nosc.utils.ContextUtils;;
import nosc.utils.PreferenceKey;

/**
 * Created by Justwen on 2018/7/2.
 */
public class ForumUtils {

    public static String getApiDomain() {
        Context context = ContextUtils.getContext();
        SharedPreferences sp = context.getSharedPreferences(PreferenceKey.PERFERENCE, Context.MODE_PRIVATE);
        int index = Integer.parseInt(sp.getString(PreferenceKey.KEY_NGA_DOMAIN, "1"));
        return getAllDomains()[index];
    }

    public static String getBrowserDomain() {
        Context context = ContextUtils.getContext();
        SharedPreferences sp = context.getSharedPreferences(PreferenceKey.PERFERENCE, Context.MODE_PRIVATE);
        int index = Integer.parseInt(sp.getString(PreferenceKey.KEY_NGA_DOMAIN_BROWSER, "1"));
        return getAllDomains()[index];
    }

    public static String[] getAllDomains(){
        Context context = ContextUtils.getContext();
        return context.getResources().getStringArray(R.array.nga_domain);
    }


    /**
     * @param statusCode
     * @return 返回子板块是否被订阅
     */
    public static boolean isBoardSubscribed(int statusCode) {
        // 3,810 返回false
        return statusCode == 7 || statusCode == 558 || statusCode == 542 || statusCode == 2606 || statusCode == 2590
                || statusCode == 4654;
    }

}
