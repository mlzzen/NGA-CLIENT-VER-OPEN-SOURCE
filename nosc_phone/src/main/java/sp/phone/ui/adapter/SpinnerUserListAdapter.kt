package sp.phone.ui.adapter

import android.content.Context
import nosc.utils.PreferenceKey
import android.view.ViewGroup
import android.widget.TextView
import android.util.TypedValue
import android.view.View
import android.widget.BaseAdapter
import sp.phone.common.User
import sp.phone.common.UserManagerImpl
import java.lang.RuntimeException

open class SpinnerUserListAdapter(protected var mContext: Context) : BaseAdapter(), PreferenceKey {
    @JvmField
    protected var mUserList: List<User>? = UserManagerImpl.getInstance().userList
    override fun getCount(): Int {
        return if (mUserList == null) 0 else mUserList!!.size
    }

    override fun getItem(position: Int): Any {
        return mUserList!![position]
    }

    override fun getItemId(position: Int): Long {
        val uid = mUserList!![position].userId
        var ret: Long = 0
        try {
            ret = java.lang.Long.valueOf(uid)
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }
        return ret
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView: View? = convertView
        if (convertView == null) {
            convertView = TextView(mContext)
        }
        (convertView as TextView).text = mUserList!![position].nickName
        convertView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30f)
        return convertView
    }

}