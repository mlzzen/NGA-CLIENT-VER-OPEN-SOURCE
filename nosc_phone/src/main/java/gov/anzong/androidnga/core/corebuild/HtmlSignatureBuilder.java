package gov.anzong.androidnga.core.corebuild;

import android.text.TextUtils;

import gov.anzong.androidnga.core.data.HtmlData;
import gov.anzong.androidnga.core.decode.ForumDecoder;

/**
 *
 * @author Justwen
 * @date 2018/8/28
 */
public class HtmlSignatureBuilder implements IHtmlBuild {

    private static final String HTML_SIGNATURE = "<br/><div class='collapse'><button onclick='toggleCollapse(this,\"签名\")'>签名(点击隐藏)</button><div name='collapse' style='display:'>%s</div></div>";

    @Override
    public CharSequence build(HtmlData htmlData) {
        if (TextUtils.isEmpty(htmlData.getSignature())) {
            return "";
        } else {
            return String.format(HTML_SIGNATURE, ForumDecoder.decode(htmlData.getSignature(), htmlData));
        }
    }
}
