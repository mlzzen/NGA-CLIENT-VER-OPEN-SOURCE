package android.preference

import android.R
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView

/**
 * Created by Justwen on 2017/7/16.
 */
class ListSummaryPreference : ListPreference {
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?) : super(context) {}

    override fun onBindView(view: View) {
        super.onBindView(view)
        val summaryView = view.findViewById<View>(R.id.summary) as TextView
        summaryView.visibility = View.VISIBLE
        summaryView.text = entry
    }
}