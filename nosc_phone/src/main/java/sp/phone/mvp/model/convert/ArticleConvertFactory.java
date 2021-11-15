package sp.phone.mvp.model.convert;

import static nosc.api.pojo.DiceDataKt.rnd;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.anzong.androidnga.Utils;
import gov.anzong.androidnga.base.util.DeviceUtils;
import gov.anzong.androidnga.core.HtmlConvertFactory;
import gov.anzong.androidnga.core.data.AttachmentData;
import gov.anzong.androidnga.core.data.CommentData;
import gov.anzong.androidnga.core.data.HtmlData;
import gov.anzong.androidnga.base.util.ContextUtils;
import sp.phone.common.PhoneConfiguration;
import nosc.api.bean.Attachment;
import nosc.api.pojo.DiceData;
import nosc.api.bean.ThreadData;
import nosc.api.bean.ThreadRowInfo;
import nosc.api.constants.ForumConstants;
import sp.phone.common.UserManagerImpl;
import sp.phone.mvp.model.entity.ThreadPageInfo;
import sp.phone.theme.ThemeManager;
import sp.phone.util.FunctionUtils;
import sp.phone.util.HttpUtil;
import sp.phone.util.NLog;
import sp.phone.util.StringUtils;

/**
 * Created by Justwen on 2017/12/3.
 */

public abstract class ArticleConvertFactory {

    private static final String TAG = ArticleConvertFactory.class.getSimpleName();

    public static ThreadData getArticleInfo(String js) {
        return parseJsonThreadPage(js);
    }

    private static ThreadData parseJsonThreadPage(String js) {
        ThreadData data = null;
        try {
            if (js.isEmpty()) {
                return null;
            } else if (js.contains("/*error fill content")) {
                js = js.substring(0, js.indexOf("/*error fill content"));
            }

            js = js.replaceAll("/\\*\\$js\\$\\*/", "")
                    .replaceAll("\"content\":\\+(\\d+),", "\"content\":\"+$1\",")
                    .replaceAll("\"subject\":\\+(\\d+),", "\"subject\":\"+$1\",")
                    .replaceAll("\"content\":(0\\d+),", "\"content\":\"$1\",")
                    .replaceAll("\"subject\":(0\\d+),", "\"subject\":\"$1\",")
                    .replaceAll("\"author\":(0\\d+),", "\"author\":\"$1\",")
                    .replaceAll("\"alterinfo\":\"\\[(\\w|\\s)+\\]\\s+\",", ""); //部分页面打不开的问题

            JSONObject obj = (JSONObject) JSON.parseObject(js).get("data");
            NLog.d(TAG, "js = :\n" + js);
            if (obj == null) {
                return null;
            }
            int allRows = (Integer) obj.get("__ROWS");
            data = new ThreadData();
            data.setRawData(js);
            data.setThreadInfo(buildThreadPageInfo(obj));
            data.setRowList(buildThreadRowList(obj));
            data.set__ROWS(allRows);
            data.setRowNum(data.getRowList().size());
        } catch (Exception e) {
            NLog.e(TAG, "can not parse :\n" + js);
            e.printStackTrace();
        }
        return data;
    }

    private static ThreadPageInfo buildThreadPageInfo(JSONObject obj) {
        JSONObject subObj = (JSONObject) obj.get("__T");
        if (subObj == null) {
            return null;
        }
        try {
            return JSONObject.toJavaObject(subObj, ThreadPageInfo.class);
        } catch (RuntimeException e) {
            NLog.e(TAG, subObj.toJSONString());
        }
        return null;
    }

    private static List<ThreadRowInfo> buildThreadRowList(JSONObject obj) {
        JSONObject subObj = (JSONObject) obj.get("__R");
        int rows = (Integer) obj.get("__R__ROWS");
        JSONObject userInfoMap = (JSONObject) obj.get("__U");
        if (subObj == null) {
            return new ArrayList<>();
        }
        return convertJsObjToList(subObj, rows, userInfoMap);
    }


