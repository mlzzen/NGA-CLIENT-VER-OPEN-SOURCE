package gov.anzong.androidnga.activity

import android.R
import gov.anzong.androidnga.arouter.ARouterConstants
import nosc.utils.PreferenceKey
import sp.phone.param.ArticleListParam
import gov.anzong.androidnga.fragment.ArticleTabFragment
import android.os.Bundle
import sp.phone.param.ParamKey
import android.content.Intent
import com.alibaba.android.arouter.facade.annotation.Route
import sp.phone.util.StringUtils

/**
 * 帖子详情页, 是否MD都用这个
 */
@Route(path = ARouterConstants.ACTIVITY_TOPIC_CONTENT)
class ArticleListActivity : BaseActivity(), PreferenceKey {
    private var mRequestParam: ArticleListParam? = null
    private fun setupFragment() {
        val fm = supportFragmentManager
        var fragment = fm.findFragmentById(R.id.content)
        if (fragment == null) {
            fragment = ArticleTabFragment()
            fragment.setHasOptionsMenu(true)
            val bundle = Bundle()
            bundle.putParcelable(ParamKey.KEY_PARAM, mRequestParam)
            fragment.setArguments(bundle)
            fm.beginTransaction().replace(R.id.content, fragment).commit()
        } else {
            fragment.setHasOptionsMenu(true)
        }
    }

    private val articleListParam: ArticleListParam?
        get() {
            val bundle = intent.extras
            val url = intent.dataString
            var param: ArticleListParam? = null
            if (url != null) {
                param = ArticleListParam()
                param.tid = StringUtils.getUrlParameter(url, "tid")
                param.pid = StringUtils.getUrlParameter(url, "pid")
                param.authorId = StringUtils.getUrlParameter(url, "authorid")
                param.page = StringUtils.getUrlParameter(url, "page")
                param.searchPost = StringUtils.getUrlParameter(url, ParamKey.KEY_SEARCH_POST)
            } else if (bundle != null) {
                param = bundle.getParcelable(ParamKey.KEY_PARAM)
                if (param == null) {
                    param = ArticleListParam()
                    param.tid = bundle.getInt(ParamKey.KEY_TID, 0)
                    param.pid = bundle.getInt(ParamKey.KEY_PID, 0)
                    param.authorId = bundle.getInt(ParamKey.KEY_AUTHOR_ID, 0)
                    param.searchPost = bundle.getInt(ParamKey.KEY_SEARCH_POST, 0)
                    param.title = bundle.getString(ParamKey.KEY_TITLE)
                }
            }
            return param
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        setToolbarEnabled(true)
        mRequestParam = articleListParam
        super.onCreate(savedInstanceState)
        if (mRequestParam == null) {
            finish()
            return
        }
        setupFragment()
        if (mRequestParam!!.title != null) {
            title = mRequestParam!!.title
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        supportFragmentManager.findFragmentById(R.id.content)!!
            .onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }
}