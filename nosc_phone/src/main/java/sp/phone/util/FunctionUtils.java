package sp.phone.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import java.util.Objects;

import gov.anzong.androidnga.R;
import gov.anzong.androidnga.Utils;
import gov.anzong.androidnga.activity.WebViewActivity;
import nosc.api.constants.ApiConstants;
import nosc.utils.uxUtils.ToastUtils;
import gov.anzong.androidnga.core.data.HtmlData;
import gov.anzong.androidnga.core.decode.ForumDecoder;
import gov.anzong.androidnga.fragment.dialog.ReportDialogFragment;
import nosc.api.bean.MessageArticlePageInfo;
import nosc.api.bean.ThreadRowInfo;
import sp.phone.common.PhoneConfiguration;
import sp.phone.theme.ThemeManager;
import sp.phone.view.webview.WebViewClientEx;

@SuppressLint("DefaultLocale")
public class FunctionUtils {
    static String userDistance = null;
    static String meter = null;
    static String kiloMeter = null;
    static String hide = null;
    static String blacklistban = null;
    static String legend = null;
    static String attachment = null;
    static String comment = null;
    static String sig = null;

    private static void initStaticStrings(Context activity) {
        userDistance = activity.getString(R.string.user_distance);
        meter = activity.getString(R.string.meter);
        kiloMeter = activity.getString(R.string.kilo_meter);
        hide = activity.getString(R.string.hide);
        blacklistban = activity.getString(R.string.blacklistban);
        legend = activity.getString(R.string.legend);
        attachment = activity.getString(R.string.attachment);
        comment = activity.getString(R.string.comment);
        sig = activity.getString(R.string.sig);
    }

