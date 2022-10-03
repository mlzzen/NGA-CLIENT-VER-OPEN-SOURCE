package gov.anzong.androidnga.activity

import android.os.Bundle
import android.view.View
import gov.anzong.androidnga.fragment.SettingsFragment
import android.view.ViewAnimationUtils
import kotlin.math.hypot

class SettingsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupFragment()
        setupActionBar()
        if (sRecreated) {
            window.setWindowAnimations(android.R.style.Animation_Toast)
            sRecreated = false
            setResult(RESULT_OK)
            findViewById<View>(android.R.id.content).post {
                startAnimation(findViewById(android.R.id.content))
            }
        }
    }

    private fun setupFragment() {
        val fm = supportFragmentManager
        var settingsFragment = fm.findFragmentByTag(SettingsFragment::class.java.simpleName)
        if (settingsFragment == null) {
            settingsFragment = SettingsFragment()
            fm.beginTransaction()
                .replace(android.R.id.content, settingsFragment, SettingsFragment::class.java.simpleName)
                .commit()
        }
    }

    private fun startAnimation(contentView: View) {
        val cx = contentView.width / 2
        val cy = contentView.height / 2
        val finalRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()
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