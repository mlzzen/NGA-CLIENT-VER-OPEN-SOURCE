package sp.phone.mvp.model.convert;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import sp.phone.common.FilterKeyword;
import sp.phone.common.FilterKeywordsManagerImpl;
import sp.phone.common.PhoneConfiguration;
import sp.phone.common.User;
import sp.phone.common.UserManagerImpl;
import nosc.api.bean.TopicListBean;
import sp.phone.mvp.model.entity.SubBoard;
import sp.phone.mvp.model.entity.ThreadPageInfo;
import sp.phone.mvp.model.entity.TopicListInfo;
import sp.phone.util.ForumUtils;
import sp.phone.util.NLog;
import sp.phone.util.StringUtils;

/**
 * Created by Justwen on 2017/11/21.
 */

public class TopicConvertFactory {

    private static final String TAG = TopicConvertFactory.class.getSimpleName();

    public TopicListInfo getTopicListInfo(String js, int page) {

        if (js.startsWith("window.script_muti_get_var_store=")) {
            js = js.substring("window.script_muti_get_var_store=".length());
        }

        TopicListBean topicListBean = JSON.parseObject(js, TopicListBean.class);

        try {
            TopicListInfo listInfo = new TopicListInfo();
            convertSubBoard(listInfo, topicListBean);
            convertTopic(listInfo, topicListBean, page);
            listInfo.curTime = topicListBean.getTime();
            sort(listInfo);
            filter(listInfo);
            return listInfo;
        } catch (NullPointerException e) {
            NLog.e(TAG, "can not parse :\n" + js);
            return null;
        }

    }

    private void filter(TopicListInfo data) {

//        // 低版本android 没有stream方法
//        // TODO: 如果第一页全部都是被屏蔽的，可能会认为加载失败
//        data.setThreadPageList(
//                data.getThreadPageList().stream().filter((ThreadPageInfo threadPageInfo) -> {
//                    return FilterKeywordsManagerImpl
//                            .getInstance()
//                            .getKeywords()
//                            .parallelStream()
//                            .noneMatch(filterKeyword -> {
//                                if (filterKeyword.isEnabled()) {
//                                    return threadPageInfo
//                                            .getSubject()
//                                            .contains(filterKeyword.getKeyword());
//                                } else {
//                                    return false;
//                                }
//                            });
//                }).collect(Collectors.toList())
//               );

        Iterator<ThreadPageInfo> iterator = data.getThreadPageList().iterator();

        List<User> blackList = UserManagerImpl.getInstance().getBlackList();
        List<FilterKeyword> filterKeywords = FilterKeywordsManagerImpl.getInstance().getKeywords();

        while (iterator.hasNext()) {
            ThreadPageInfo pageInfo = iterator.next();
            boolean removed = false;
            for (FilterKeyword keyword : filterKeywords) {
                if (keyword.isEnabled() && pageInfo.getSubject().contains(keyword.getKeyword())) {
                    iterator.remove();
                    removed = true;
                    break;
                }
            }
            if (removed) {
                continue;
            }
            for (User user : blackList) {
                if (Objects.equals(pageInfo.getAuthor(), user.getNickName())) {
                    iterator.remove();
                    break;
                }
            }

        }
    }

    private void sort(TopicListInfo listInfo) {
        List<ThreadPageInfo> list = listInfo.getThreadPageList();
        if (PhoneConfiguration.getInstance().needSortByPostOrder()) {
            Collections.sort(list, new Comparator<ThreadPageInfo>() {
                @Override
                public int compare(ThreadPageInfo o1, ThreadPageInfo o2) {
                    return o1.getPostDate() < o2.getPostDate() ? 1 : -1;
                }
            });
        }

        List<SubBoard> subBoards = listInfo.getSubBoardList();
        if (!subBoards.isEmpty()) {
            Collections.sort(subBoards, new Comparator<SubBoard>() {
                @Override
                public int compare(SubBoard o1, SubBoard o2) {
                    return o1.getFid() < o2.getFid() ? 1 : -1;
                }
            });
        }
    }

