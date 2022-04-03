package nosc.utils.uxUtils

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.savedstate.ViewTreeSavedStateRegistryOwner
import nosc.ui.NOSCTheme

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

fun Context.showComposeDialog(content:@Composable (AppCompatDialog)->Unit):Boolean{
    val activity: AppCompatActivity = this as? AppCompatActivity ?: return false
    val dialog: AppCompatDialog = AppCompatDialog(activity).apply {
        ViewTreeLifecycleOwner.set(window?.decorView ?:return false ,activity)
        ViewTreeSavedStateRegistryOwner.set(window?.decorView ?:return false ,activity)
        setContentView(ComposeView(activity).also {
            it.setContent{
                NOSCTheme {
                    content(this@apply)
                }

            }
        })
    }
    dialog.show()
    return true
}