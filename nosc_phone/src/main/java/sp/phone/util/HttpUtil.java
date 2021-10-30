package sp.phone.util;


import android.graphics.Bitmap;
import android.os.Build;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

public class HttpUtil {

    public final static String PATH_OLD = android.os.Environment.getExternalStorageDirectory().getPath() + "/nga_cache";
    public static final String NGA_ATTACHMENT_HOST = "img.nga.178.com"; //img.ngacn.cc";
    public static final String Servlet_phone = "/servlet/PhoneServlet";
    public static final String Servlet_timer = "/servlet/TimerServlet";
    private static final String[] servers = {"http://nga.178.com", "http://bbs.ngacn.cc"};
    private static final String TAG = HttpUtil.class.getSimpleName();
    public static String PATH_AVATAR_OLD = PATH_OLD + "/nga_cache";
    public static String PATH_IMAGES = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures";
    public static String PATH = android.os.Environment.getExternalStorageDirectory().getPath() + "/nga_cache";
    public static String PATH_AVATAR = PATH + "/nga_cache";
    public static String PATH_NOMEDIA = PATH + "/.nomedia";

    public static String Server = "http://bbs.nga.cn";
    public static String NonameServer = "http://ngac.sinaapp.com/nganoname";
    public static String HOST = "";
    public static String HOST_PORT = "";
    //软件名/版本 (硬件信息; 操作系统信息)
    //AndroidNga/571 (Xiaomi MI 2S; Android 4.1.1)
    public static String MODEL = android.os.Build.MODEL.toUpperCase(Locale.US);
    public static String MANUFACTURER = android.os.Build.MANUFACTURER.toUpperCase(Locale.US);


    public static void switchServer() {
        int i = 0;
        for (; i < servers.length; ++i) {
            if (Server.equals(servers[i]))
                break;
        }
        i = (i + 1) % servers.length;
        Server = servers[i];
    }

    public static void downImage(String uri, String fileName) {
        try {
            URL url = new URL(uri);
            File file = new File(fileName);

            FileUtils.copyURLToFile(url, file, 2000, 5000);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            NLog.e(TAG, "failed to download img:" + uri + "," + e.getMessage());
        }
    }

    public static void downImage3(Bitmap bitmap, String fileName) {
        File f = new File(fileName);
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static String getHtml(String uri, String cookie) {
        InputStream is = null;
        String machine = "";
        if (MODEL.contains(MANUFACTURER)) {
            machine = android.os.Build.MODEL;
        } else {
            machine = android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL;
        }
        if (machine.length() < 19) {
            machine = "[" + machine + "]";
        }
        final String USER_AGENT = "Nga_Official/" + 573 + "(" + machine + ";Android" + Build.VERSION.RELEASE + ")";
        try {
            URL url = new URL(uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (!StringUtils.isEmpty(cookie))
                conn.setRequestProperty("Cookie", cookie);
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setRequestProperty("Accept-Charset", "GBK");
            conn.setRequestProperty("Connection", "close");
            conn.setRequestProperty("Accept-Encoding", "gzip,deflate");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);
            conn.connect();
            if (conn.getResponseCode() == 200)
                is = conn.getInputStream();
            else
                is = conn.getErrorStream();
            if ("gzip".equals(conn.getHeaderField("Content-Encoding")))
                is = new GZIPInputStream(is);
            String encoding = getCharset(conn, "GBK");
            return IOUtils.toString(is, encoding);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(is);
        }
        return null;
    }

    public static String getCharset(HttpURLConnection conn, String defaultValue) {
        if (conn == null)
            return defaultValue;
        String contentType = conn.getHeaderField("Content-Type");
        if (StringUtils.isEmpty(contentType))
            return defaultValue;
        String startTag = "charset=";
        String endTag = " ";
        int start = contentType.indexOf(startTag);
        if (-1 == start)
            return defaultValue;
        start += startTag.length();
        int end = contentType.indexOf(endTag, start);
        if (-1 == end)
            end = contentType.length();
        if (contentType.substring(start, end).equals("no")) {
            return "utf-8";
        }
        return contentType.substring(start, end);
    }
}
