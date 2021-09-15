package sp.phone.util;

import android.content.Context;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

import sp.phone.http.bean.ThreadData;
import sp.phone.http.bean.ThreadRowInfo;
import sp.phone.common.UserManagerImpl;
import sp.phone.mvp.model.entity.ThreadPageInfo;

public class ArticleUtil {
    private final static String TAG = ArticleUtil.class.getSimpleName();
    private Context context;

    @SuppressWarnings("static-access")
    public ArticleUtil(Context context) {
        super();
        this.context = context;
    }

    public static int showImageQuality() {
        return 0;
//        if (NetUtil.getInstance().isInWifi()) {
//            return 0;
//        } else {
//            return PhoneConfiguration.getInstance().imageQuality;
//        }
    }

}
