package gov.anzong.androidnga.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import gov.anzong.androidnga.R
import gov.anzong.androidnga.arouter.ARouterConstants
import gov.anzong.androidnga.fragment.TopicFavoriteFragment
import gov.anzong.androidnga.fragment.TopicListFragment
import nosc.api.model.BoardModel.getBoardName
import sp.phone.param.ParamKey
import sp.phone.param.TopicListParam
import sp.phone.util.ActivityUtils
import sp.phone.util.StringUtils

/**
 * 帖子列表
 */
@Route(path = ARouterConstants.ACTIVITY_TOPIC_LIST)
class TopicListActivity : BaseActivity() {
    private var mRequestParam: TopicListParam? = null
    private val requestParam: TopicListParam?
        get() {
            val bundle = intent.extras
            val url = intent.dataString
            var requestParam: TopicListParam? = null
            if (url != null) {
                requestParam = TopicListParam()
                requestParam.authorId = StringUtils.getUrlParameter(url, "authorid")
                requestParam.searchPost = StringUtils.getUrlParameter(url, "searchpost")
                requestParam.favor = StringUtils.getUrlParameter(url, "favor")
                requestParam.key = StringUtils.getStringBetween(url, 0, "key=", "&").result
                requestParam.author = StringUtils.getStringBetween(url, 0, "author=", "&").result
                requestParam.fidGroup =
                    StringUtils.getStringBetween(url, 0, "fidgroup=", "&").result
                requestParam.content = StringUtils.getUrlParameter(url, "content")
                requestParam.fid = StringUtils.getUrlParameter(url, "fid")
                requestParam.stid = StringUtils.getUrlParameter(url, "stid")
            } else if (bundle != null) {
                requestParam = bundle.getParcelable(ParamKey.KEY_PARAM)
                if (requestParam == null) {
                    requestParam = TopicListParam()
                    requestParam.fid = bundle.getInt(ParamKey.KEY_FID, 0)
                    requestParam.authorId = bundle.getInt(ParamKey.KEY_AUTHOR_ID, 0)
                    requestParam.content = bundle.getInt(ParamKey.KEY_CONTENT, 0)
                    requestParam.searchPost = bundle.getInt(ParamKey.KEY_SEARCH_POST, 0)
                    requestParam.favor = bundle.getInt(ParamKey.KEY_FAVOR, 0)
                    requestParam.key = bundle.getString(ParamKey.KEY_KEY)
                    requestParam.author = bundle.getString(ParamKey.KEY_AUTHOR)
                    requestParam.fidGroup = bundle.getString(ParamKey.KEY_FID_GROUP)
                    requestParam.title = bundle.getString(ParamKey.KEY_TITLE)
                    requestParam.recommend = bundle.getInt(ParamKey.KEY_RECOMMEND, 0)
                    requestParam.twentyfour = bundle.getInt(ParamKey.KEY_TWENTYFOUR, 0)
                    requestParam.stid = bundle.getInt(ParamKey.KEY_STID, 0)
                    requestParam.boardHead = bundle.getString(ParamKey.BOARD_HEAD, null)
                }
            }
            if (requestParam != null && TextUtils.isEmpty(requestParam.title)) {
                requestParam.title = getBoardName(requestParam.fid, requestParam.stid)
            }
            return requestParam
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        setToolbarEnabled(true)
        mRequestParam = requestParam
        super.onCreate(savedInstanceState)
        if (mRequestParam != null) {
            setupFragment()
        } else {
            finish()
        }
    }

    private fun setupFragment() {
        val fm = supportFragmentManager
        if (fm.findFragmentById(android.R.id.content) == null) {
            val fragment: Fragment = if (mRequestParam!!.favor != 0) {
                TopicFavoriteFragment()
            } else {
                TopicListFragment()
            }
            val bundle = Bundle()
            bundle.putParcelable(ParamKey.KEY_PARAM, mRequestParam)
            fragment.arguments = bundle
            fm.beginTransaction().replace(android.R.id.content, fragment).commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.menu_favorite -> ActivityUtils.startFavoriteTopicActivity(this)
            R.id.menu_recommend -> showRecommendTopicList()
            R.id.menu_twenty_four -> showTwentyFourList()
            R.id.menu_search -> ARouter.getInstance()
                .build(ARouterConstants.ACTIVITY_SEARCH)
                .withInt("fid", mRequestParam!!.fid)
                .navigation(this)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun showRecommendTopicList() {
        val param = mRequestParam!!.clone() as TopicListParam
        param.recommend = 1
        val intent = Intent()
        val bundle = Bundle()
        bundle.putParcelable(ParamKey.KEY_PARAM, param)
        intent.putExtras(bundle)
        ActivityUtils.startRecommendTopicActivity(this, intent)
    }

    private fun showTwentyFourList() {
        val param = mRequestParam!!.clone() as TopicListParam
        param.twentyfour = 1
        val intent = Intent()
        val bundle = Bundle()
        bundle.putParcelable(ParamKey.KEY_PARAM, param)
        intent.putExtras(bundle)
        ActivityUtils.startTwentyFourActivity(this, intent)
    }
}