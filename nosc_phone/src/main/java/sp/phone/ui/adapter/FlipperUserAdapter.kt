package sp.phone.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import gov.anzong.androidnga.R
import nosc.utils.getActivity
import nosc.utils.jumpToLogin
import nosc.utils.startUserProfile
import sp.phone.common.User
import sp.phone.common.UserManagerImpl
import sp.phone.util.ImageUtils

class FlipperUserAdapter(private val onNextUserCallback:(List<User>)->Unit) :
    RecyclerView.Adapter<FlipperUserAdapter.UserViewHolder>() {
    private val mUserManager = UserManagerImpl.getInstance()
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): UserViewHolder {
        val itemView = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.nav_header_view_login_user, viewGroup, false)
        return UserViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: UserViewHolder, i: Int) {
        val size = mUserManager.userSize
        if (size == 0) {
            viewHolder.loginState.text = "未登录"
            viewHolder.loginId.text = "点击下面的登录账号登录"
            viewHolder.nextImage.visibility = View.GONE
            viewHolder.itemView.setOnClickListener { v: View -> v.getActivity()?.jumpToLogin() }
        } else {
            if (size <= 1) {
                viewHolder.nextImage.visibility = View.GONE
            }
            if (size == 1) {
                viewHolder.loginState.visibility = View.GONE
            } else {
                viewHolder.loginState.visibility = View.VISIBLE
                viewHolder.loginState.text = String.format("已登录%s", size.toString() + "个账户")
            }
            val user = mUserManager.userList[i]
            viewHolder.loginId.text = String.format("%s(%s)", user.nickName, user.userId)
            handleUserAvatar(viewHolder.avatarImage, user.avatarUrl)
            viewHolder.itemView.setOnClickListener { v: View -> v.getActivity()?.startUserProfile(user.userId) }
            viewHolder.nextImage.setOnClickListener { v: View -> onNextUserCallback.invoke(mUserManager.userList) }
        }
    }


    private fun handleUserAvatar(avatarIV: ImageView, url: String?) {
        avatarIV.imageTintList = null
        ImageUtils.loadRoundCornerAvatar(avatarIV, url)
    }

    override fun getItemCount(): Int {
        val size = mUserManager.userSize
        return if (size == 0) 1 else size
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var loginId: TextView = itemView.findViewById(R.id.tv_user_name)
        var loginState: TextView = itemView.findViewById(R.id.tv_login_state)
        var avatarImage: ImageView = itemView.findViewById(R.id.iv_avatar)
        var nextImage: ImageView = itemView.findViewById(R.id.iv_next)

    }
}