package gov.anzong.androidnga.debug;

import android.os.Environment;

import org.apache.commons.io.FileUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.File;
import java.io.IOException;

import gov.anzong.androidnga.base.util.ToastUtils;
import okhttp3.Request;

/**
 * @author Justwen
 */
public class Debugger {

    private static boolean sDebugMode;

    public static void collectBody(String body) {
        if (!sDebugMode) {
            return;
        }
        try {
            JSONObject obj = JSONObject.parseObject(body);
            File debugFile = new File(Environment.getExternalStorageDirectory() + "/gov.anzong.androidnga/debug/body.json");
            FileUtils.write(debugFile, JSON.toJSONString(obj, SerializerFeature.PrettyFormat,SerializerFeature.WriteMapNullValue,
                    SerializerFeature.WriteDateUseDateFormat) + "\n\n", true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void collectRequest(Request request) {
        if (!sDebugMode) {
            return;
        }
        try {
            File debugFile = new File(Environment.getExternalStorageDirectory() + "/gov.anzong.androidnga/debug/request.json");
            FileUtils.write(debugFile, request + "\n\n", true);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void setDebugMode(boolean debugMode) {
        sDebugMode = debugMode;
        ToastUtils.flat(sDebugMode ? "调试模式开启" : "调试模式关闭");
    }

    public static void toggleDebugMode() {
        setDebugMode(!sDebugMode);
    }
}