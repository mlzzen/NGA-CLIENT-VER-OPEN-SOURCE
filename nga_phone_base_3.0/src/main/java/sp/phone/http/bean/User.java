package sp.phone.http.bean;

public class User {

    private String userId;
    private String nickName;
    private String cid;
    private String replyString;
    private int totalReplyNum;
    private String blacklist = "";

    public String getBlackList() {
        return blacklist;
    }

    public void setBlackList(String blacklist) {
        this.blacklist = blacklist;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getTotalReplyNum() {
        return totalReplyNum;
    }

    public void setTotalReplyNum(int totalReplyNum) {
        this.totalReplyNum = totalReplyNum;
    }

    public String getReplyString() {
        return replyString;
    }

    public void setReplyString(String replyString) {
        this.replyString = replyString;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }


}
