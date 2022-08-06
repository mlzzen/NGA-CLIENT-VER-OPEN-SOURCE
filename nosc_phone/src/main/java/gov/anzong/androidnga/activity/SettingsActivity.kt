package gov.anzong.androidnga.activity

import android.R
import android.os.Bundle
import android.view.View
import gov.anzong.androidnga.fragment.SettingsFragment
import android.view.ViewAnimationUtils

class SettingsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupFragment()
        setupActionBar()
        if (sRecreated) {
            window.setWindowAnimations(R.style.Animation_Toast)
            sRecreated = false
            setResult(RESULT_OK)
            findViewById<View>(R.id.content).post { startAnimation(findViewById(R.id.content)) }
        }
    }

    private fun setupFragment() {
        val fm = fragmentManager
        var settingsFragment = fm.findFragmentByTag(SettingsFragment::class.java.simpleName)
        if (settingsFragment == null) {
            settingsFragment = SettingsFragment()
            fm.beginTransaction()
                .replace(R.id.content, settingsFragment, SettingsFragment::class.java.simpleName)
                .commit()
        }
    }

    private fun startAnimation(contentView: View) {
        val cx = contentView.width / 2
        val cy = contentView.height / 2
        val finalRadius = Math.hypot(cx.toDouble(), cy.toDouble()).toFloat()
        ViewAnimationUtils.createCircularReveal(contentView, cx, cy, 0f, finalRadius).start()
    }

    override fun onDestroy() {
        sRecreated = false
        super.onDestroy()
    }

    companion object {
        @JvmField
        var sRecreated = false
    }
}