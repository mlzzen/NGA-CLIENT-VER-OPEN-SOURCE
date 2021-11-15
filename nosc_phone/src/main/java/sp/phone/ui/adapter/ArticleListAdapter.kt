package sp.phone.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import sp.phone.ui.adapter.ArticleListAdapter.ArticleViewHolder
import nosc.api.bean.ThreadData
import android.view.LayoutInflater
import sp.phone.theme.ThemeManager
import sp.phone.view.webview.LocalWebView
import nosc.api.bean.ThreadRowInfo
import gov.anzong.androidnga.base.util.ToastUtils
import android.content.Intent
import sp.phone.common.UserManagerImpl
import sp.phone.common.PhoneConfiguration
import io.reactivex.ObservableOnSubscribe
import io.reactivex.ObservableEmitter
import io.reactivex.schedulers.Schedulers
import io.reactivex.android.schedulers.AndroidSchedulers
import android.app.Activity
import android.content.Context
import com.alibaba.android.arouter.launcher.ARouter
import gov.anzong.androidnga.arouter.ARouterConstants
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import sp.phone.ui.fragment.dialog.BaseDialogFragment
import sp.phone.ui.fragment.dialog.AvatarDialogFragment
import gov.anzong.androidnga.R
import android.widget.TextView
import android.widget.FrameLayout
import sp.phone.ui.adapter.ArticleListAdapter
import android.view.ViewGroup
import android.widget.ImageView
import sp.phone.rxjava.RxUtils
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentManager
import gov.anzong.androidnga.base.util.ContextUtils
import gov.anzong.androidnga.base.util.DeviceUtils
import io.reactivex.Observable
import sp.phone.rxjava.BaseSubscriber
import sp.phone.util.*
import java.lang.Exception
import java.lang.StringBuilder
import java.text.MessageFormat

/**
 * 帖子详情列表Adapter
 */