    private void convertSubBoard(TopicListInfo listInfo, TopicListBean topicListBean) {
        try {
            String subForumsStr = String.valueOf(topicListBean.getData().get__F().getSub_forums());
            if (TextUtils.isEmpty(subForumsStr)) {
                return;
            }
            Map<String, Map<String, String>> subBoardMap = JSON.parseObject(subForumsStr, Map.class);
            for (String key : subBoardMap.keySet()) {
                Map<String, String> boardMap = subBoardMap.get(key);
                SubBoard board = new SubBoard();
                Object obj = boardMap.get("0");
                if (key.startsWith("t")) {
                    board.setStid(Integer.parseInt(obj.toString()));
                } else {
                    board.setFid(Integer.parseInt(obj.toString()));
                }

                // 有些子版块的fid的key是3，大部分都是1
                if (boardMap.containsKey("3")) {
                    obj = boardMap.get("3");
                    board.setTidStr(obj.toString());
                    board.setType(1);
                } else {
                    board.setType(0);
                }
                board.setParentFidStr(String.valueOf(topicListBean.getData().get__F().getFid()));
                board.setName(boardMap.get("1"));
                board.setDescription(boardMap.get("2"));
                if (boardMap.containsKey("4")) {
                    obj = boardMap.get("4");
                    board.setChecked(ForumUtils.isBoardSubscribed(Integer.parseInt(obj.toString())));
                } else {
                    board.setType(-1);
                    board.setChecked(true);
                }
                listInfo.addSubBoard(board);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void convertTopic(TopicListInfo listInfo, TopicListBean topicListBean, int page) {
        Map<String, TopicListBean.DataBean.TBean> map = topicListBean.getData().get__T();
        int count = 0;
        while (count < map.size()) {
            String key = String.valueOf(count);
            TopicListBean.DataBean.TBean tBean = map.get(key);
            if (tBean == null || filterTopic(listInfo, topicListBean, tBean)) {
                count++;
                continue;
            }
            ThreadPageInfo pageInfo = new ThreadPageInfo();
            String author = tBean.getAuthor();
            if (author.startsWith("#anony_")) {
                pageInfo.setAnonymity(true);
                pageInfo.setAuthor(getAnonymityName(tBean.getAuthor()));
            } else {
                pageInfo.setAuthorId(Integer.parseInt(tBean.getAuthorid()));
                pageInfo.setAuthor(tBean.getAuthor());
            }
            pageInfo.setLastPoster(tBean.getLastposter());
            pageInfo.setSubject(tBean.getSubject());
            pageInfo.setReplies(tBean.getReplies());
            pageInfo.setType(tBean.getType());
            pageInfo.setTopicMisc(tBean.getTopic_misc());
            pageInfo.setTitleFont(tBean.getTitlefont());
            int tid = tBean.getTid();
            String tpcUrl = tBean.getTpcurl();
            if (tpcUrl != null && tpcUrl.contains("tid")) {
                tid = StringUtils.getUrlParameter(tpcUrl, "tid");
            }
            pageInfo.setTid(tid);
            pageInfo.setPage(page);
            TopicListBean.DataBean.TBean.PBean pBean = tBean.get__P();
            if (pBean != null) {
                pageInfo.setPid(pBean.getPid());
                ThreadPageInfo.ReplyInfo replyInfo = new ThreadPageInfo.ReplyInfo();
                replyInfo.setAuthorId(pBean.getAuthorid());
                replyInfo.setContent(pBean.getContent());
                replyInfo.setPostDate(String.valueOf(pBean.getPostdate()));
                replyInfo.setPidStr(String.valueOf(pBean.getPid()));
                replyInfo.setTidStr(String.valueOf(pageInfo.getTid()));
                replyInfo.setSubject(pageInfo.getSubject());
                pageInfo.setReplyInfo(replyInfo);
            }

            Map<String, String> parent = tBean.getParent();
            if (parent != null) {
                pageInfo.setBoard(parent.get("2"));
            }

            pageInfo.setPostDate(tBean.getPostdate());

            Map<String, String> topicMiscVar = tBean.topic_misc_var;
            if (topicMiscVar != null && pageInfo.isMirrorBoard()) {
                Object obj = topicMiscVar.get("3");
                if (obj != null) {
                    pageInfo.setFid(Integer.parseInt(obj.toString()));
                }
            }


            listInfo.addThreadPage(pageInfo);
            count++;
        }
    }

    private boolean filterTopic(TopicListInfo listInfo, TopicListBean topicListBean, TopicListBean.DataBean.TBean tBean) {
        if (topicListBean.getData().get__F() != null
                && PhoneConfiguration.getInstance().needFilterSubBoard()
                && topicListBean.getData().get__F().getFid() == -7
                && tBean.getRecommend() > 9) {
            NLog.d("屏蔽固定的渣帖子 " + tBean.toString());
            return true;
        } else {
            return false;
        }

    }

    public static String getAnonymityName(String author) {
        String prefix = "甲乙丙丁戊己庚辛壬癸子丑寅卯辰巳午未申酉戌亥";
        String suffix = "王李张刘陈杨黄吴赵周徐孙马朱胡林郭何高罗郑梁谢宋唐许邓冯韩曹曾彭萧蔡潘田董袁于余叶蒋杜苏魏程吕丁沈任姚卢傅钟姜崔谭廖范汪陆金石戴贾韦夏邱方侯邹熊孟秦白江阎薛尹段雷黎史龙陶贺顾毛郝龚邵万钱严赖覃洪武莫孔汤向常温康施文牛樊葛邢安齐易乔伍庞颜倪庄聂章鲁岳翟殷詹申欧耿关兰焦俞左柳甘祝包宁尚符舒阮柯纪梅童凌毕单季裴霍涂成苗谷盛曲翁冉骆蓝路游辛靳管柴蒙鲍华喻祁蒲房滕屈饶解牟艾尤阳时穆农司卓古吉缪简车项连芦麦褚娄窦戚岑景党宫费卜冷晏席卫米柏宗瞿桂全佟应臧闵苟邬边卞姬师和仇栾隋商刁沙荣巫寇桑郎甄丛仲虞敖巩明佘池查麻苑迟邝";

        StringBuilder sb = new StringBuilder();
        int i = 6;
        for (int j = 0; j < 6; j++) {
            int pos;
            if (j == 0 || j == 3) {
                pos = Integer.valueOf(author.substring(i + 1, i + 2), 16);
                if (pos >= prefix.length()) {
                    pos = prefix.length() - 1;
                } else if (pos < 0) {
                    pos = 0;
                }
                sb.append(prefix.charAt(pos));
            } else {
                pos = Integer.valueOf(author.substring(i, i + 2), 16);
                if (pos >= suffix.length()) {
                    pos = suffix.length() - 1;
                } else if (pos < 0) {
                    pos = 0;
                }
                sb.append(suffix.charAt(pos));
            }
            i += 2;
        }
        return sb.toString();
    }
}
