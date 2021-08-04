package gov.anzong.androidnga.activity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.danielstone.materialaboutlibrary.MaterialAboutActivity;
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem;
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard;
import com.danielstone.materialaboutlibrary.model.MaterialAboutList;

import gov.anzong.androidnga.BuildConfig;
import gov.anzong.androidnga.R;
import gov.anzong.androidnga.debug.Debugger;
import sp.phone.ui.fragment.dialog.VersionUpgradeDialogFragment;
import sp.phone.theme.ThemeManager;
import sp.phone.util.FunctionUtils;

public class AboutActivity extends MaterialAboutActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeManager.getInstance().applyAboutTheme(this);
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    protected MaterialAboutList getMaterialAboutList(@NonNull Context context) {
        return new MaterialAboutList(buildAppCard(), buildDevelopCard());
    }

    private MaterialAboutCard buildAppCard() {
        MaterialAboutCard.Builder builder = new MaterialAboutCard.Builder();
        builder.addItem(new MaterialAboutActionItem.Builder()
                .text(R.string.start_title)
                .subText("已经惨遭Yricky魔改，点这里可以骂他。")
                .icon(R.mipmap.ic_launcher)
                .setOnClickAction(() -> FunctionUtils.openUrlByDefaultBrowser(AboutActivity.this, "https://gitee.com/sjtuYricky/NGA-CLIENT-VER-OPEN-SOURCE/issues"))
                .build());

        builder.addItem(new MaterialAboutActionItem.Builder()
                .text("版本")
                .subText(BuildConfig.VERSION_NAME)
                .icon(R.drawable.ic_about)
                .setOnClickAction(() -> {
                    try {
                        String url = "market://details?id=" + getPackageName();
                        Intent intent = new Intent("android.intent.action.VIEW");
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        FunctionUtils.openUrlByDefaultBrowser(AboutActivity.this, "https://gitee.com/sjtuYricky/NGA-CLIENT-VER-OPEN-SOURCE");
                    }
                })
                .build());

        builder.addItem(new MaterialAboutActionItem.Builder()
                .text("License")
                .subText("GNU GPL v2,开放源代码许可")
                .setOnClickAction(() -> {
                    Intent intent = new Intent(AboutActivity.this, WebViewerActivity.class);
                    intent.putExtra("path", "file:///android_asset/OSLICENSE.TXT");
                    startActivity(intent);

                })
                .icon(R.drawable.ic_license)
                .build());

        builder.addItem(new MaterialAboutActionItem.Builder()
                .text("来康康有没有更新")
                .setOnClickAction(() -> {
                    FunctionUtils.openUrlByDefaultBrowser(AboutActivity.this, "https://gitee.com/sjtuYricky/NGA-CLIENT-VER-OPEN-SOURCE/releases");
                    /*
                    Intent intent = new Intent(AboutActivity.this, WebViewerActivity.class);
                    intent.putExtra("path", "https://gitee.com/sjtuYricky/NGA-CLIENT-VER-OPEN-SOURCE/releases");
                    intent.putExtra("title","更新");
                    startActivity(intent);
                     */

                })
                .icon(R.drawable.ic_update_24dp)
                .build());



        return builder.build();
    }

    private MaterialAboutCard buildDevelopCard() {
        MaterialAboutCard.Builder builder = new MaterialAboutCard.Builder();
        builder.title("原来的开源版");
        builder.addItem(new MaterialAboutActionItem.Builder()
                .text("代码")
                .subText("[@竹井詩織里]/[@cfan8]/[@jjimmys]\n[@Moandor]/[@Elrond]/[@Justwen]")
                .setOnLongClickAction(Debugger::toggleDebugMode)
                .icon(R.drawable.ic_code)
                .build());

        builder.addItem(new MaterialAboutActionItem.Builder()
                .text("美工")
                .subText("[@那个惩戒骑]/[@从来不卖萌]")
                .icon(R.drawable.ic_color_lens)
                .build());

        builder.addItem(new MaterialAboutActionItem.Builder()
                .text("Github")
                .subText("bug & 建议")
                .setOnClickAction(() -> FunctionUtils.openUrlByDefaultBrowser(AboutActivity.this, "https://github.com/Justwen/NGA-CLIENT-VER-OPEN-SOURCE"))
                .icon(R.drawable.ic_github)
                .build());
        builder.addItem(new MaterialAboutActionItem.Builder()
                .text("客户端吐槽QQ群,欢迎加入捡肥皂")
                .subText("1065310118")
                .setOnClickAction(() -> FunctionUtils.copyToClipboard(AboutActivity.this, "1065310118"))
                .icon(R.drawable.ic_qq)
                .build());
        builder.addItem(new MaterialAboutActionItem.Builder()
                .text("客户端问题反馈群，请勿开车！")
                .subText("1077054628")
                .setOnClickAction(() -> FunctionUtils.copyToClipboard(AboutActivity.this, "1077054628"))
                .icon(R.drawable.ic_qq)
                .build());
        return builder.build();
    }

/*
    private MaterialAboutCard buildExtraCard() {
        MaterialAboutCard.Builder builder = new MaterialAboutCard.Builder();
        builder.title("赞美片总!感谢[@force0119]");


        return builder.build();
    }

 */

    @Nullable
    @Override
    protected CharSequence getActivityTitle() {
        return getString(R.string.title_about);
    }
}