    private static List<ThreadRowInfo> convertJsObjToList(JSONObject rowMap, int count, JSONObject userInfoMap) {
        List<ThreadRowInfo> rowList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Object obj = rowMap.get(String.valueOf(i));
            JSONObject rowObj;
            if (obj instanceof JSONObject) {
                rowObj = (JSONObject) obj;
            } else {
                continue;
            }
            ThreadRowInfo row = JSONObject.toJavaObject(rowObj, ThreadRowInfo.class);
            buildRowHotReplay(row, rowObj);
            buildRowComment(row, rowObj, userInfoMap);
            buildRowClientInfo(row, rowObj);
            buildRowUserInfo(row, userInfoMap);
            buildRowVote(row, rowObj);
            buildRowContent(row);
            rowList.add(row);
        }
        return rowList;
    }

    private static void buildRowContent(ThreadRowInfo row) {
        if (row.getContent() == null) {
            row.setContent(row.getSubject());
            row.setSubject(null);
        }
        if (!StringUtils.isEmpty(row.getFromClient())
                && row.getFromClient().startsWith("103 ")
                && !StringUtils.isEmpty(row.getContent())) {
            row.setContent(StringUtils.unescape(row.getContent()));
        }
        List<String> imageUrls = new ArrayList<>();
        String ngaHtml = HtmlConvertFactory.convert(buildHtmlData(row),imageUrls);
        DiceData arg = new DiceData(
                ngaHtml,
                row.getAuthorid(),
                row.getTid(),
                row.getPid(),
                0,
                0.0
        );

        ngaHtml = getRealDice(arg);
        row.getImageUrls().addAll(imageUrls);
        row.setFormattedHtmlData(ngaHtml);
    }

    private static HtmlData buildHtmlData(ThreadRowInfo row) {
        HtmlData htmlData = new HtmlData(row.getContent());
        htmlData.setAlertInfo(row.getAlterinfo());
        htmlData.setDarkMode(ThemeManager.getInstance().isNightMode());
        htmlData.setInBackList(row.get_isInBlackList());
        htmlData.setTextSize(PhoneConfiguration.getInstance().getTopicContentSize());
        htmlData.setEmotionSize(PhoneConfiguration.getInstance().getEmoticonSize());
        htmlData.setSignature(PhoneConfiguration.getInstance().isShowSignature() ? row.getSignature() : null);
        htmlData.setVote(row.getVote());
        htmlData.setSubject(row.getSubject());
        htmlData.setShowImage(PhoneConfiguration.getInstance().isDownImgNoWifi()
                || DeviceUtils.isWifiConnected(ContextUtils.getContext()));
        htmlData.setNGAHost(Utils.getNGAHost());
        if (row.getAttachs() != null) {
            List<AttachmentData> attachments = new ArrayList<>();
            for (Map.Entry<String, Attachment> entry : row.getAttachs().entrySet()) {
                AttachmentData data = new AttachmentData();
                data.setAttachUrl(entry.getValue().getAttachurl());
                data.setThumb(entry.getValue().getThumb());
                data.setAttachmentHost(HttpUtil.NGA_ATTACHMENT_HOST);
                attachments.add(data);
            }
            htmlData.setAttachmentList(attachments);
        }

        if (row.getComments() != null) {
            List<CommentData> comments = new ArrayList<>();
            for (ThreadRowInfo value : row.getComments()) {
                CommentData comment = new CommentData();
                comment.setAuthor(value.getAuthor());
                comment.setContent(value.getContent());
                comment.setPostTime(value.getPostdate());
                comment.setAvatarUrl(FunctionUtils.parseAvatarUrl(value.getJs_escap_avatar()));
                comments.add(comment);
            }
            htmlData.setCommentList(comments);
        }
        return htmlData;
    }

    private static void buildRowVote(ThreadRowInfo row, JSONObject rowObj) {
        String vote = rowObj.getString("vote");
        if (!StringUtils.isEmpty(vote)) {
            row.setVote(vote);
        }
    }

    //热门回复
    private static void buildRowHotReplay(ThreadRowInfo row, JSONObject rowObj) {
        String hotObj = rowObj.getString("17");
        if (hotObj != null) {
            row.hotReplies = new ArrayList<>();
            String[] hots = hotObj.split(",");
            for (String hot : hots) {
                if (!TextUtils.isEmpty(hot)) {
                    row.hotReplies.add(hot);
                }
            }
        }
    }

    //解析贴条
    private static void buildRowComment(ThreadRowInfo row, JSONObject rowObj, JSONObject userInfoMap) {
        JSONObject commObj = (JSONObject) rowObj.get("comment");
        if (commObj != null) {
            row.setComments(convertJsObjToList(commObj, commObj.size(), userInfoMap));
        }
    }

    private static void buildRowClientInfo(ThreadRowInfo row, JSONObject rowObj) {
        String client = rowObj.getString("from_client");
        if (!StringUtils.isEmpty(client)) {
            row.setFromClient(client);
            if (!client.trim().equals("")) {
                String clientAppCode;
                if (client.contains(" ")) {
                    clientAppCode = client.substring(0, client.indexOf(' '));
                } else {
                    clientAppCode = client;
                }
                if (clientAppCode.equals("1") || clientAppCode.equals("7") || clientAppCode.equals("101")) {
                    row.setFromClientModel("ios");
                } else if (clientAppCode.equals("103") || clientAppCode.equals("9")) {
                    row.setFromClientModel("wp");
                } else if (!clientAppCode.equals("8") && !clientAppCode.equals("100")) {
                    row.setFromClientModel("unknown");
                } else {
                    row.setFromClientModel("android");
                }
            }
        }
    }

    private static void buildRowUserInfo(ThreadRowInfo row, JSONObject userInfoMap) {
        if (row.getAuthorid() == 0) {
            return;
        }
        JSONObject userInfo = (JSONObject) userInfoMap.get(String.valueOf(row
                .getAuthorid()));
        JSONObject groupObj = userInfoMap.getJSONObject("__GROUPS");

        if (userInfo == null) {
            return;
        }
        int uid = row.getAuthorid();
        row.set_IsInBlackList(UserManagerImpl.getInstance().checkBlackList(String.valueOf(uid)));
        if (userInfo.getString("username").length() == 39
                && userInfo.getString("username").startsWith("#anony_")) {
//            StringBuilder builder = new StringBuilder();
//            String username = userInfo.getString("username");
//            int i = 6;
//            for (int j = 0; j < 6; j++) {
//                int pos;
//                if (j == 0 || j == 3) {
//                    pos = Integer.valueOf(username.substring(i + 1, i + 2), 16);
//                    builder.append(t1.charAt(pos));
//                } else {
//                    pos = Integer.valueOf(username.substring(i, i + 2), 16);
//                    builder.append(t2.charAt(pos));
//                }
//                i += 2;
//            }
            row.setAuthor(TopicConvertFactory.getAnonymityName(userInfo.getString("username")));
            row.setISANONYMOUS(true);
        } else {
            row.setAuthor(userInfo.getString("username"));
        }
        row.setJs_escap_avatar(userInfo.getString("avatar"));
        row.setYz(userInfo.getString("yz"));
        row.setMuteTime(userInfo.getString("mute_time"));
        try {
            row.setAurvrc(Integer.parseInt(userInfo.getString("rvrc")));
        } catch (Exception e) {
            row.setAurvrc(0);
        }
        row.setSignature(userInfo.getString("signature"));

        try {
            row.setPostCount(userInfo.getString("postnum"));
            row.setReputation(Float.parseFloat(userInfo.getString("rvrc")) / 10.0f);
            row.setMemberGroup(groupObj.getJSONObject(userInfo.getString("memberid")).getString("0"));
        } catch (Exception ignored) {
        }

        JSONObject obj = userInfo.getJSONObject("buffs");
        if (obj != null) {
            for (String id : ForumConstants.BUFF_MUTE_IDS) {
                if (obj.containsKey(id)) {
                    row.setMuted(true);
                    break;
                }
            }
        }
    }




