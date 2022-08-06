package sp.phone.ui.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import gov.anzong.androidnga.R
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.Switch
import gov.anzong.androidnga.GlideApp
import com.bumptech.glide.load.engine.DiskCacheStrategy
import sp.phone.common.User
import sp.phone.common.UserManager
import sp.phone.common.UserManagerImpl

class UserListAdapter(private val mContext: Context, userList: List<User>) :
    RecyclerView.Adapter<UserListAdapter.UserViewHolder>() {
    private val mUserList: List<User>
    private var mOnClickListener: View.OnClickListener? = null
    private val mUserManager: UserManager = UserManagerImpl.getInstance()

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var userNameView: TextView
        var avatarView: ImageView
        var checkView: CheckBox

        init {
            userNameView = itemView.findViewById(R.id.user_name)
            avatarView = itemView.findViewById(R.id.avatar)
            checkView = itemView.findViewById(R.id.check)
        }
    }

    fun setOnClickListener(listener: View.OnClickListener?) {
        mOnClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val convertView =
            LayoutInflater.from(mContext).inflate(R.layout.list_user_manager_item, parent, false)
        val holder = UserViewHolder(convertView)
        holder.itemView.setOnClickListener(mOnClickListener)
        holder.checkView.setOnClickListener(mOnClickListener)
        return holder
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = mUserList[position]
        holder.userNameView.text = user.nickName
        holder.checkView.isChecked = mUserManager.activeUserIndex == position
        holder.itemView.tag = position
        holder.checkView.tag = position
        val avatarUrl = user.avatarUrl
        GlideApp.with(mContext)
            .load(avatarUrl)
            .placeholder(R.drawable.default_avatar)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .into(holder.avatarView)
    }

    override fun getItemCount(): Int {
        return mUserList.size
    }

    init {
        mUserList = userList
    }
}