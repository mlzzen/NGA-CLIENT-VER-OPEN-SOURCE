package sp.phone.common;

import android.content.Context;
import android.preference.PreferenceManager;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.List;

import gov.anzong.androidnga.NgaClientApp;
import nosc.utils.ContextUtils;
import nosc.utils.PreferenceKey;
import sp.phone.mvp.model.entity.ThreadPageInfo;

/**
 * Created by Justwen on 2018/1/17.
 */

public class TopicHistoryManager {


    private List<ThreadPageInfo> mTopicList;

    private static final int MAX_HISTORY_TOPIC_COUNT = 40;

    private static class SingleTonHolder {

        private static final TopicHistoryManager sInstance = new TopicHistoryManager();
    }

    public static TopicHistoryManager getInstance() {
        return SingleTonHolder.sInstance;
    }

    private TopicHistoryManager() {
        String topicStr = PreferenceManager.getDefaultSharedPreferences(NgaClientApp.Companion.getInst()).getString(PreferenceKey.KEY_TOPIC_HISTORY, null);
        if (topicStr != null) {
            mTopicList = JSON.parseArray(topicStr, ThreadPageInfo.class);
        }
        if (mTopicList == null) {
            mTopicList = new ArrayList<>();
        }
    }

    public void addTopicHistory(ThreadPageInfo topic) {
        if (mTopicList.contains(topic)) {
            mTopicList.remove(topic);
        } else if (mTopicList.size() >= MAX_HISTORY_TOPIC_COUNT){
            mTopicList.remove(mTopicList.size() - 1);
        }
        mTopicList.add(0,topic);
        commit();
    }

    public void removeTopicHistory(ThreadPageInfo item) {
        mTopicList.remove(item);
        commit();
    }

    public List<ThreadPageInfo> getTopicHistoryList() {
        return mTopicList;
    }

    public void removeAllTopicHistory() {
        mTopicList.clear();
        commit();
    }

    private void commit() {
        String topicStr = JSON.toJSONString(mTopicList);
        PreferenceManager.getDefaultSharedPreferences(NgaClientApp.Companion.getInst())
                .edit()
                .putString(PreferenceKey.KEY_TOPIC_HISTORY,topicStr)
                .apply();
    }

}
