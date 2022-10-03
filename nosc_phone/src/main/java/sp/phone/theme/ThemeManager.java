package sp.phone.theme;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.TypedValue;

import androidx.annotation.ColorInt;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import gov.anzong.androidnga.R;
import nosc.utils.ContextUtils;
import nosc.utils.ThreadUtils;
import nosc.utils.PreferenceKey;

public class ThemeManager implements SharedPreferences.OnSharedPreferenceChangeListener {

    public int sessionThemeVersion = 0;

    private final int[] mAppThemes = {
            R.style.AppThemeDayNightBrown_NoActionBar,
            R.style.AppThemeDayNightGreen_NoActionBar,
            R.style.AppThemeDayNightBlack_NoActionBar,
    };

    private final int[] mAppThemesActionBar = {
            R.style.AppThemeDayNightBrown,
            R.style.AppThemeDayNightGreen,
            R.style.AppThemeDayNightBlack,
    };

    private int mThemeIndex;

    private boolean mNightMode;

    private WebViewTheme mWebViewTheme;

    private final TypedValue mTypedValue = new TypedValue();

    private boolean mNightModeFollowSystem;

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
        switch (key) {
            case PreferenceKey.NIGHT_MODE:
                mNightMode = sp.getBoolean(key, false);
                applyDayNight();
                break;
            case PreferenceKey.MATERIAL_THEME:
                mThemeIndex = Integer.parseInt(sp.getString(key, "0"));
                break;
            case PreferenceKey.KEY_NIGHT_MODE_FOLLOW_SYSTEM:
                mNightModeFollowSystem = sp.getBoolean(key, false);
                applyDayNight();
                break;
        }
        mWebViewTheme = null;
        sessionThemeVersion++;
    }

    private static class ThemeManagerHolder {

        private static final ThemeManager sInstance = new ThemeManager();
    }

    private ThemeManager() {
        Context mContext = ContextUtils.getContext();
        SharedPreferences sp = mContext.getSharedPreferences(PreferenceKey.PERFERENCE, Context.MODE_PRIVATE);
        sp.registerOnSharedPreferenceChangeListener(this);
        mNightMode = sp.getBoolean(PreferenceKey.NIGHT_MODE, false);
        mThemeIndex = Integer.parseInt(sp.getString(PreferenceKey.MATERIAL_THEME, "1"));
        mNightModeFollowSystem = sp.getBoolean(PreferenceKey.KEY_NIGHT_MODE_FOLLOW_SYSTEM, false);
        applyDayNightDelay(0);
    }

    public static ThemeManager getInstance() {
        return ThemeManagerHolder.sInstance;
    }

    private void applyDayNightDelay(long delay) {
        ThreadUtils.postOnMainThreadDelay(() -> {
            if (mNightModeFollowSystem) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            } else if (mNightMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        }, delay);
    }

    private void applyDayNight() {
        applyDayNightDelay(200);
    }

    public boolean isNightModeFollowSystem() {
        return mNightModeFollowSystem;
    }

    public void initializeWebTheme(Context context) {
        if (mWebViewTheme == null) {
            mWebViewTheme = new WebViewTheme(context);
        }
    }

    public int getForegroundColorRes() {
        return R.color.foreground_color;
    }

    public int getBackgroundColorRes() {
        return getBackgroundColorRes(0);
    }

    public int getBackgroundColorRes(int position) {
        return position % 2 == 1 ? R.color.background_color2 : R.color.background_color;
    }

    public boolean isNightMode() {
        return mNightModeFollowSystem ? ContextUtils.getResources().getBoolean(R.bool.night_mode) : mNightMode;
    }



    @ColorInt
    public int getPrimaryColor(Context context) {
        context.getTheme().resolveAttribute(android.R.attr.colorPrimary, mTypedValue, true);
        return ContextCompat.getColor(context, mTypedValue.resourceId);
    }

    @ColorInt
    public int getAccentColor(Context context) {
        context.getTheme().resolveAttribute(android.R.attr.colorAccent, mTypedValue, true);
        return ContextCompat.getColor(context, mTypedValue.resourceId);
    }

    @StyleRes
    public int getTheme(boolean toolbarEnabled) {
        int index = mThemeIndex;
        return toolbarEnabled ? mAppThemes[index] : mAppThemesActionBar[index];
    }

    public String getThemeName(){
        return ContextUtils.getResources().getStringArray(R.array.material_theme)[mThemeIndex];
    }

    public void applyAboutTheme(AppCompatActivity activity) {
        activity.setTheme(ThemeConstants.THEME_ACTIVITY_ABOUT[mThemeIndex]);
        activity.getDelegate().setLocalNightMode(isNightMode() ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }
}