class ArticleListAdapter(
    private val mContext: Context,
    private val mFragmentManager: FragmentManager
) : RecyclerView.Adapter<ArticleViewHolder>() {
    private var mData: ThreadData? = null
    private val mLayoutInflater: LayoutInflater
    private val mThemeManager = ThemeManager.getInstance()
    private val mLocalWebViews: Array<LocalWebView?>? = arrayOfNulls(20)
    private var mTopicOwner: String? = null
    private val mOnClientClickListener = View.OnClickListener { v ->
        val row = v.tag as ThreadRowInfo
        val fromClient = row.fromClient
        val clientModel = row.fromClientModel
        val deviceInfo: String
        if (!StringUtils.isEmpty(clientModel)) {
            val clientAppCode: String
            clientAppCode = if (!fromClient.contains(" ")) {
                fromClient
            } else {
                fromClient.substring(
                    0,
                    fromClient.indexOf(' ')
                )
            }
            deviceInfo = when (clientAppCode) {
                "1" -> if (fromClient.length <= 2) {
                    "发送自Life Style苹果客户端 机型及系统:未知"
                } else {
                    ("发送自Life Style苹果客户端 机型及系统:"
                            + fromClient.substring(2))
                }
                "7" -> if (fromClient.length <= 2) {
                    "发送自NGA苹果官方客户端 机型及系统:未知"
                } else {
                    ("发送自NGA苹果官方客户端 机型及系统:"
                            + fromClient.substring(2))
                }
                "8" -> if (fromClient.length <= 2) {
                    "发送自NGA安卓客户端 机型及系统:未知"
                } else {
                    val fromData = fromClient.substring(2)
                    if (fromData.startsWith("[")
                        && fromData.contains("](Android")
                    ) {
                        ("发送自NGA安卓开源版客户端 机型及系统:"
                                + fromData.substring(1).replace(
                            "](Android", "(Android"
                        ))
                    } else {
                        "发送自NGA安卓官方客户端 机型及系统:$fromData"
                    }
                }
                "9" -> if (fromClient.length <= 2) {
                    "发送自NGA Windows Phone官方客户端 机型及系统:未知"
                } else {
                    ("发送自NGA Windows Phone官方客户端 机型及系统:"
                            + fromClient.substring(2))
                }
                "100" -> if (fromClient.length <= 4) {
                    "发送自安卓浏览器 机型及系统:未知"
                } else {
                    ("发送自安卓浏览器 机型及系统:"
                            + fromClient.substring(4))
                }
                "101" -> if (fromClient.length <= 4) {
                    "发送自苹果浏览器 机型及系统:未知"
                } else {
                    ("发送自苹果浏览器 机型及系统:"
                            + fromClient.substring(4))
                }
                "102" -> if (fromClient.length <= 4) {
                    "发送自Blackberry浏览器 机型及系统:未知"
                } else {
                    ("发送自Blackberry浏览器 机型及系统:"
                            + fromClient.substring(4))
                }
                "103" -> if (fromClient.length <= 4) {
                    "发送自Windows Phone客户端 机型及系统:未知"
                } else {
                    ("发送自Windows Phone客户端 机型及系统:"
                            + fromClient.substring(4))
                }
                else -> if (!fromClient.contains(" ")) {
                    "发送自未知浏览器 机型及系统:未知"
                } else {
                    if (fromClient.length == fromClient.indexOf(' ') + 1) {
                        "发送自未知浏览器 机型及系统:未知"
                    } else {
                        ("发送自未知浏览器 机型及系统:"
                                + fromClient.substring(
                            fromClient
                                .indexOf(' ') + 1
                        ))
                    }
                }
            }
            ToastUtils.info(deviceInfo)
        }
    }
    private val mOnReplyClickListener: View.OnClickListener = object : View.OnClickListener {
        private fun getReplyIntent(row: ThreadRowInfo): Intent {
            val intent = Intent()
            val postPrefix = StringBuilder()
            var mention: String? = null
            val quote_regex = "\\[quote\\]([\\s\\S])*\\[/quote\\]"
            val replay_regex =
                "\\[b\\]Reply to \\[pid=\\d+,\\d+,\\d+\\]Reply\\[/pid\\] Post by .+?\\[/b\\]"
            var content = row.content
            val name = row.author
            val uid = row.authorid.toString()
            val page = (row.lou + 20) / 20 // 以楼数计算page
            content = content.replace(quote_regex.toRegex(), "")
            content = content.replace(replay_regex.toRegex(), "")
            val postTime = row.postdate
            val tidStr = row.tid.toString()
            content = FunctionUtils.checkContent(content)
            content = StringUtils.unEscapeHtml(content)
            if (row.pid != 0 || row.lou == 0) {
                mention = name
                postPrefix.append("[quote][pid=")
                postPrefix.append(row.pid)
                postPrefix.append(',')
                postPrefix.append(tidStr)
                postPrefix.append(",")
                if (page > 0) postPrefix.append(page)
                postPrefix.append("]") // Topic
                postPrefix.append("Reply")
                if (row.isanonymous) { // 是匿名的人
                    postPrefix.append("[/pid] [b]Post by [uid=")
                    postPrefix.append("-1")
                    postPrefix.append("]")
                    postPrefix.append(name)
                    postPrefix.append("[/uid][color=gray](")
                    postPrefix.append(row.lou)
                    postPrefix.append("楼)[/color] (")
                } else {
                    postPrefix.append("[/pid] [b]Post by [uid=")
                    postPrefix.append(uid)
                    postPrefix.append("]")
                    postPrefix.append(name)
                    postPrefix.append("[/uid] (")
                }
                postPrefix.append(postTime)
                postPrefix.append("):[/b]\n")
                postPrefix.append(content)
                postPrefix.append("[/quote]\n")
            }
            if (!StringUtils.isEmpty(mention)) intent.putExtra("mention", mention)
            intent.putExtra(
                "prefix",
                StringUtils.removeBrTag(postPrefix.toString())
            )
            intent.putExtra("tid", tidStr)
            intent.putExtra("action", "reply")
            if (UserManagerImpl.getInstance().activeUser != null) { // 登入了才能发
                intent.setClass(
                    ContextUtils.getContext(),
                    PhoneConfiguration.getInstance().postActivityClass
                )
            } else {
                intent.setClass(
                    ContextUtils.getContext(),
                    PhoneConfiguration.getInstance().loginActivityClass
                )
            }
            return intent
        }

        override fun onClick(view: View) {
            val row = view.tag as ThreadRowInfo
            Observable.create(ObservableOnSubscribe { emitter: ObservableEmitter<Intent?> ->
                emitter.onNext(getReplyIntent(row))
                emitter.onComplete()
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : BaseSubscriber<Intent?>() {
                    override fun onNext(intent: Intent) {
                        try {
                            view.isEnabled = true
                            (view.context as Activity).startActivityForResult(
                                intent,
                                ActivityUtils.REQUEST_CODE_TOPIC_POST
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        super.onNext(intent)
                    }
                })
        }
    }
    private val mOnProfileClickListener = View.OnClickListener { view ->
        val row = view.tag as ThreadRowInfo
        if (row.isanonymous) {
            ToastUtils.info("这白痴匿名了,神马都看不到")
        } else if (row.author != null) {
            ARouter.getInstance()
                .build(ARouterConstants.ACTIVITY_PROFILE)
                .withString("uid", "" + row.authorid)
                .navigation()
        }
    }
    private val mOnAvatarClickListener = View.OnClickListener { view ->
        val row = view.tag as ThreadRowInfo
        if (row.isanonymous) {
            ToastUtils.info("这白痴匿名了,神马都看不到")
        } else {
            val bundle = Bundle()
            bundle.putString("name", row.author)
            bundle.putString("url", FunctionUtils.parseAvatarUrl(row.js_escap_avatar))
            BaseDialogFragment.show(mFragmentManager, bundle, AvatarDialogFragment::class.java)
            //FunctionUtils.Create_Avatar_Dialog(row, view.getContext(), null);
        }
    }
    private var mMenuTogglerListener: View.OnClickListener? = null
    private var mSupportListener: View.OnClickListener? = null
    private var mOpposeListener: View.OnClickListener? = null

    class ArticleViewHolder
        (itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nickNameTV: TextView? = itemView.findViewById(R.id.tv_nickName)
        var contentTV: LocalWebView? = null

        val contentContainer: FrameLayout? = itemView.findViewById(R.id.wv_container)

        val floorTv: TextView? = itemView.findViewById(R.id.tv_floor)

        val postTimeTv: TextView? = itemView.findViewById(R.id.tv_post_time)

        val replyBtn: ImageView? = itemView.findViewById(R.id.iv_reply)

        val favourBtn: ImageView? = itemView.findViewById(R.id.iv_favour)

        val treadBtn: ImageView? = itemView.findViewById(R.id.iv_tread)

        val avatarIv: ImageView? = itemView.findViewById(R.id.iv_avatar)

        val clientIv: ImageView? = itemView.findViewById(R.id.iv_client)

        val scoreTv: TextView? = itemView.findViewById(R.id.tv_score)

        val menuIv: ImageView? = itemView.findViewById(R.id.iv_more)

        val avatarPanel: FrameLayout? = itemView.findViewById(R.id.fl_avatar)

        val detailTv: TextView? = itemView.findViewById(R.id.tv_detail)
    }

    fun setTopicOwner(topicOwner: String?) {
        mTopicOwner = topicOwner
    }

    fun setData(data: ThreadData?) {
        mData = data
    }

    fun setMenuTogglerListener(menuTogglerListener: View.OnClickListener?) {
        mMenuTogglerListener = menuTogglerListener
    }

    fun setSupportListener(listener: View.OnClickListener?) {
        mSupportListener = listener
    }

    fun setOpposeListener(listener: View.OnClickListener?) {
        mOpposeListener = listener
    }

    override fun getItemViewType(position: Int): Int {
        val row = mData!!.rowList[position]
        return if (TextUtils.isEmpty(row.formattedHtmlData)) VIEW_TYPE_NATIVE_VIEW else VIEW_TYPE_WEB_VIEW
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val view = mLayoutInflater.inflate(R.layout.fragment_article_list_item, parent, false)
        val viewHolder = ArticleViewHolder(view)
        val lp = viewHolder.avatarIv!!.layoutParams
        lp.height = PhoneConfiguration.getInstance().avatarSize
        lp.width = lp.height
        //        if (viewType == VIEW_TYPE_WEB_VIEW) {
//            viewHolder.contentTextView.setVisibility(View.GONE);
//            // viewHolder.contentTV.setVisibility(View.VISIBLE);
//        } else {
//            viewHolder.contentTextView.setVisibility(View.VISIBLE);
//            //  viewHolder.contentTV.setVisibility(View.GONE);
//        }
        RxUtils.clicks(viewHolder.nickNameTV, mOnProfileClickListener)
        RxUtils.clicks(viewHolder.replyBtn, mOnReplyClickListener)
        RxUtils.clicks(viewHolder.clientIv, mOnClientClickListener)
        RxUtils.clicks(viewHolder.menuIv, mMenuTogglerListener)
        RxUtils.clicks(viewHolder.avatarPanel, mOnAvatarClickListener)
        RxUtils.clicks(viewHolder.favourBtn, mSupportListener)
        RxUtils.clicks(viewHolder.treadBtn, mOpposeListener)
        //viewHolder.contentTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, PhoneConfiguration.getInstance().getTopicContentSize());
        // viewHolder.contentTV.setTextSize(PhoneConfiguration.getInstance().getTopicContentSize());
        return viewHolder
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val row = mData!!.rowList[position] ?: return
        val color = holder.itemView.context.getColor(
            ThemeManager.getInstance().getBackgroundColor(
                if (PhoneConfiguration.getInstance().useSolidColorBackground()) 1 else position
            )
        )
        (holder.itemView as CardView).setCardBackgroundColor(color)
        holder.replyBtn!!.tag = row
        holder.nickNameTV!!.tag = row
        holder.menuIv!!.tag = row
        holder.avatarPanel!!.tag = row
        holder.favourBtn!!.tag = row
        holder.treadBtn!!.tag = row
        onBindAvatarView(holder.avatarIv, row)
        onBindDeviceType(holder.clientIv, row)
        onBindContentView(holder, row, position)
        val fgColor = mThemeManager.getAccentColor(mContext)
        FunctionUtils.handleNickName(row, fgColor, holder.nickNameTV, mTopicOwner, mContext)
        holder.floorTv!!.text = MessageFormat.format("#{0}", row.lou.toString())
        holder.postTimeTv!!.text = row.postdate
        holder.scoreTv!!.text = MessageFormat.format("{0}", row.score)
        //todo 赞多加粗
        holder.detailTv!!.text = String.format(
            "级别:%s   威望:%s   发帖:%s",
            row.memberGroup,
            row.reputation,
            row.postCount
        )
    }

    private fun createLocalWebView(): LocalWebView {
        val localWebView = LocalWebView(mContext)
        val lp = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        lp.marginStart = mContext.resources.getDimensionPixelSize(R.dimen.material_standard_half)
        lp.marginEnd = mContext.resources.getDimensionPixelSize(R.dimen.material_standard_half)
        localWebView.layoutParams = lp
        localWebView.isVerticalScrollBarEnabled = false
        localWebView.isHorizontalScrollBarEnabled = false
        return localWebView
    }

    private fun onBindContentView(holder: ArticleViewHolder, row: ThreadRowInfo, position: Int) {
        val html = row.formattedHtmlData
        if (html != null) {
            if (mLocalWebViews != null) {
                var localWebView = mLocalWebViews[position]
                if (localWebView == null) {
                    localWebView = createLocalWebView()
                    mLocalWebViews[position] = localWebView
                }
                if (localWebView !== holder.contentTV) {
                    holder.contentContainer!!.removeView(holder.contentTV)
                    if (localWebView.parent != null) {
                        (localWebView.parent as ViewGroup).removeView(localWebView)
                    }
                    holder.contentTV = localWebView
                    holder.contentContainer!!.addView(localWebView)
                }
            } else if (holder.contentTV == null) {
                holder.contentTV = createLocalWebView()
                holder.contentContainer!!.addView(holder.contentTV)
            }
            holder.contentTV!!.webViewClientEx.setImgUrls(row.imageUrls)
            holder.contentTV!!.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
        } else {
            //holder.contentTextView.setText(row.getContent());
        }
    }

    private fun onBindDeviceType(clientBtn: ImageView?, row: ThreadRowInfo) {
        val deviceType = row.fromClientModel
        if (TextUtils.isEmpty(deviceType)) {
            clientBtn!!.visibility = View.GONE
        } else {
            when (deviceType) {
                DEVICE_TYPE_IOS -> clientBtn!!.setImageResource(R.drawable.ic_apple_12dp)
                DEVICE_TYPE_WP -> clientBtn!!.setImageResource(R.drawable.ic_windows_12dp)
                DEVICE_TYPE_ANDROID -> clientBtn!!.setImageResource(R.drawable.ic_android_12dp)
                else -> clientBtn!!.setImageResource(R.drawable.ic_smartphone_12dp)
            }
            clientBtn.tag = row
            clientBtn.visibility = View.VISIBLE
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return if (mData == null) 0 else mData!!.rowNum
    }

    private fun onBindAvatarView(avatarIv: ImageView?, row: ThreadRowInfo) {
        val avatarUrl = FunctionUtils.parseAvatarUrl(row.js_escap_avatar)
        val downImg = (DeviceUtils.isWifiConnected(mContext)
                || PhoneConfiguration.getInstance()
            .isDownAvatarNoWifi)
        ImageUtils.loadRoundCornerAvatar(avatarIv, avatarUrl, !downImg)
    }

    companion object {
        private const val DEVICE_TYPE_IOS = "ios"
        private const val DEVICE_TYPE_ANDROID = "android"
        private const val DEVICE_TYPE_WP = "wp"
        private const val VIEW_TYPE_WEB_VIEW = 0
        private const val VIEW_TYPE_NATIVE_VIEW = 1
    }

    init {
        if (HtmlUtils.hide == null) {
            HtmlUtils.initStaticStrings(mContext)
        }
        mLayoutInflater = LayoutInflater.from(mContext)
    }
}