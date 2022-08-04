package sp.phone.common

import android.content.Context
import nosc.utils.PreferenceUtils.getData
import nosc.utils.PreferenceUtils.putData
import nosc.utils.PreferenceKey
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import gov.anzong.androidnga.activity.TopicListActivity
import gov.anzong.androidnga.activity.ArticleListActivity
import gov.anzong.androidnga.activity.PostActivity
import gov.anzong.androidnga.activity.SignPostActivity
import gov.anzong.androidnga.activity.ProfileActivity
import gov.anzong.androidnga.activity.LoginActivity
import gov.anzong.androidnga.activity.MessageDetailActivity
import android.content.SharedPreferences
import nosc.api.constants.Constants
import nosc.utils.ContextUtils
import java.lang.Exception

object PhoneConfiguration : PreferenceKey, OnSharedPreferenceChangeListener {
    @JvmField
    var topicActivityClass: Class<*> = TopicListActivity::class.java
    @JvmField
    var articleActivityClass: Class<*> = ArticleListActivity::class.java
    var postActivityClass: Class<*> = PostActivity::class.java
    var signPostActivityClass: Class<*> = SignPostActivity::class.java
    @JvmField
    var profileActivityClass: Class<*> = ProfileActivity::class.java
    @JvmField
    var loginActivityClass: Class<*> = LoginActivity::class.java
    @JvmField
    var messageDetialActivity: Class<*> = MessageDetailActivity::class.java
    var isNotificationEnabled = false
        private set
    var isNotificationSoundEnabled = false
        private set
    var isDownAvatarNoWifi = false
        private set
    var isDownImgNoWifi = false
        private set
    var isShowSignature = false
        private set
    var isShowColorText = false
        private set
    private var mUpdateAfterPost = false
    var isShowClassicIcon = false
        private set
    var isLeftHandMode = false
        private set
    var isShowBottomTab = false
        private set
    var isHardwareAcceleratedEnabled = false
        private set
    private var mFilterSubBoard = false
    private var mSortByPostOrder = false
    override fun onSharedPreferenceChanged(sp: SharedPreferences, key: String) {
        when (key) {
            PreferenceKey.NOTIFIACTION_SOUND -> isNotificationSoundEnabled =
                sp.getBoolean(key, true)
            PreferenceKey.ENABLE_NOTIFIACTION -> isNotificationEnabled = sp.getBoolean(key, true)
            PreferenceKey.DOWNLOAD_AVATAR_NO_WIFI -> isDownAvatarNoWifi = sp.getBoolean(key, true)
            PreferenceKey.DOWNLOAD_IMG_NO_WIFI -> isDownImgNoWifi = sp.getBoolean(key, true)
            PreferenceKey.SHOW_SIGNATURE -> isShowSignature = sp.getBoolean(key, false)
            PreferenceKey.SHOW_COLORTXT -> isShowColorText = sp.getBoolean(key, false)
            PreferenceKey.REFRESH_AFTERPOST_SETTING_MODE -> mUpdateAfterPost =
                sp.getBoolean(key, true)
            PreferenceKey.SHOW_ICON_MODE -> isShowClassicIcon = sp.getBoolean(key, false)
            PreferenceKey.LEFT_HAND -> isLeftHandMode = sp.getBoolean(key, false)
            PreferenceKey.BOTTOM_TAB -> isShowBottomTab = sp.getBoolean(key, false)
            PreferenceKey.HARDWARE_ACCELERATED -> isHardwareAcceleratedEnabled =
                sp.getBoolean(key, true)
            PreferenceKey.FILTER_SUB_BOARD -> mFilterSubBoard = sp.getBoolean(key, false)
            PreferenceKey.SORT_BY_POST -> mSortByPostOrder = sp.getBoolean(key, false)
            else -> {}
        }
    }

