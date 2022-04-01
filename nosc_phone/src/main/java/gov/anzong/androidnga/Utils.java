package gov.anzong.androidnga;

import sp.phone.util.ForumUtils;

/**
 * Created by liuboyu on 2015/8/25.
 */
public class Utils {
    public static String getNGAHost() {
        return ForumUtils.getAvailableDomain() + "/";
    }

    public static String getNGADomain() {
        return ForumUtils.getAvailableDomain();
    }
}
