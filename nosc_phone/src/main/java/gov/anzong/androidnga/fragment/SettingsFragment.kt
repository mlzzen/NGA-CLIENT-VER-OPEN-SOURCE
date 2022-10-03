package gov.anzong.androidnga.fragment

import android.content.Context
import androidx.preference.PreferenceFragmentCompat
import android.os.Bundle
import nosc.utils.PreferenceKey
import gov.anzong.androidnga.R
import sp.phone.theme.ThemeManager
import gov.anzong.androidnga.fragment.dialog.AlertDialogFragment
import android.content.DialogInterface
import gov.anzong.androidnga.activity.SettingsActivity
import nosc.utils.ThreadUtils
import android.content.Intent
import android.os.Build
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceGroup
import gov.anzong.androidnga.activity.LauncherSubActivity
import nosc.utils.ContextUtils
import sp.phone.common.UserManagerImpl
import nosc.utils.uxUtils.ToastUtils
import org.apache.commons.io.FileUtils
import sp.phone.util.ForumUtils
import java.io.IOException

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = PreferenceKey.PERFERENCE
        setPreferencesFromResource(R.xml.settings, rootKey)
        configPreference()
        mapping(preferenceScreen)
    }

    private fun mapping(group: PreferenceGroup) {
        for (i in 0 until group.preferenceCount) {
            val preference = group.getPreference(i)
            if (preference is PreferenceGroup) {
                mapping(preference)
            } else {
                preference.onPreferenceChangeListener = this
            }
        }
    }

    private fun configPreference() {
        findPreference<ListPreference>(PreferenceKey.KEY_NGA_DOMAIN)?.setSummaryProvider {
            ForumUtils.getApiDomain()
        }
        findPreference<ListPreference>(PreferenceKey.KEY_NGA_DOMAIN_BROWSER)?.setSummaryProvider {
            ForumUtils.getBrowserDomain()
        }
        findPreference<ListPreference>(PreferenceKey.MATERIAL_THEME)?.setSummaryProvider {
            ThemeManager.getInstance().themeName
        }
        findPreference<Preference>(PreferenceKey.NIGHT_MODE)?.isEnabled =
            (!ThemeManager.getInstance().isNightModeFollowSystem) || Build.VERSION.SDK_INT < 29
        findPreference<Preference>(PreferenceKey.KEY_NIGHT_MODE_FOLLOW_SYSTEM)?.isEnabled =
            Build.VERSION.SDK_INT >=29
        findPreference<Preference>(PreferenceKey.KEY_CLEAR_CACHE)?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                showClearCacheDialog()
                true
            }
    }

    private fun showClearCacheDialog() {
        val dialogFragment = AlertDialogFragment.create("确认要清除缓存吗？")
        dialogFragment.setPositiveClickListener { _: DialogInterface?, _: Int -> clearCache() }
        dialogFragment.show(requireActivity().supportFragmentManager, "clear_cache")
    }

    override fun onResume() {
        requireActivity().setTitle(R.string.menu_setting)
        super.onResume()
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        when (preference.key) {
            PreferenceKey.NIGHT_MODE -> SettingsActivity.sRecreated = true
            PreferenceKey.KEY_NIGHT_MODE_FOLLOW_SYSTEM -> {
                findPreference<Preference>(PreferenceKey.NIGHT_MODE)!!.isEnabled =
                    java.lang.Boolean.FALSE == newValue
                SettingsActivity.sRecreated = true
            }
            PreferenceKey.MATERIAL_THEME -> {
                SettingsActivity.sRecreated = true
                ThreadUtils.postOnMainThreadDelay({
                    requireActivity().recreate()
                }, 200)
            }
            else -> {}
        }
        return true
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when (preference.key) {
            PreferenceKey.ADJUST_SIZE, PreferenceKey.PREF_USER, PreferenceKey.PREF_BLACK_LIST, "pref_keyword" -> {
                val intent = Intent(activity, LauncherSubActivity::class.java)
                intent.putExtra("fragment", preference.fragment)
                startActivity(intent)
            }
            else -> return super.onPreferenceTreeClick(preference)
        }
        return true
    }

    private fun clearCache() {
        ThreadUtils.postOnSubThread {
            // 清除avatar数据
            UserManagerImpl.getInstance().clearAvatarUrl()
            // 清除之前的使用过的awp缓存数据
            try {
                FileUtils.deleteDirectory(
                    ContextUtils.getContext().getDir("awp", Context.MODE_PRIVATE)
                )
                FileUtils.deleteDirectory(ContextUtils.getContext().externalCacheDir)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        ToastUtils.success("缓存清除成功")
    }
}