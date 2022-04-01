package gov.anzong.androidnga.activity

import com.danielstone.materialaboutlibrary.MaterialAboutActivity
import android.os.Bundle
import sp.phone.theme.ThemeManager
import com.danielstone.materialaboutlibrary.model.MaterialAboutList
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem
import gov.anzong.androidnga.R
import android.content.Context
import gov.anzong.androidnga.BuildConfig
import nosc.utils.ContextUtils
import nosc.utils.uxUtils.ToastUtils
import java.nio.charset.Charset

class CrashReportActivity : MaterialAboutActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.getInstance().applyAboutTheme(this)
        super.onCreate(savedInstanceState)
    }

    override fun getMaterialAboutList(context: Context): MaterialAboutList {
        return MaterialAboutList(buildCrashCard())
    }

    private fun buildCrashCard(): MaterialAboutCard {
        val builder = MaterialAboutCard.Builder()
        builder.addItem(MaterialAboutActionItem.Builder()
            .text("版本")
            .subText(BuildConfig.VERSION_NAME)
            .icon(R.drawable.ic_about_debug)
            .setOnLongClickAction {
                throw RuntimeException("Crash test")
            }
            .build())
        ContextUtils.getApplication().externalCacheDir?.listFiles { dir, name ->
            name.startsWith("CrashReport")
        }?.forEach {
            builder.addItem(MaterialAboutActionItem.Builder()
                .text(it.name.removePrefix("CrashReport_").removeSuffix(".crash"))
                .subText(it.readText(Charset.defaultCharset()))
                .setOnLongClickAction {
                    it.delete()
                    ToastUtils.info("Deleted ${it.name}")
                }
                .icon(R.drawable.ic_license)
                .build())
        }

        return builder.build()
    }

    override fun getActivityTitle(): CharSequence {
        return "Crash report"//getString(R.string.title_about)
    }
}