package sp.phone.util;

import android.annotation.SuppressLint;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

import gov.anzong.androidnga.R;
import nosc.utils.ContextUtils;
import nosc.api.bean.StringFindResult;
import okhttp3.RequestBody;
import okio.Buffer;
;

@SuppressLint("SimpleDateFormat")
public class StringUtils {
    public final static String key = "asdfasdf";

    private static final String[] SAYING = ContextUtils.getResources().getStringArray(R.array.saying);

//    /**
//     * 验证是否是邮箱
//     */
//    public static boolean isEmail(String email) {
//        if (isEmpty(email))
//            return false;
//        String pattern1 = "^([a-z0-9A-Z]+[-_|\\.]?)+[a-z0-9A-Z_]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
//        Pattern pattern = Pattern.compile(pattern1);
//        Matcher mat = pattern.matcher(email);
//        return mat.find();
//    }

    private static Map<String, Pattern> sPatternMap = new WeakHashMap<>();

    public static String replaceAll(String content, String regex, String replacement) {
        return getPattern(regex).matcher(content).replaceAll(replacement);
    }

    public static Pattern getPattern(String regex) {
        Pattern pattern = sPatternMap.get(regex);
        if (pattern == null) {
            pattern = Pattern.compile(regex);
            sPatternMap.put(regex, pattern);
        }
        return pattern;
    }
    public static String requestBody2String(final RequestBody request) {
        try {
            final Buffer buffer = new Buffer();
            if (request != null) {
                request.writeTo(buffer);
                return buffer.readUtf8();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 判断是否是 "" 或者 null
     */
    public static boolean isEmpty(String str) {
        return str == null || "".equals(str);
    }

    /* 给候总客户端乱码加适配 */
    public static String unescape(String src) {
        if (isEmpty(src))
            return "";
        StringBuilder tmp = new StringBuilder();
        tmp.ensureCapacity(src.length());
        int lastPos = 0, pos = 0;
        char ch;
        String patternStr = "[A-Fa-f0-9]{4}";
        while (lastPos < src.length()) {
            pos = src.indexOf("%", lastPos);
            if (pos == lastPos) {
                if (pos > src.length() - 3) {
                    tmp.append(src.substring(pos));
                    lastPos = pos + 3;
                } else {
                    if (src.charAt(pos + 1) == 'u') {
                        try {
                            String substring = src.substring(pos + 2, pos + 6);
                            if (Pattern.matches(patternStr,
                                    substring)) {
                                ch = (char) Integer.parseInt(
                                        substring, 16);
                                tmp.append(ch);
                                lastPos = pos + 6;
                            } else {
                                tmp.append(src.substring(pos, pos + 3));
                                lastPos = pos + 3;
                            }
                        } catch (Exception e) {
                            tmp.append(src.substring(pos, pos + 3));
                            lastPos = pos + 3;
                        }

                    } else {
                        try {
                            ch = (char) Integer.parseInt(
                                    src.substring(pos + 1, pos + 3), 16);
                            tmp.append(ch);
                            lastPos = pos + 3;
                        } catch (Exception e) {
                            tmp.append(src.substring(pos, pos + 3));
                            lastPos = pos + 3;
                        }
                    }
                }
            } else {
                if (pos == -1) {
                    tmp.append(src.substring(lastPos));
                    lastPos = src.length();
                } else {
                    tmp.append(src.substring(lastPos, pos));
                    lastPos = pos;
                }
            }
        }
        return tmp.toString();
    }


    public static String encodeUrl(final String s, final String charset) {

        /*
         * try { return java.net.URLEncoder.encode(s,charset); // this not work
         * in android 4.4 if a english char is followed //by a Chinese character
         *
         * } catch (UnsupportedEncodingException e) {
         *
         * return ""; }
         */
        // NLog.i("111111", s+"----->"+ret);
        return UriEncoderWithCharset.encode(s, null, charset);
    }

    public static String removeBrTag(String s) {
        s = s.replaceAll("<br/><br/>", "\n");
        s = s.replaceAll("<br/>", "\n");
        return s;
    }

    public static String getSaying() {
        Random random = new Random();
        int num = random.nextInt(SAYING.length);
        return SAYING[num];
    }

    public static String unEscapeHtml(String s) {
        return StringHelper.unescapeHTML(s);
    }

    public static StringFindResult getStringBetween(String data, int begPosition, String startStr, String endStr) {
        StringFindResult ret = new StringFindResult();
        do {
            if (isEmpty(data) || begPosition < 0
                    || data.length() <= begPosition || isEmpty(startStr)
                    || isEmpty(startStr))
                break;

            int start = data.indexOf(startStr, begPosition);
            if (start == -1)
                break;

            start += startStr.length();
            int end = data.indexOf(endStr, start);
            if (end == -1)
                end = data.length();
            ret.result = data.substring(start, end);
            ret.position = end + endStr.length();

        } while (false);

        return ret;
    }

    public static String toBinaryArray(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * Byte.SIZE);
        for (int i = 0; i < Byte.SIZE * bytes.length; i++) {
            builder.append((bytes[i / Byte.SIZE] << i % Byte.SIZE & 0x80) == 0 ? '0' : '1');
        }
        return builder.toString();
    }

    public static int getUrlParameter(String url, String paraName) {
        if (StringUtils.isEmpty(url)) {
            return 0;
        }
        final String pattern = paraName + "=";
        int start = url.indexOf(pattern);
        if (start == -1)
            return 0;
        start += pattern.length();
        int end = url.indexOf("&", start);
        if (end == -1)
            end = url.length();
        String value = url.substring(start, end);
        int ret = 0;
        try {
            ret = Integer.parseInt(value);
        } catch (Exception e) {
            NLog.e("getUrlParameter", "invalid url:" + url);
        }

        return ret;
    }

    public static String timeStamp2Date1(String timeStamp) {
        return timeStamp2Date(timeStamp, "yyyy-MM-dd HH:mm:ss");
    }

    public static String timeStamp2Date2(String timeStamp) {
        return timeStamp2Date(timeStamp, "MM-dd HH:mm");
    }

    public static String timeStamp2Date(String timeStamp, String format) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(timeStamp) * 1000);
        return new SimpleDateFormat(format, Locale.getDefault()).format(calendar.getTime());
    }


}