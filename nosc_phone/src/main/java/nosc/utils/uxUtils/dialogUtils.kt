package nosc.utils.uxUtils

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog


/**
 * @author Yricky
 * @date 2022/4/1
 */

fun Context.showConfirmDialog(message: CharSequence, action: Runnable) {
    try {
        AlertDialog.Builder(this).setMessage(message)
            .setPositiveButton(android.R.string.ok) { dialog, which -> action.run() }
            .setNegativeButton(android.R.string.cancel, null).show()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}