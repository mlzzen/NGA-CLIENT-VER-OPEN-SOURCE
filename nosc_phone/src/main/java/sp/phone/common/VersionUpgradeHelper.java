package sp.phone.common;

import android.content.SharedPreferences;

import nosc.utils.ContextUtils;
import nosc.utils.PreferenceUtils;
import nosc.utils.PreferenceKey;

public class VersionUpgradeHelper {

    public static void upgrade() {
        upgradeBookmarkBoards();
    }

    private static void upgradeBookmarkBoards() {
        SharedPreferences sp = ContextUtils.getSharedPreferences(PreferenceKey.PERFERENCE);
        if (sp.contains(PreferenceKey.BOOKMARK_BOARD)) {
            String data = sp.getString(PreferenceKey.BOOKMARK_BOARD, "");
            sp.edit().remove(PreferenceKey.BOOKMARK_BOARD).apply();
            PreferenceUtils.putData(PreferenceKey.BOOKMARK_BOARD, data);
        }
    }

}
