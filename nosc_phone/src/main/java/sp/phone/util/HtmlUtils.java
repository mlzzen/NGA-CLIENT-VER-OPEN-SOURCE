package sp.phone.util;

import android.content.Context;

import gov.anzong.androidnga.R;

/**
 * Created by liuboyu on 16/6/30.
 */
public class HtmlUtils {

    public static String hide = null;
    static String blacklistban = null;
    static String legend = null;
    static String attachment = null;
    static String comment = null;
    static String sig = null;

    public static void initStaticStrings(Context activity) {
        hide = activity.getString(R.string.hide);
        blacklistban = activity.getString(R.string.blacklistban);
        legend = activity.getString(R.string.legend);
        attachment = activity.getString(R.string.attachment);
        comment = activity.getString(R.string.comment);
        sig = activity.getString(R.string.sig);
    }



}
