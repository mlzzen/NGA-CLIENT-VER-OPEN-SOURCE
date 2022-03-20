package sp.phone.rxjava;

/**
 * Created by Justwen on 2017/11/25.
 */

public class RxEvent {
    public static final int EVENT_ARTICLE_GO_FLOOR = 3;

    public static final int EVENT_INSERT_EMOTICON = 5;

    public static final int EVENT_SHOW_TOPIC_LIST = 6;

    public int what;

    public int arg;

    public Object obj;

    public RxEvent(int what, Object obj) {
        this.what = what;
        this.obj = obj;
    }

    public RxEvent(int what, int arg, Object obj) {
        this.what = what;
        this.arg = arg;
        this.obj = obj;
    }
}
