package sp.phone.common;

import android.content.Context;

import java.util.List;

import sp.phone.http.bean.ThreadData;

public interface UserManager {

    int getUserSize();

    User getActiveUser();

    void initialize(Context context);

    int getActiveUserIndex();

    List<User> getUserList();

    boolean hasValidUser();

    void setActiveUser(int index);

    int toggleUser(boolean isNext);

    void addUser(User user);

    void addUser(String uid, String cid, String name, String replyString, int replyTotalNum);

    void removeUser(int index);

    void swapUser(int from, int to);

    // User 类辅助接口

    String getCookie();

    String getUserId();

    String getCid();

    String getUserName();

    void setAvatarUrl(int userId, String url);

    // 被喷

    int getReplyCount();

    String getReplyString();

    void setReplyString(int count, String replyString);


    // 黑名单

    void addToBlackList(String authorName, String authorId);

    void addToBlackList(User user);

    void removeFromBlackList(String authorId);

    boolean checkBlackList(String authorId);

    List<User> getBlackList();

    void removeAllBlackList();

    void putAvatarUrl(String uid, String url);

    void putAvatarUrl(ThreadData info);

    String getAvatarUrl(String uid);

    void clearAvatarUrl();

}