//    public static double rnd(DiceData arg) {
//        double seed = arg.getSeed();
//        if (arg.getArgsId() != null) {
//            if (arg.getRndSeed() == 0.0) {
//                arg.setRndSeed(arg.getAuthorId() + arg.gettId() + arg.getpId() +
//                        (arg.gettId() > 10246184 || arg.getpId() > 200188932 ? arg.getSeedOffset() : 0));
//                if (arg.getRndSeed() == 0.0) arg.setRndSeed(Math.floor(Math.random() * 10000));
//            }
//            arg.setRndSeed((arg.getRndSeed() * 9301 + 49297) % 233280);
//            return arg.getRndSeed() / 233280.0;
//        }
//        seed = (seed * 9301 + 49297) % 233280;
//        arg.setSeed(seed);
//        return seed / 233280.0;
//    }

    // 计算掷骰子结果
    public static String getRealDice(DiceData arg) {
        String reg = "\\[dice].+?\\[/dice\\]";
        int sum = 0;
        String txt = arg.getTxt();
        Pattern r = Pattern.compile(reg);
        Matcher m = r.matcher(txt);
        if (!m.find()) return txt;
        do {
            StringBuilder diceStr = new StringBuilder();
            String $0 = m.group(0);
            assert $0 != null;
            String $1 = $0.replace("[dice]", "").replace("[/dice]", "");
            String rr = $1;
            $1 = "+" + $1;
            String[] strs = $1.split("\\+");
            StringBuilder rx = new StringBuilder();
            for (String str : strs) {
                if (str.length() > 0) {
                    String[] sstrs = str.split("d");
                    int num;
                    int covers;
                    if (sstrs.length > 1) {
                        if (sstrs[0].length() > 0) {
                            try {
                                num = parseInt(sstrs[0],1);
                            }catch (Exception e){
                                num = 1;
                            }
                        } else {
                            num = 1;
                        }
                        covers = parseInt(sstrs[1],0);
                        if (num > 10 || covers > 100000) {
                            sum = -1;
                            diceStr.append("+OUT OF LIMIT");
                        }
                        for (int j = 0; j < num; j++) {
                            String argsId = "postcomment__510458140";
                            arg.setArgsId(argsId);
                            double a = rnd(arg);
                            double rand = Math.floor(a * covers) + 1;
                            rx.append("+d").append(covers).append("(").append(Math.round(rand)).append(")");
                            if (sum != -1) sum += rand;
                        }
                    } else {
                        covers =parseInt(sstrs[0].trim(),0);
                        sum += covers;
                        rx.append("+").append(covers);
                    }
                }
            }
            diceStr.append("<p><b>ROLL:").append(rr).append("</b>=").append(rx.substring(1)).append("=<b>").append(sum).append("</b></p>");
            sum = 0;
            txt = txt.replaceFirst(reg, diceStr.toString());
            m = r.matcher(txt);
        } while (m.find());
        return txt;
    }

    private static int parseInt(String str,int defaultWhenFailed){
        try {
            return Integer.parseInt(str);
        }catch (Exception e){
            return defaultWhenFailed;
        }
    }

}
