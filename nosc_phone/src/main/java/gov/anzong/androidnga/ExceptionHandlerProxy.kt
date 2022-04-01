package gov.anzong.androidnga

import android.os.DeadSystemException
import android.os.Process
import nosc.utils.ContextUtils
import nosc.utils.PreferenceUtils
import nosc.utils.PreferenceKey
import java.io.File
import java.lang.IllegalStateException
import java.lang.RuntimeException
import java.nio.charset.Charset
import java.util.concurrent.TimeoutException

/**
 * UncaughtException处理类,当程序发生Uncaught异常的时候,有该类
 * 来接管程序,并记录 发送错误报告.
 */
class ExceptionHandlerProxy(private val mOrigExceptionHandler: Thread.UncaughtExceptionHandler?) :
    Thread.UncaughtExceptionHandler {
    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    override fun uncaughtException(thread: Thread, ex: Throwable) {
        File(ContextUtils.getApplication().externalCacheDir,"CrashReport_${System.currentTimeMillis()}_${thread.name}_${BuildConfig.VERSION_CODE}.crash")
            .writeText(ex.stackTraceToString(), Charset.defaultCharset())
        if (ex is TimeoutException && thread.name == "FinalizerWatchdogDaemon"
            || ex is IllegalStateException && thread.name == "GoogleApiHandler"
        ) {
            return
        }
        if (ex is RuntimeException
            && ex.message?.contains("Using WebView from more than one process at once with the same data directory is not supported") == true
        ) {
            var index = PreferenceUtils.getData(PreferenceKey.KEY_WEBVIEW_DATA_INDEX, 0)
            index++
            PreferenceUtils.edit().putInt(PreferenceKey.KEY_WEBVIEW_DATA_INDEX, index).commit()
        }
        if (mOrigExceptionHandler == null || ex is DeadSystemException) {
            Process.killProcess(Process.myPid())
        } else {
            mOrigExceptionHandler.uncaughtException(thread, ex)
        }
    }
}