    private fun initialize() {
        val sp = ContextUtils.getContext()
            .getSharedPreferences(PreferenceKey.PERFERENCE, Context.MODE_PRIVATE)
        sp.registerOnSharedPreferenceChangeListener(this)
        isNotificationSoundEnabled = sp.getBoolean(PreferenceKey.NOTIFIACTION_SOUND, true)
        isNotificationEnabled = sp.getBoolean(PreferenceKey.ENABLE_NOTIFIACTION, true)
        isDownAvatarNoWifi = sp.getBoolean(PreferenceKey.DOWNLOAD_AVATAR_NO_WIFI, true)
        isDownImgNoWifi = sp.getBoolean(PreferenceKey.DOWNLOAD_IMG_NO_WIFI, true)
        isShowSignature = sp.getBoolean(PreferenceKey.SHOW_SIGNATURE, false)
        isShowColorText = sp.getBoolean(PreferenceKey.SHOW_COLORTXT, false)
        mUpdateAfterPost = sp.getBoolean(PreferenceKey.REFRESH_AFTERPOST_SETTING_MODE, true)
        isShowClassicIcon = sp.getBoolean(PreferenceKey.SHOW_ICON_MODE, false)
        isLeftHandMode = sp.getBoolean(PreferenceKey.LEFT_HAND, false)
        isShowBottomTab = sp.getBoolean(PreferenceKey.BOTTOM_TAB, false)
        isHardwareAcceleratedEnabled = sp.getBoolean(PreferenceKey.HARDWARE_ACCELERATED, true)
        mFilterSubBoard = sp.getBoolean(PreferenceKey.FILTER_SUB_BOARD, false)
        mSortByPostOrder = sp.getBoolean(PreferenceKey.SORT_BY_POST, false)
    }

    fun needSortByPostOrder(): Boolean {
        return mSortByPostOrder
    }

    fun needFilterSubBoard(): Boolean {
        return mFilterSubBoard
    }

    fun needUpdateAfterPost(): Boolean {
        return mUpdateAfterPost
    }

    var avatarSize: Int
        get() = try {
            getData(PreferenceKey.KEY_AVATAR_SIZE, Constants.AVATAR_SIZE_DEFAULT)
        } catch (e: Exception) {
            avatarSize = Constants.AVATAR_SIZE_DEFAULT
            Constants.AVATAR_SIZE_DEFAULT
        }
        set(value) {
            putData(PreferenceKey.KEY_AVATAR_SIZE, value)
        }
    var emoticonSize: Int
        get() = getData(PreferenceKey.KEY_EMOTICON_SIZE, Constants.EMOTICON_SIZE_DEFAULT)
        set(value) {
            putData(PreferenceKey.KEY_EMOTICON_SIZE, value)
        }
    var topicTitleSize: Int
        get() = getData(PreferenceKey.KEY_TOPIC_TITLE_SIZE, Constants.TOPIC_TITLE_SIZE_DEFAULT)
        set(size) {
            putData(PreferenceKey.KEY_TOPIC_TITLE_SIZE, size)
        }
    var topicContentSize: Int
        get() = getData(PreferenceKey.KEY_TOPIC_CONTENT_SIZE, Constants.TOPIC_CONTENT_SIZE_DEFAULT)
        set(size) {
            putData(PreferenceKey.KEY_TOPIC_CONTENT_SIZE, size)
        }
    var webViewTextZoom: Int
        get() = getData(PreferenceKey.KEY_WEBVIEW_TEXT_ZOOM, Constants.WEBVIEW_DEFAULT_TEXT_ZOOM)
        set(textRoom) {
            putData(PreferenceKey.KEY_WEBVIEW_TEXT_ZOOM, textRoom)
        }

    fun useSolidColorBackground(): Boolean {
        return ContextUtils.getSharedPreferences(PreferenceKey.PERFERENCE)
            .getBoolean(PreferenceKey.KEY_USE_SOLID_COLOR_BG, true)
    }

    @get:Deprecated("")
    val webSize: Int
        get() = topicContentSize
    val cookie: String
        get() = UserManagerImpl.getInstance().cookie

    init {
        initialize()
    }
}

val appConfig = PhoneConfiguration