    public static void openUrlByDefaultBrowser(Context context, String url) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.setData(Uri.parse(url));
        context.startActivity(intent);
    }

    public static void openArticleByWebView(Context context, String url) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra("path",url);
        intent.putExtra("fallbackRead",true);
        context.startActivity(intent);
    }

    public static void copyToClipboard(Context context, String text) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager != null) {
            ClipData clipData = ClipData.newPlainText(text, text);
            clipboardManager.setPrimaryClip(clipData);
            ToastUtils.info(R.string.copied_to_clipboard);
        }
    }

    public static void handleContentTV(final WebView contentTV, final MessageArticlePageInfo row, int bgColor, int fgColor, Context context) {
        final WebViewClient client = new WebViewClientEx();
        contentTV.setBackgroundColor(0);
        contentTV.setFocusableInTouchMode(false);
        contentTV.setFocusable(false);
        contentTV.setLongClickable(false);


        WebSettings setting = contentTV.getSettings();
        setting.setUserAgentString(ApiConstants.clientUa);
        setting.setDefaultFontSize(PhoneConfiguration.INSTANCE.getWebSize());
        setting.setJavaScriptEnabled(false);
        contentTV.setWebViewClient(client);

        contentTV.setTag(row.getLou());
        contentTV.loadDataWithBaseURL(null, row.getFormated_html_data(),
                "text/html", "utf-8", null);
    }


    public static void createSignatureDialog(ThreadRowInfo row, final Context context) {
        LayoutInflater layoutInflater = ((Activity) context).getLayoutInflater();
        final View view = layoutInflater.inflate(R.layout.dialog_signature,
                null);
        String name = row.getAuthor();
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setView(view);
        alert.setTitle(name + "的签名");
        // COLOR

        ThemeManager theme = ThemeManager.getInstance();
        int bgColor = context.getResources().getColor(theme.getBackgroundColorRes(0));
        int fgColor = context.getResources().getColor(theme.getForegroundColorRes());
        bgColor = bgColor & 0xffffff;
        final String bgcolorStr = String.format("%06x", bgColor);

        int htmlfgColor = fgColor & 0xffffff;
        final String fgColorStr = String.format("%06x", htmlfgColor);

        WebViewClient client = new WebViewClientEx();
        WebView contentTV = view.findViewById(R.id.signature);
        contentTV.setBackgroundColor(0);
        contentTV.setFocusableInTouchMode(false);
        contentTV.setFocusable(false);
        contentTV.setLongClickable(false);

        WebSettings setting = contentTV.getSettings();
        setting.setDefaultFontSize(PhoneConfiguration.INSTANCE
                .getWebSize());
        setting.setJavaScriptEnabled(true);
        contentTV.setWebViewClient(client);
        contentTV
                .loadDataWithBaseURL(
                        null,
                        FunctionUtils.signatureToHtmlText(row,  fgColorStr,
                                bgcolorStr, context), "text/html", "utf-8", null);
        alert.setPositiveButton("关闭", (dialog, which) -> dialog.dismiss());

        final AlertDialog dialog = alert.create();
        dialog.show();
        dialog.setOnDismissListener(arg0 -> dialog.dismiss());
    }


    public static void handleNickName(MessageArticlePageInfo row, int fgColor,
                                      TextView nickNameTV, Context context) {
        initStaticStrings(context);
        String nickName = row.getAuthor();
        // int now = 0;
        if ("-1".equals(row.getYz()))// nuked
        {
            fgColor = nickNameTV.getResources().getColor(R.color.title_red);
            nickName += "(VIP)";
        } else if (!StringUtils.isEmpty(row.getMute_time())
                && !"0".equals(row.getMute_time())) {
            fgColor = nickNameTV.getResources().getColor(R.color.title_orange);
            nickName += "(" + legend + ")";
        }
        nickNameTV.setText(nickName);
        TextPaint tp = nickNameTV.getPaint();
        tp.setFakeBoldText(true);// bold for Chinese character
        nickNameTV.setTextColor(fgColor);
    }


    public static void handleNickName(ThreadRowInfo row, int fgColor,
                                      TextView nickNameTV, String topicOwner, Context context) {
        initStaticStrings(context);
        String nickName = row.getAuthor();
        // int now = 0;
        if ("-1".equals(row.getYz()))// nuked
        {
            fgColor = nickNameTV.getResources().getColor(R.color.title_red);
            nickName += "(VIP)";
        } else if (!StringUtils.isEmpty(row.getMuteTime())
                && !"0".equals(row.getMuteTime()) || row.isMuted()) {
            fgColor = nickNameTV.getResources().getColor(R.color.title_orange);
            nickName += "(" + legend + ")";
        }
        if (row.get_isInBlackList()) {
            fgColor = nickNameTV.getResources().getColor(R.color.title_orange);
            nickName += "(" + blacklistban + ")";
        }
        if (row.getISANONYMOUS()) {
            fgColor = nickNameTV.getResources().getColor(R.color.title_red);
            nickName += "(匿名)";
        }

        if (Objects.equals(row.getAuthor(), topicOwner)) {
            nickName += "(楼主)";
        }

        nickNameTV.setText(nickName);
        TextPaint tp = nickNameTV.getPaint();
        tp.setFakeBoldText(true);// bold for Chinese character
        nickNameTV.setTextColor(fgColor);
    }


    public static String signatureToHtmlText(final ThreadRowInfo row,
                                             final String fgColorStr,
                                             final String bgcolorStr, Context context) {
        initStaticStrings(context);
        String ngaHtml = ForumDecoder.decode(row.getSignature(), HtmlData.create(row.getSignature(), Utils.getNGAHost()));
        if (StringUtils.isEmpty(ngaHtml)) {
            ngaHtml = row.getAlterinfo();
        }
        if (StringUtils.isEmpty(ngaHtml)) {
            ngaHtml = "<font color='red'>[" + context.getString(R.string.hide)
                    + "]</font>";
        }
        ngaHtml = "<HTML> <HEAD><META   http-equiv=Content-Type   content= \"text/html;   charset=utf-8 \">"
                + "<body bgcolor= '#"
                + bgcolorStr
                + "'>"
                + "<font color='#"
                + fgColorStr + "' size='2'>" + ngaHtml + "</font></body>"
                + "<script type=\"text/javascript\" src=\"file:///android_asset/html/script.js\"></script>";

        return ngaHtml;
    }


    public static String parseAvatarUrl(String js_escap_avatar) {
        // "js_escap_avatar":"{ \"t\":1,\"l\":2,\"0\":{ \"0\":\"http://pic2.178.com/53/533387/month_1109/93ba4788cc8c7d6c75453fa8a74f3da6.jpg\",\"cX\":0.47,\"cY\":0.78},\"1\":{ \"0\":\"http://pic2.178.com/53/533387/month_1108/8851abc8674af3adc622a8edff731213.jpg\",\"cX\":0.49,\"cY\":0.68}}"
        if (null == js_escap_avatar)
            return null;

        int start = js_escap_avatar.indexOf("http");
        if (start == 0 || start == -1)
            return js_escap_avatar;
        int end = js_escap_avatar.indexOf("\"", start);//
        if (end == -1)
            end = js_escap_avatar.length();
        String ret = null;
        try {
            ret = js_escap_avatar.substring(start, end);
        } catch (Exception e) {
            NLog.e("FunctionUtils", "cann't handle avatar url " + js_escap_avatar);
        }
        return ret;
    }

    public static boolean isComment(ThreadRowInfo row) {

        return row.getAlterinfo() == null && row.getAttachs() == null
                && row.getComments() == null
                && row.getJs_escap_avatar() == null && row.getLevel() == null
                && row.getSignature() == null;
    }

    public static void handleReport(ThreadRowInfo row, int tid, FragmentManager fm) {

        DialogFragment df = new ReportDialogFragment();
        Bundle args = new Bundle();
        args.putInt("tid", tid);
        args.putInt("pid", row.getPid());
        df.setArguments(args);
        df.show(fm, null);

    }

    public static String checkContent(String content) {
        int i;
        boolean mode = false;
        content = content.trim();
        String[][] quoteKeyword = {
                {"[customachieve]", "[/customachieve]"},// 0
                {"[wow", "]]"},
                {"[lol", "]]"},
                {"[cnarmory", "]"},
                {"[usarmory", "]"},
                {"[twarmory", "]"},// 5
                {"[euarmory", "]"},
                {"[url", "[/url]"},
                {"[color=", "[/color]"},
                {"[size=", "[/size]"},
                {"[font=", "[/font]"},// 10
                {"[b]", "[/b]"},
                {"[u]", "[/u]"},
                {"[i]", "[/i]"},
                {"[del]", "[/del]"},
                {"[align=", "[/align]"},// 15
                {"[h]", "[/h]"},
                {"[l]", "[/l]"},
                {"[r]", "[/r]"},
                {"[list", "[/list]"},
                {"[img]", "[/img]"},// 20
                {"[album=", "[/album]"},
                {"[code]", "[/code]"},
                {"[code=lua]", "[/code] lua"},
                {"[code=php]", "[/code] php"},
                {"[code=c]", "[/code] c"},// 25
                {"[code=js]", "[/code] javascript"},
                {"[code=xml]", "[/code] xml/html"},
                {"[flash]", "[/flash]"},
                {"[table]", "[/table]"},
                {"[tid", "[/tid]"},// 30
                {"[pid", "[/pid]"}, {"[dice]", "[/dice]"},
                {"[crypt]", "[/crypt]"},
                {"[randomblock]", "[/randomblock]"}, {"[@", "]"},
                {"[t.178.com/", "]"}, {"[collapse", "[/collapse]"},};
        while (content.startsWith("\n")) {
            content = content.replaceFirst("\n", "");
        }
        if (content.length() > 100) {
            content = content.substring(0, 99);
            mode = true;
        }
        for (i = 0; i < quoteKeyword.length; i++) {
            while (content.toLowerCase().lastIndexOf(quoteKeyword[i][0]) > content
                    .toLowerCase().lastIndexOf(quoteKeyword[i][1])) {
                content = content.substring(0, content.toLowerCase()
                        .lastIndexOf(quoteKeyword[i][0]));
            }
        }
        if (mode) {
            content = content + "......";
        }
        return content;
    }


    public static String ColorTxt(String bodyString) {
        while (bodyString.startsWith("\n")) {
            bodyString = bodyString.substring(1);
        }
        String existquotetxt = "";
        if (bodyString.toLowerCase().indexOf("[quote]") == 0) {
            existquotetxt = bodyString.substring(0, bodyString.toLowerCase().indexOf("[/quote]")) + "[/quote]";
            bodyString = bodyString.substring(bodyString.toLowerCase().indexOf("[/quote]") + 8);
        }
        int i, ia,  itmp, bslenth;
        bslenth = bodyString.length();


        String[] scolor = {"[color=skyblue]", "[color=royalblue]", "[color=blue]", "[color=darkblue]", "[color=orange]", "[color=orangered]", "[color=crimson]", "[color=red]", "[color=firebrick]", "[color=darkred]", "[color=green]", "[color=limegreen]", "[color=seagreen]", "[color=teal]", "[color=deeppink]", "[color=tomato]", "[color=coral]", "[color=purple]", "[color=indigo]", "[color=burlywood]", "[color=sandybrown]", "[color=sienna]", "[color=chocolate]", "[color=silver]"};
        String[][] keyword = {
                {"[customachieve]", "[/customachieve]", "16"},
                {"[wow", "]]", "2"},
                {"[lol", "]]", "2"},
                {"[cnarmory", "]", "1"},
                {"[usarmory", "]", "1"},
                {"[twarmory", "]", "1"},
                {"[euarmory", "]", "1"},
                {"[url", "[/url]", "6"},
                {"[size=", "]", "1"},
                {"[/size]", "[/size]", "7"},
                {"[font=", "]", "1"},
                {"[/font]", "[/font]", "7"},
                {"[b]", "[b]", "3"},
                {"[/b]", "[/b]", "4"},
                {"[u]", "[u]", "3"},
                {"[/u]", "[/u]", "4"},
                {"[i]", "[i]", "3"},
                {"[/i]", "[/i]", "4"},
                {"[del]", "[del]", "5"},
                {"[/del]", "[/del]", "6"},
                {"[align", "]", "1"},
                {"[/align]", "[/align]", "8"},
                {"[l]", "[l]", "3"},
                {"[/l]", "[/l]", "4"},
                {"[h]", "[h]", "3"},
                {"[/h]", "[/h]", "4"},
                {"[r]", "[r]", "3"},
                {"[/r]", "[/r]", "4"},
                {"[img]", "[/img]", "6"},
                {"[album=", "[/album]", "8"},
                {"[code]", "[/code]", "7"},
                {"[code=lua]", "[/code] lua", "11"},
                {"[code=php]", "[/code] php", "11"},
                {"[code=c]", "[/code] c", "9"},
                {"[code=js]", "[/code] javascript", "18"},
                {"[code=xml]", "[/code] xml/html", "16"},
                {"[flash]", "[/flash]", "8"},
                {"[table]", "[table]", "7"},
                {"[/table]", "[/table]", "8"},
                {"[tid", "[/tid]", "6"},
                {"[pid", "[/pid]", "6"},
                {"[dice]", "[/dice]", "7"},
                {"[crypt]", "[/crypt]", "8"},
                {"[randomblock]", "[randomblock]", "13"},
                {"[/randomblock]", "[/randomblock]", "14"},
                {"[@", "]", "1"},
                {"[t.178.com/", "]", "1"},
                {"[tr]", "[tr]", "4"},
                {"[/tr]", "[/tr]", "5"},
                {"[td", "]", "1"},
                {"[/td]", "[/td]", "5"},
                {"[*]", "[*]", "3"},
                {"[list", "]", "1"},
                {"[/list]", "[/list]", "7"},
                {"[collapse", "]", "1"},
                {"[/collapse]", "[/collapse]", "11"}};
        char[] arrtxtchar = bodyString.toCharArray();
        StringBuilder txtsendout = new StringBuilder(scolor[(int) (Math.random() * 23)]);
        String quotetxt = "";
        for (i = 0; i < bslenth; i++) {
            if (!Character.toString(arrtxtchar[i]).equals("\n") && !Character.toString(arrtxtchar[i]).equals("[") && !Character.toString(arrtxtchar[i]).equals(" ")) {
                txtsendout.append(arrtxtchar[i]).append("[/color]").append(scolor[(int) (Math.random() * 23)]);/*开始就是普通文字的话就直接加彩色字体了*/
            } else if (Character.toString(arrtxtchar[i]).equals("[")) {//首字符是[要判断
                if (bodyString.toLowerCase().indexOf("[quote]", i - 1) == i) {//是引用的话
                    if (bodyString.toLowerCase().indexOf("[quote]", i - 1) > bodyString.toLowerCase().indexOf("[/quote]", i - 1)) {//这个他妈的引用没完
                        quotetxt = bodyString.substring(i + 7);
                        if (quotetxt.toLowerCase().lastIndexOf("[") >= 0) {//最后还有点留下来
                            quotetxt = quotetxt.substring(0, quotetxt.toLowerCase().lastIndexOf("["));
                        }
                        while (quotetxt.endsWith(".")) {
                            quotetxt = quotetxt.substring(0, quotetxt.length() - 1);
                        }
                        txtsendout = new StringBuilder(txtsendout.substring(0, txtsendout.toString().toLowerCase().lastIndexOf("[color")));
                        quotetxt = "[quote]" + FunctionUtils.checkContent(quotetxt) + "[/quote]";
                        txtsendout.append(quotetxt).append(scolor[(int) (Math.random() * 23)]);
                        break;
                    } else {
                        quotetxt = bodyString.substring(i + 7, bodyString.toLowerCase().indexOf("[/quote]", i));
                        while (quotetxt.endsWith(".")) {
                            quotetxt = quotetxt.substring(0, quotetxt.length() - 1);
                        }
                        txtsendout = new StringBuilder(txtsendout.substring(0, txtsendout.toString().toLowerCase().lastIndexOf("[color")));
                        quotetxt = "[quote]" + FunctionUtils.checkContent(quotetxt) + "[/quote]";
                        txtsendout.append(quotetxt).append(scolor[(int) (Math.random() * 23)]);
                        i = bodyString.toLowerCase().indexOf("[/quote]", i) + 7;
                    }
                } else if (bodyString.toLowerCase().indexOf("[color", i - 1) == i) {
                    if (bodyString.toLowerCase().indexOf("[/color]", i) >= 0) {
                        txtsendout.append(bodyString.substring(bodyString.indexOf("]", i) + 1, bodyString.toLowerCase().indexOf("[/color]", i) + 8)).append(scolor[(int) (Math.random() * 23)]);
                        i = bodyString.indexOf("[/color]", i) + 7;
                    } else {
                        bodyString = bodyString.substring(0, i) + bodyString.substring(bodyString.toLowerCase().indexOf("]", i) + 1, bslenth);
                        i = bodyString.indexOf("]", i);
                    }
                } else {
                    for (ia = 0; ia < 56; ia++) {
                        if (bodyString.toLowerCase().indexOf(keyword[ia][0], i - 1) == i) {
                            if (bodyString.toLowerCase().indexOf(keyword[ia][1], i) >= 0) {
                                txtsendout = new StringBuilder(txtsendout.substring(0, txtsendout.toString().toLowerCase().lastIndexOf("[color")));
                                txtsendout.append(bodyString.substring(i, bodyString.toLowerCase().indexOf(keyword[ia][1], i))).append(keyword[ia][1]).append(scolor[(int) (Math.random() * 23)]);
                                i = bodyString.toLowerCase().indexOf(keyword[ia][1], i) + Integer.parseInt(keyword[ia][2]) - 1;
                            } else {
                                itmp = bodyString.indexOf("]", i);
                                bodyString = bodyString.substring(0, i) + bodyString.substring(bodyString.toLowerCase().indexOf("]", i) + 1, bslenth);
                                i = itmp;
                            }
                            break;
                        }
                    }
                }
            } else if (Character.toString(arrtxtchar[i]).equals(" ") || Character.toString(arrtxtchar[i]).equals("\n")) {
                txtsendout = new StringBuilder(txtsendout.substring(0, txtsendout.toString().toLowerCase().lastIndexOf("[color")));
                txtsendout.append(bodyString.substring(i, i + 1)).append(scolor[(int) (Math.random() * 23)]);
            }
        }
        if (txtsendout.toString().toLowerCase().lastIndexOf("[color") >= 0) {
            txtsendout = new StringBuilder(txtsendout.substring(0, txtsendout.toString().toLowerCase().lastIndexOf("[color")));
        }
        txtsendout = new StringBuilder(existquotetxt + txtsendout.toString().replaceAll("&nbsp;", " ").trim());
        return txtsendout.toString();
    }

    public static String ColorTxtCheck(String text) {
        String xxtp = "";
        if (PhoneConfiguration.INSTANCE.isShowColorText()) {
            xxtp = FunctionUtils.ColorTxt(text.trim());
        } else {
            xxtp = text;
        }
        return xxtp;
    }

    public static String getPath(final Context context, final Uri uri) {


        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/"
                            + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.parseLong(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection,
                        selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri,
                                       String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri
                .getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri
                .getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri
                .getAuthority());
    }

    public static void share(Context context, String title, String content) {
        try {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, content);
            context.startActivity(Intent.createChooser(intent, title));
        } catch (ActivityNotFoundException e) {
            ToastUtils.error("分享失败！");
        }
    }
}
