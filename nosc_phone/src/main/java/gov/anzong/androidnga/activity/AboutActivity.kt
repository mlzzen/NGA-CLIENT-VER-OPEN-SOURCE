package gov.anzong.androidnga.activity

import com.danielstone.materialaboutlibrary.MaterialAboutActivity
import android.os.Bundle
import sp.phone.theme.ThemeManager
import com.danielstone.materialaboutlibrary.model.MaterialAboutList
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem
import gov.anzong.androidnga.R
import sp.phone.util.FunctionUtils
import android.content.Intent
import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import gov.anzong.androidnga.BuildConfig
import gov.anzong.androidnga.debug.Debugger
import java.lang.RuntimeException

class AboutActivity : MaterialAboutActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.getInstance().applyAboutTheme(this)
        super.onCreate(savedInstanceState)
    }

    override fun getMaterialAboutList(context: Context): MaterialAboutList {
        return MaterialAboutList(buildAppCard(), buildDevelopCard())
    }

    private fun buildAppCard(): MaterialAboutCard {
        val builder = MaterialAboutCard.Builder()
        builder.addItem(MaterialAboutActionItem.Builder()
            .text(R.string.start_title)
            .subText("已经惨遭Yricky魔改，点这里可以骂他。")
            .icon(R.mipmap.ic_launcher)
            .setOnClickAction {
                FunctionUtils.openUrlByDefaultBrowser(
                    this@AboutActivity,
                    "https://gitee.com/sjtuYricky/NGA-CLIENT-VER-OPEN-SOURCE/issues"
                )
            }
            .build())
        builder.addItem(MaterialAboutActionItem.Builder()
            .text("版本")
            .subText(BuildConfig.VERSION_NAME)
            .icon(if (BuildConfig.DEBUG) R.drawable.ic_about_debug else R.drawable.ic_about)
            .setOnLongClickAction {
                startActivity(Intent(this,CrashReportActivity::class.java))
            }
//            .setOnClickAction {
//                try {
//                    val url = "market://details?id=$packageName"
//                    val intent = Intent("android.intent.action.VIEW")
//                    intent.data = Uri.parse(url)
//                    startActivity(intent)
//                } catch (e: ActivityNotFoundException) {
//                    FunctionUtils.openUrlByDefaultBrowser(
//                        this@AboutActivity,
//                        "https://gitee.com/sjtuYricky/NGA-CLIENT-VER-OPEN-SOURCE"
//                    )
//                }
//            }
            .build())
        builder.addItem(MaterialAboutActionItem.Builder()
            .text("License")
            .subText("GNU GPL v2,开放源代码许可")
            .setOnClickAction {
                val intent = Intent(this@AboutActivity, FullScreenWebViewActivity::class.java)
                intent.putExtra("path", "file:///android_asset/OSLICENSE.TXT")
                startActivity(intent)
            }
            .icon(R.drawable.ic_license)
            .build())
        builder.addItem(MaterialAboutActionItem.Builder()
            .text("代码")
            .subText("[@Yricky]")
//            .setOnLongClickAction { Debugger.toggleDebugMode() }
            .icon(R.drawable.ic_code)
            .build())
        builder.addItem(
            MaterialAboutActionItem.Builder()
                .text("感谢MNGA作者提供的新图标")
                .icon(R.drawable.ic_color_lens)
                .build()
        )
        builder.addItem(MaterialAboutActionItem.Builder()
            .text("来康康有没有更新")
            .setOnClickAction {
                FunctionUtils.openUrlByDefaultBrowser(
                    this@AboutActivity,
                    "https://gitee.com/sjtuYricky/NGA-CLIENT-VER-OPEN-SOURCE/releases"
                )
            }
            .icon(R.drawable.ic_update_24dp)
            .build())

        return builder.build()
    }

    private fun buildDevelopCard(): MaterialAboutCard {
        val builder = MaterialAboutCard.Builder()
        builder.title("原来的开源版")
        builder.addItem(MaterialAboutActionItem.Builder()
            .text("代码")
            .subText("[@竹井詩織里]/[@cfan8]/[@jjimmys]\n[@Moandor]/[@Elrond]/[@Justwen]")
            .setOnLongClickAction { Debugger.toggleDebugMode() }
            .icon(R.drawable.ic_code)
            .build())
        builder.addItem(
            MaterialAboutActionItem.Builder()
                .text("美工")
                .subText("[@那个惩戒骑]/[@从来不卖萌]")
                .icon(R.drawable.ic_color_lens)
                .build()
        )
        builder.addItem(MaterialAboutActionItem.Builder()
            .text("Github")
            .setOnClickAction {
                FunctionUtils.openUrlByDefaultBrowser(
                    this@AboutActivity,
                    "https://github.com/Justwen/NGA-CLIENT-VER-OPEN-SOURCE"
                )
            }
            .icon(R.drawable.ic_github)
            .build())
//        builder.addItem(MaterialAboutActionItem.Builder()
//            .text("客户端吐槽QQ群,欢迎加入捡肥皂")
//            .subText("1065310118")
//            .setOnClickAction {
//                FunctionUtils.copyToClipboard(
//                    this@AboutActivity,
//                    "1065310118"
//                )
//            }
//            .icon(R.drawable.ic_qq)
//            .build())
//        builder.addItem(MaterialAboutActionItem.Builder()
//            .text("客户端问题反馈群，请勿开车！")
//            .subText("1077054628")
//            .setOnClickAction {
//                FunctionUtils.copyToClipboard(
//                    this@AboutActivity,
//                    "1077054628"
//                )
//            }
//            .icon(R.drawable.ic_qq)
//            .build())
        return builder.build()
    }

    override fun getActivityTitle(): CharSequence {
        return getString(R.string.title_about)
    }
}