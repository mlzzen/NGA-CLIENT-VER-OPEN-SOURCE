package gov.anzong.androidnga.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import gov.anzong.androidnga.R;
import nosc.utils.uxUtils.SwipeBackHelper;
import nosc.utils.PreferenceKey;
import nosc.utils.ThemeUtilsKt;
import sp.phone.common.NotificationController;
import sp.phone.common.PhoneConfiguration;
import sp.phone.theme.ThemeManager;

/**
 * Created by liuboyu on 16/6/28.
 */
public abstract class BaseActivity extends AppCompatActivity {

    private boolean mToolbarEnabled;

    private SwipeBackHelper mSwipeBackHelper;

    private int themeVersionForThisActivity = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        updateThemeUi();
        ThemeManager.getInstance().initializeWebTheme(this);
        ThemeUtilsKt.applyNavBarColor(this);
        themeVersionForThisActivity = ThemeManager.getInstance().sessionThemeVersion;
        setSwipeBackEnable(getSharedPreferences(PreferenceKey.PREFERENCE_SETTINGS, Context.MODE_PRIVATE).getBoolean(PreferenceKey.KEY_SWIPE_BACK, false));

        super.onCreate(savedInstanceState);

        if (mSwipeBackHelper != null) {
            mSwipeBackHelper.onCreate(this);
        }
    }

    protected void setSwipeBackEnable(boolean enable) {
        if (!enable) {
            mSwipeBackHelper = null;
        } else if (mSwipeBackHelper == null) {
            mSwipeBackHelper = new SwipeBackHelper();
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mSwipeBackHelper != null) {
            mSwipeBackHelper.onPostCreate();
        }
    }

    @Override
    public <T extends View> T findViewById(int id) {
        T t = super.findViewById(id);
        if (t == null && mSwipeBackHelper != null) {
            t = mSwipeBackHelper.findViewById(id);
        }
        return t;
    }


    protected void setToolbarEnabled(boolean enabled) {
        mToolbarEnabled = enabled;
    }

    public void setupToolbar(Toolbar toolbar) {
        if (toolbar != null && getSupportActionBar() == null) {
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeButtonEnabled(true);
            }
        }
    }

    public void setupToolbar() {
        setupToolbar((Toolbar) findViewById(R.id.toolbar));
    }

    public void setupActionBar() {
        if (mToolbarEnabled) {
            setupToolbar();
        } else {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeButtonEnabled(true);
            }
        }
    }

    protected void updateThemeUi() {
        ThemeManager tm = ThemeManager.getInstance();
        setTheme(tm.getTheme(mToolbarEnabled));
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }


    @Override
    protected void onResume() {
        NotificationController.getInstance().checkNotificationDelay();
        if(themeVersionForThisActivity != ThemeManager.getInstance().sessionThemeVersion){
            recreate();
        }
        super.onResume();
    }
}
