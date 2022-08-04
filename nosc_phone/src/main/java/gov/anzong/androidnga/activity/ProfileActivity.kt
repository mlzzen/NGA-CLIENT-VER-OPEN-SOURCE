package gov.anzong.androidnga.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.google.android.material.appbar.AppBarLayout
import gov.anzong.androidnga.R
import gov.anzong.androidnga.Utils
import gov.anzong.androidnga.arouter.ARouterConstants
import nosc.utils.DeviceUtils
import nosc.utils.uxUtils.ToastUtils
import gov.anzong.androidnga.core.data.HtmlData
import gov.anzong.androidnga.core.decode.ForumDecoder
import gov.anzong.androidnga.databinding.ActivityUserProfileBinding
import nosc.api.callbacks.OnHttpCallBack
import nosc.api.bean.ProfileData
import sp.phone.common.PhoneConfiguration
import sp.phone.common.UserManagerImpl
import sp.phone.param.ParamKey
import sp.phone.task.JsonProfileLoadTask
import sp.phone.theme.ThemeManager
import sp.phone.util.ActivityUtils
import sp.phone.util.FunctionUtils
import sp.phone.util.ImageUtils
import sp.phone.util.StringUtils
import sp.phone.view.webview.WebViewEx

@Route(path = ARouterConstants.ACTIVITY_PROFILE)
class ProfileActivity : BaseActivity(),
    OnHttpCallBack<ProfileData?> {
    private var mProfileData: ProfileData? = null
    private var binding: ActivityUserProfileBinding? = null
    private val mThemeManager = ThemeManager.getInstance()
    private var mParams: String? = null
    private var mCurrentUser = false
    private var mProfileLoadTask: JsonProfileLoadTask? = null
    private var mOptionMenu: Menu? = null//获取状态栏高度的资源id

    private val mSignWebView:WebViewEx get() = binding?.content?.wvSign!!
    private val mAdminWebView:WebViewEx get() = binding?.content?.wvAdmin!!
    private val mFameWebView:WebViewEx get() = binding?.content?.wvFame!!

    /**
     * 利用反射获取状态栏高度
     */
    private val statusBarHeight: Int
        get() {
            val result: Int
            val res = resources
            //获取状态栏高度的资源id
            val resourceId = res.getIdentifier("status_bar_height", "dimen", "android")
            result = if (resourceId > 0) {
                res.getDimensionPixelSize(resourceId)
            } else {
                res.getDimensionPixelSize(R.dimen.status_bar_height)
            }
            return result
        }

    private fun updateToolbarLayout() {
        val appBarLayout = findViewById<AppBarLayout>(R.id.app_bar)
        if (DeviceUtils.isFullScreenDevice()) {
            appBarLayout.layoutParams.height =
                resources.getDimensionPixelSize(R.dimen.app_bar_height_full_screen)
        }
        val statusBarHeight = statusBarHeight
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        val lp = toolbar.layoutParams as FrameLayout.LayoutParams
        lp.setMargins(0, statusBarHeight, 0, 0)
        val parentView = binding!!.ivAvatar.parent as View
        parentView.setPadding(0, statusBarHeight, 0, 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setToolbarEnabled(true)
        super.onCreate(savedInstanceState)
        val intent = intent
        val um = UserManagerImpl.getInstance()
        if (intent.hasExtra("uid")) {
            val uid = intent.getStringExtra("uid")
            mCurrentUser = uid == um.userId
            mParams = "uid=$uid"
        } else if (intent.hasExtra("username")) {
            val userName = intent.getStringExtra("username")
            mCurrentUser = userName!!.endsWith(um.userName)
            mParams = if (userName.startsWith("UID")) {
                "uid=" + userName.substring(3)
            } else {
                "username=" + StringUtils.encodeUrl(userName, "gbk")
            }
        }
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        binding?.content?.btnModifySign?.setOnClickListener {
            startChangeSignActivity()
        }
        setContentView(binding!!.root)
        setupActionBar()
        updateToolbarLayout()
        setupStatusBar()
        refresh()
    }

    private fun setupStatusBar() {
        val window = window
        val decorView = window.decorView
        //两个 flag 要结合使用，表示让应用的主体内容占用系统状态栏的空间
        val option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        decorView.systemUiVisibility = option
        getWindow().statusBarColor = Color.TRANSPARENT
    }

    private fun refresh() {
        ActivityUtils.getInstance().noticeSaying(this)
        mProfileLoadTask = JsonProfileLoadTask(this)
        mProfileLoadTask!!.execute(mParams)
    }

    override fun onDestroy() {
        mProfileLoadTask!!.cancel()
        super.onDestroy()
    }

    private fun loadBasicProfile(profileInfo: ProfileData) {
        binding?.apply {
            toolbarLayout.title = profileInfo.userName
            tvUid.text = String.format("用户ID : %s", profileInfo.uid)
            content.apply{
                tvPostCount.text = profileInfo.postCount
                tvUserRegisterTime.text = profileInfo.registerDate
                tvUserEmail.text = profileInfo.emailAddress
                tvUserTel.text = profileInfo.phoneNumber
                tvUserGroup.text = profileInfo.memberGroup
                if (mCurrentUser) {
                    btnModifySign.visibility = View.VISIBLE
                } else {
                    btnModifySign.visibility = View.GONE
                }
            }
        }

        handleAvatar(profileInfo)
        handleUserState(profileInfo)
        handleUserMoney(profileInfo)
    }

    private fun handleUserState(profileInfo: ProfileData) {
        binding?.content?.let{
            if (profileInfo.isMuted) {
                it.tvUserState.text = "已禁言"
                it.tvUserState.setTextColor(ContextCompat.getColor(this, R.color.color_state_muted))
                if (!StringUtils.isEmpty(profileInfo.mutedTime)) {
                    it.tvUserMuteTime.text = profileInfo.mutedTime
                    //  mUserMuteTime.setVisibility(View.VISIBLE);
                }
            } else if (profileInfo.isNuked) {
                it.tvUserState.text = "NUKED(?)"
                it.tvUserState.setTextColor(ContextCompat.getColor(this, R.color.color_state_nuked))
            } else {
                it.tvUserState.text = "已激活"
                it.tvUserState.setTextColor(ContextCompat.getColor(this, R.color.color_state_active))
            }
        }

    }

    private fun handleUserMoney(profileInfo: ProfileData) {
        val money = profileInfo.money.toInt()
        val gold = money / 10000
        val silver = (money - gold * 10000) / 100
        val copper = money - gold * 10000 - silver * 100
        binding?.content?.apply {
            tvUserMoneyGold.text = gold.toString()
            tvUserMoneySilver.text = silver.toString()
            tvUserMoneyCopper.text = copper.toString()

        }

    }

    private val url: String get() = "http://bbs.ngacn.cc/nuke.php?func=ucp&$mParams"

    private fun createAdminHtml(ret: ProfileData): String {
        val builder = StringBuilder()
        val adminForumsEntryList = ret.adminForums
        if (adminForumsEntryList == null) {
            builder.append("无管理板块")
        } else {
            for (data in adminForumsEntryList) {
                builder.append("<a style=\"color:#551200;\" href=\"http://nga.178.com/thread.php?fid=")
                    .append(data.fid).append("\">[")
                    .append(data.forumName)
                    .append("]</a>&nbsp;")
            }
            builder.append("<br>")
        }
        return builder.toString()
    }

    private fun startChangeSignActivity() {
        val intent = Intent()
        intent.putExtra("prefix", mProfileData!!.sign)
        intent.setClass(this, PhoneConfiguration.signPostActivityClass)
        startActivityForResult(intent, 321)
    }

    private fun loadProfileInfo(profileInfo: ProfileData) {
        loadBasicProfile(profileInfo)
        handleSignWebView(mSignWebView, profileInfo)
        handleAdminWebView(mAdminWebView, profileInfo)
        handleFameWebView(mFameWebView, profileInfo)
        if (mOptionMenu != null) {
            onPrepareOptionsMenu(mOptionMenu!!)
        }
    }

    private fun createFameHtml(ret: ProfileData, color: String): String {
        var frame = ret.frame
        try {
            frame = (ret.frame.toDouble() / 10.0).toString()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
        val builder = StringBuilder("<ul style=\"padding: 0px; margin: 0px;\">")
        builder.append("<li style=\"display: block;float: left;width: 33%;\">")
            .append("<label style=\"float: left;color: ").append(color).append(";\">威望</label>")
            .append("<span style=\"float: left; color: #808080;\">:</span>")
            .append("<span style=\"float: left; color: #808080;\">")
            .append(frame)
            .append("</span></li>")
        val reputationEntryList = ret.reputationEntryList
        if (reputationEntryList != null) {
            for (data in reputationEntryList) {
                builder.append("<li style=\"display: block;float: left;width: 33%;\">")
                    .append("<label style=\"float: left;color: ")
                    .append(color).append(";\">").append(data.name).append("</label>")
                    .append("<span style=\"float: left; color: #808080;\">:</span>")
                    .append("<span style=\"float: left; color: #808080;\">")
                    .append(data.data).append("</span></li>")
            }
        }
        builder.append("</ul><br>")
        return builder.toString()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_user_profile, menu)
        menuInflater.inflate(R.menu.menu_default, menu)
        mOptionMenu = menu
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.menu_copy_url).isVisible = true
        menu.findItem(R.id.menu_open_by_browser).isVisible = true
        menu.findItem(R.id.menu_search_post).isVisible = mProfileData != null
        menu.findItem(R.id.menu_search_reply).isVisible = mProfileData != null
        menu.findItem(R.id.menu_send_message).isVisible = mProfileData != null && !mCurrentUser
        menu.findItem(R.id.menu_modify_avatar).isVisible = mProfileData != null && mCurrentUser
        val item = menu.findItem(R.id.menu_ban_this_one)
        if (item != null) {
            if (mProfileData == null) {
                item.isVisible = false
            } else {
                item.isVisible = true
                val ban = UserManagerImpl.getInstance().checkBlackList(
                    mProfileData!!.uid
                )
                item.setTitle(if (ban) R.string.cancel_ban_thisone else R.string.ban_thisone)
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_send_message -> sendShortMessage()
            R.id.menu_search_post -> searchPost()
            R.id.menu_search_reply -> searchReply()
            R.id.menu_modify_avatar -> startModifyAvatar()
            R.id.menu_copy_url -> FunctionUtils.copyToClipboard(this, url)
            R.id.menu_open_by_browser -> FunctionUtils.openUrlByDefaultBrowser(this, url)
            R.id.menu_ban_this_one -> banThisSB()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    fun banThisSB() {
        val um = UserManagerImpl.getInstance()
        if (um.checkBlackList(mProfileData!!.uid)) {
            um.removeFromBlackList(mProfileData!!.uid)
            ToastUtils.success(R.string.remove_from_blacklist_success)
        } else {
            um.addToBlackList(mProfileData!!.userName, mProfileData!!.uid)
            ToastUtils.success(R.string.add_to_blacklist_success)
        }
    }

    private fun startModifyAvatar() {
        val intent = Intent()
        intent.putExtra("prefix", mProfileData!!.sign)
        intent.setClass(this, AvatarPostActivity::class.java)
        startActivity(intent)
    }

    private fun sendShortMessage() {
        ARouter.getInstance()
            .build(ARouterConstants.ACTIVITY_MESSAGE_POST)
            .withString("to", mProfileData!!.userName)
            .withString(ParamKey.KEY_ACTION, "new")
            .withString("messagemode", "yes")
            .navigation(this)
    }

    private fun searchPost() {
        val intent = Intent(this, PhoneConfiguration.topicActivityClass)
        intent.putExtra(ParamKey.KEY_AUTHOR_ID, mProfileData!!.uid.toInt())
        intent.putExtra(ParamKey.KEY_AUTHOR, mProfileData!!.userName)
        startActivity(intent)
    }

    private fun searchReply() {
        val intent = Intent(this, PhoneConfiguration.topicActivityClass)
        intent.putExtra(ParamKey.KEY_AUTHOR_ID, mProfileData!!.uid.toInt())
        intent.putExtra(ParamKey.KEY_SEARCH_POST, 1)
        intent.putExtra(ParamKey.KEY_AUTHOR, mProfileData!!.userName)
        startActivity(intent)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 321 && resultCode == RESULT_OK) {
            val signData = data!!.getStringExtra("sign")
            if (mProfileData != null) {
                mProfileData!!.sign = signData
                mSignWebView.requestLayout()
                handleSignWebView(mSignWebView, mProfileData!!)
            }
        } else if (requestCode == 123 && resultCode == RESULT_OK) {
            val avatarData = data!!.getStringExtra("avatar")
            mProfileData!!.avatarUrl = avatarData
            mSignWebView.requestLayout()
            //  handleAvatar(avatarImage, mProfileData);
        }
    }

    private fun handleSignWebView(contentTV: WebViewEx, ret: ProfileData) {
        val theme = ThemeManager.getInstance()
        val fgColor = resources.getColor(theme.foregroundColorRes)
        var bgColor: Int = if (mThemeManager.isNightMode) {
            resources.getColor(theme.getBackgroundColorRes(0))
        } else {
            resources.getColor(R.color.profilebgcolor)
        }
        bgColor = bgColor and 0xffffff
        val bgcolorStr = String.format("%06x", bgColor)
        val htmlfgColor = fgColor and 0xffffff
        val fgColorStr = String.format("%06x", htmlfgColor)
        contentTV.setLocalMode()
        contentTV.loadDataWithBaseURL(
            null,
            signatureToHtmlText(ret, fgColorStr, bgcolorStr),
            "text/html", "utf-8", null
        )
    }

    private fun handleAdminWebView(contentTV: WebViewEx, ret: ProfileData) {
        var bgColor: Int
        val fgColor: Int
        val theme = ThemeManager.getInstance()
        if (mThemeManager.isNightMode) {
            bgColor = resources.getColor(theme.getBackgroundColorRes(0))
            fgColor = resources.getColor(theme.foregroundColorRes)
        } else {
            bgColor = resources.getColor(R.color.profilebgcolor)
            fgColor = resources.getColor(R.color.profilefcolor)
        }
        bgColor = bgColor and 0xffffff
        val bgcolorStr = String.format("%06x", bgColor)
        val htmlfgColor = fgColor and 0xffffff
        val fgColorStr = String.format("%06x", htmlfgColor)
        contentTV.setLocalMode()
        contentTV.loadDataWithBaseURL(
            null,
            adminToHtmlText(ret, fgColorStr, bgcolorStr), "text/html", "utf-8", null
        )
    }

    private fun handleFameWebView(contentTV: WebViewEx, ret: ProfileData) {
        var bgColor: Int
        val fgColor: Int
        val theme = ThemeManager.getInstance()
        if (mThemeManager.isNightMode) {
            bgColor = resources.getColor(theme.getBackgroundColorRes(0))
            fgColor = resources.getColor(theme.foregroundColorRes)
        } else {
            bgColor = resources.getColor(R.color.profilebgcolor)
            fgColor = resources.getColor(R.color.profilefcolor)
        }
        bgColor = bgColor and 0xffffff
        val bgcolorStr = String.format("%06x", bgColor)
        val htmlfgColor = fgColor and 0xffffff
        val fgColorStr = String.format("%06x", htmlfgColor)
        contentTV.setLocalMode()
        contentTV.loadDataWithBaseURL(
            null,
            fameToHtmlText(ret, fgColorStr, bgcolorStr), "text/html", "utf-8", null
        )
    }

    fun fameToHtmlText(ret: ProfileData, fgColorStr: String, bgcolorStr: String): String {
        var color = "#121C46"
        if (mThemeManager.isNightMode) {
            color = "#712D08"
        }
        var ngaHtml = createFameHtml(ret, color)
        ngaHtml =
            ("<HTML> <HEAD><META   http-equiv=Content-Type   content= \"text/html;   charset=utf-8 \">"
                    + "<body bgcolor= '#"
                    + bgcolorStr
                    + "'>"
                    + "<font color='#"
                    + fgColorStr + "' size='2'>" + ngaHtml + "</font></body>")
        return ngaHtml
    }

    fun adminToHtmlText(ret: ProfileData, fgColorStr: String, bgcolorStr: String): String {
        var ngaHtml = createAdminHtml(ret)
        ngaHtml =
            ("<HTML> <HEAD><META   http-equiv=Content-Type   content= \"text/html;   charset=utf-8 \">"
                    + "<body bgcolor= '#"
                    + bgcolorStr
                    + "'>"
                    + "<font color='#"
                    + fgColorStr + "' size='2'>" + ngaHtml + "</font></body>")
        return ngaHtml
    }

    fun signatureToHtmlText(ret: ProfileData, fgColorStr: String, bgcolorStr: String): String {
        var ngaHtml = ForumDecoder.decode(ret.sign, HtmlData.create(ret.sign, Utils.getNGAHost()))
        ngaHtml =
            ("<HTML> <HEAD><META   http-equiv=Content-Type   content= \"text/html;   charset=utf-8 \">"
                    + "<body bgcolor= '#"
                    + bgcolorStr
                    + "'>"
                    + "<font color='#"
                    + fgColorStr
                    + "' size='2'>"
                    + "<div style=\"border: 3px solid rgb(204, 204, 204);padding: 2px; \">"
                    + ngaHtml + "</div>" + "</font></body>"
                    + "<script type=\"text/javascript\" src=\"file:///android_asset/html/script.js\"></script>")
        return ngaHtml
    }

    private fun handleAvatar(row: ProfileData) {
        val avatarUrl = FunctionUtils.parseAvatarUrl(row.avatarUrl) //
        ImageUtils.loadRoundCornerAvatar(binding?.ivAvatar, avatarUrl)
        ImageUtils.loadAvatar(findViewById<View>(R.id.iv_toolbar_layout_bg) as ImageView, avatarUrl)
    }

    override fun onError(text: String) {
        ToastUtils.error(text)
    }

    override fun onSuccess(data: ProfileData?) {
        mProfileData = data
        if (data != null) {
            loadProfileInfo(data)
        }
    }

    companion object {
        private const val TAG = "ProfileActivity"
    }
}