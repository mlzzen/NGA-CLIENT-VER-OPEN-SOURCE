package gov.anzong.androidnga.activity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.alibaba.android.arouter.launcher.ARouter
import gov.anzong.androidnga.R
import gov.anzong.androidnga.arouter.ARouterConstants
import nosc.utils.PermissionUtils
import gov.anzong.androidnga.fragment.NavigationDrawerFragment
import sp.phone.common.UserManagerImpl
import sp.phone.param.ParamKey
import sp.phone.theme.ThemeManager
import sp.phone.util.ARouterUtils
import sp.phone.util.ActivityUtils

class MainActivity : BaseActivity() {
    private var mIsNightMode = false
    private lateinit var mBoardFragment: NavigationDrawerFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        setToolbarEnabled(true)
        super.onCreate(savedInstanceState)
        setSwipeBackEnable(false)
        checkPermission()
        initView()
        mIsNightMode = ThemeManager.getInstance().isNightMode
        setTitle(R.string.start_title)
    }

    private fun checkPermission() {
        PermissionUtils.request(this, null, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    override fun onResume() {
        if (mIsNightMode != ThemeManager.getInstance().isNightMode) {
            recreate()
        }else{
            super.onResume()
        }
    }

    private fun initView() {
        val fm = supportFragmentManager
        mBoardFragment =
            (fm.findFragmentByTag(NavigationDrawerFragment::class.java.simpleName) as NavigationDrawerFragment?) ?: NavigationDrawerFragment().also{
                fm.beginTransaction().replace(
                    android.R.id.content,
                    it,
                    NavigationDrawerFragment::class.java.simpleName
                ).commit()
            }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action buttons
        when (item.itemId) {
            R.id.menu_setting -> startSettingActivity()
            R.id.menu_bookmark -> startFavoriteTopicActivity()
            R.id.menu_msg -> startMessageActivity()
            R.id.menu_history -> ActivityUtils.startHistoryTopicActivity(this)
            R.id.menu_post -> startPostActivity(false)
            R.id.menu_reply -> startPostActivity(true)
            R.id.menu_about -> aboutNgaClient()
            R.id.menu_search -> startSearchActivity()
            R.id.menu_forward -> gov.anzong.androidnga.fragment.dialog.UrlInputDialogFragment().show(supportFragmentManager)
            R.id.menu_gun -> startNotificationActivity()
            R.id.menu_download -> startActivity(Intent(this, TopicCacheActivity::class.java))
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun startSearchActivity() {
        ARouter.getInstance()
            .build(ARouterConstants.ACTIVITY_SEARCH)
            .navigation(this)
    }

    private fun startMessageActivity() {
        ARouterUtils
            .build(ARouterConstants.ACTIVITY_MESSAGE_LIST)
            .navigation(this)
    }

    private fun aboutNgaClient() {
        // new AboutClientDialogFragment().show(getSupportFragmentManager());
        startActivity(Intent(this, AboutActivity::class.java))
    }

    private fun startSettingActivity() {
        val intent = Intent()
        intent.setClass(this@MainActivity, SettingsActivity::class.java)
        startActivityForResult(intent, ActivityUtils.REQUEST_CODE_SETTING)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ActivityUtils.REQUEST_CODE_SETTING && resultCode == RESULT_OK) {
            recreate()
        } else {
            mBoardFragment.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun startNotificationActivity() {
        ARouterUtils
            .build(ARouterConstants.ACTIVITY_NOTIFICATION)
            .navigation(this)
    }

    private fun startPostActivity(isReply: Boolean) {
        UserManagerImpl.getInstance().activeUser?.let{ user ->
            val userName = user.nickName ?: ""
            val postcard = ARouterUtils
                .build(ARouterConstants.ACTIVITY_TOPIC_LIST)
                .withInt(ParamKey.KEY_AUTHOR_ID, user.userId.toInt())
                .withString(ParamKey.KEY_AUTHOR, userName)
            if (isReply) {
                postcard.withInt(ParamKey.KEY_SEARCH_POST, 1)
            }
            postcard.navigation(this)
        }
    }

    private fun startFavoriteTopicActivity() {
        ARouterUtils
            .build(ARouterConstants.ACTIVITY_TOPIC_LIST)
            .withInt(ParamKey.KEY_FAVOR, 1)
            .navigation(this)
    }
}