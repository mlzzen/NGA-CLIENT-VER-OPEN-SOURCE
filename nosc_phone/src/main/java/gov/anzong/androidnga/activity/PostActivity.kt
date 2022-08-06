package gov.anzong.androidnga.activity

import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.StyleSpan
import com.alibaba.android.arouter.facade.annotation.Route
import gov.anzong.androidnga.R
import gov.anzong.androidnga.arouter.ARouterConstants
import gov.anzong.androidnga.fragment.TopicPostFragment
import sp.phone.param.ParamKey
import sp.phone.param.PostParam

@Route(path = ARouterConstants.ACTIVITY_POST)
class PostActivity : BaseActivity() {
    private val mPostFragment: TopicPostFragment by lazy{
        TopicPostFragment()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        setToolbarEnabled(true)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)
        setupToolbar()
        val act = postParam
        if (act.postAction == null) {
            finish()
            return
        }
        setTitle(getTitleResId(act.postAction))
        val bundle = Bundle()
        bundle.putString(ParamKey.KEY_ACTION, act.postAction)
        bundle.putParcelable("param", act)
        if (savedInstanceState != null) {
            bundle.putBundle("savedInstanceState", savedInstanceState)
        }
        mPostFragment.arguments = bundle
        mPostFragment.setHasOptionsMenu(true)
        supportFragmentManager.beginTransaction().replace(R.id.content, mPostFragment).commit()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mPostFragment.onSaveInstanceState(outState)
    }

    private fun getTitleResId(action: String): Int {
        return when (action) {
            "reply" -> R.string.reply_thread
            "modify" -> R.string.modify_thread
            else -> R.string.new_thread
        }
    }

    private val postParam: PostParam get() {
            val intent = intent
            val tid = intent.getStringExtra(ParamKey.KEY_TID)
            val fid = intent.getIntExtra(ParamKey.KEY_FID, -7)
            val title = intent.getStringExtra("title")
            val pid = intent.getStringExtra(ParamKey.KEY_PID)
            val action = intent.getStringExtra(ParamKey.KEY_ACTION)
            var prefix = intent.getStringExtra("prefix")
            val stid = intent.getStringExtra(ParamKey.KEY_STID)
            if (prefix != null && prefix.startsWith("[quote][pid=") && prefix.endsWith("[/quote]\n")) {
                val spanString = SpannableString(prefix)
                spanString.setSpan(
                    BackgroundColorSpan(-1513240),
                    0,
                    prefix.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spanString.setSpan(
                    StyleSpan(Typeface.BOLD),
                    prefix.indexOf("[b]Post by"),
                    prefix.indexOf("):[/b]") + 5,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                prefix = spanString.toString()
            }
            val act = PostParam(tid, "", "")
            act.postAction = action
            act.postFid = fid
            act.postPid = pid
            act.postContent = prefix
            act.postSubject = title
            act.stid = stid
            return act
        }

    override fun onBackPressed() {
        if (!mPostFragment.onBackPressed()) {
            super.onBackPressed()
        }
    }
}