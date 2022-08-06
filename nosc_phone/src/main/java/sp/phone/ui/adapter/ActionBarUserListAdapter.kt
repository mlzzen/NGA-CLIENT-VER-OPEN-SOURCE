package sp.phone.ui.adapter

import android.content.Context
import sp.phone.ui.adapter.SpinnerUserListAdapter
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import sp.phone.theme.ThemeManager
import androidx.core.content.ContextCompat
import gov.anzong.androidnga.R

/**
 * Created by GDB437 on 9/3/13,nga_phone_base_3.0
 */
class ActionBarUserListAdapter(context: Context) : SpinnerUserListAdapter(
    context
) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
        }
        (convertView as TextView).text = mUserList!![position].nickName
        convertView.setBackgroundColor(ThemeManager.getInstance().getPrimaryColor(mContext))
        convertView.setTextColor(ContextCompat.getColor(mContext, R.color.toolbar_text_color))
        return convertView
    }
}