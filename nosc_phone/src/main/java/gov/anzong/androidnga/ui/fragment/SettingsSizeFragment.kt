package gov.anzong.androidnga.ui.fragment

import android.widget.SeekBar.OnSeekBarChangeListener
import sp.phone.common.PhoneConfiguration
import android.webkit.WebView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import gov.anzong.androidnga.R
import android.widget.SeekBar
import nosc.api.constants.Constants

class SettingsSizeFragment : BaseFragment(), OnSeekBarChangeListener {
    private val mConfiguration = PhoneConfiguration.getInstance()
    private var mWebView: WebView? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_settings_size, container, false)
        initView(rootView)
        return rootView
    }

    override fun onResume() {
        activity?.setTitle(R.string.setting_title_size)
        super.onResume()
    }

    private fun initView(rootView: View) {
        initFontSizeView(rootView)
        initAvatarSizeView(rootView)
        initWebFontSizeView(rootView)
        initEmotionSizeView(rootView)
    }

    private fun initFontSizeView(rootView: View) {
        val seekBar = rootView.findViewById<SeekBar>(R.id.seek_topic_title)
        seekBar.apply {
            max = Constants.TOPIC_TITLE_SIZE_MAX
            min = Constants.TOPIC_CONTENT_SIZE_MIN
            progress = mConfiguration.topicContentSize
        }
        seekBar.setOnSeekBarChangeListener(this)
    }

    private fun initWebFontSizeView(rootView: View) {
        val seekBar: SeekBar = rootView.findViewById(R.id.seek_web_size)

        seekBar.apply {
            max = 100
            min = 1
            progress = mConfiguration.webViewTextZoom
        }
        seekBar.setOnSeekBarChangeListener(this)
        mWebView = rootView.findViewById(R.id.webview)
        mWebView?.loadUrl("file:///android_asset/html/adjust_size.html")
    }

    private fun initAvatarSizeView(rootView: View) {
        val seekBar: SeekBar = rootView.findViewById(R.id.seek_avatar)

        seekBar.apply{
            max= Constants.AVATAR_SIZE_MAX
            min = Constants.AVATAR_SIZE_MIN
            progress = mConfiguration.avatarSize
        }
        seekBar.setOnSeekBarChangeListener(this)
    }

    private fun initEmotionSizeView(rootView: View) {
        val seekBar: SeekBar = rootView.findViewById(R.id.seek_emoticon)

        seekBar.apply {
            max = Constants.EMOTICON_SIZE_MAX
            min = Constants.EMOTICON_SIZE_MIN
            progress = mConfiguration.emoticonSize
        }
        seekBar.setOnSeekBarChangeListener(this)
    }

    override fun onProgressChanged(signSeekBar: SeekBar, progress: Int, fromUser: Boolean) {
        when (signSeekBar.id) {
            R.id.seek_web_size -> {
                mWebView!!.settings.textZoom = progress
                mConfiguration.webViewTextZoom = progress
            }
            R.id.seek_topic_title -> mConfiguration.setTopicTitleSize(progress)
            R.id.seek_avatar -> mConfiguration.avatarSize = progress
            R.id.seek_emoticon -> mConfiguration.emoticonSize = progress
            else -> {
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {}
    override fun onStopTrackingTouch(seekBar: SeekBar) {}
}