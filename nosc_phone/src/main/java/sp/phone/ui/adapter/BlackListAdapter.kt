package sp.phone.ui.adapter

import android.content.Context
import sp.phone.util.ImageUtils.sDefaultAvatar
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import gov.anzong.androidnga.R
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import coil.load
import coil.transform.CircleCropTransformation
import sp.phone.common.User
import sp.phone.util.ImageUtils

class BlackListAdapter(private val mContext: Context, private val mUserList: List<User>) :
    RecyclerView.Adapter<BlackListAdapter.UserViewHolder>() {
    private var mOnClickListener: View.OnClickListener? = null

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var userNameView: TextView
        var avatarView: ImageView

        init {
            userNameView = itemView.findViewById(R.id.user_name)
            avatarView = itemView.findViewById(R.id.avatar)
        }
    }

    fun setOnClickListener(listener: View.OnClickListener?) {
        mOnClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val convertView = LayoutInflater.from(mContext)
            .inflate(R.layout.fragment_settings_black_list_item, parent, false)
        val holder = UserViewHolder(convertView)
        holder.itemView.setOnClickListener(mOnClickListener)
        holder.avatarView.load(sDefaultAvatar){
            transformations(CircleCropTransformation())
        }
        return holder
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.userNameView.text = mUserList[position].nickName
        holder.itemView.tag = mUserList[position]
    }

    override fun getItemCount(): Int {
        return mUserList.size
    }
}