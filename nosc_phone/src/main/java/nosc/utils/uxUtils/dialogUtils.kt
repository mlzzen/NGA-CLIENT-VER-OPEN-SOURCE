package nosc.utils.uxUtils

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity

/**
 * @author Yricky
 * @date 2022/4/1
 */

fun showConfirmDialog(activity: FragmentActivity, message: CharSequence, action: Runnable) {
    try {
        object: DialogFragment() {
            override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
                val builder = AlertDialog.Builder(requireContext());
                builder.setMessage(message)
                    .setPositiveButton(android.R.string.ok) { dialog, which -> action.run() }
                    .setNegativeButton(android.R.string.cancel, null);
                return builder.create();
            }
        }.show(activity.supportFragmentManager, null);
    } catch (e: Exception) {
        e.printStackTrace()
    }
}