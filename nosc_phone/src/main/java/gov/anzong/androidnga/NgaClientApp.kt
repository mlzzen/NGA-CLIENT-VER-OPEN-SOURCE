package gov.anzong.androidnga

import android.app.Application
import sp.phone.util.NLog
import sp.phone.common.VersionUpgradeHelper
import gov.anzong.androidnga.base.util.PreferenceUtils
import gov.anzong.androidnga.common.PreferenceKey
import android.webkit.WebView
import com.alibaba.android.arouter.launcher.ARouter
import gov.anzong.androidnga.base.util.ContextUtils
import sp.phone.common.UserManagerImpl
import sp.phone.common.FilterKeywordsManagerImpl

class NgaClientApp : Application() {
    override fun onCreate() {
        NLog.w(TAG, "app nga android start")
        ContextUtils.setApplication(this)
        checkNewVersion()
        VersionUpgradeHelper.upgrade()
        initCoreModule()
        initRouter()
        super.onCreate()
        fixWebViewMultiProcessException()
        Thread.setDefaultUncaughtExceptionHandler(ExceptionHandlerProxy(Thread.getDefaultUncaughtExceptionHandler()))
    }

    private fun fixWebViewMultiProcessException() {
        val index = PreferenceUtils.getData(PreferenceKey.KEY_WEBVIEW_DATA_INDEX, 0)
        if (index > 0) {
            WebView.setDataDirectorySuffix(index.toString())
        }
    }

    private fun initRouter() {
        if (BuildConfig.DEBUG) {   // 这两行必须写在init之前，否则这些配置在init过程中将无效
            ARouter.openLog() // 打印日志
            ARouter.openDebug() // 开启调试模式(如果在InstantRun模式下运行，必须开启调试模式！线上版本需要关闭,否则有安全风险)
        }
        ARouter.init(this) // 尽可能早，推荐在Application中初始化
    }

    private fun initCoreModule() {
        UserManagerImpl.getInstance().initialize(this)
        FilterKeywordsManagerImpl.getInstance().initialize(this)
        //        // 注册crashHandler
//        CrashHandler.getInstance().init(this);
    }

    private fun checkNewVersion() {
        val versionCode = PreferenceUtils.getData(PreferenceKey.VERSION_CODE, 0)
        if (BuildConfig.VERSION_CODE > versionCode) {
            PreferenceUtils.putData(PreferenceKey.PREVIOUS_VERSION_CODE, versionCode)
            PreferenceUtils.putData(PreferenceKey.VERSION_CODE, BuildConfig.VERSION_CODE)
            isNewVersion = true
            PreferenceUtils.putData(PreferenceKey.KEY_WEBVIEW_DATA_INDEX, 0)
        }
    }

    companion object {
        private val TAG = NgaClientApp::class.java.simpleName
        var isNewVersion = false
    }
}