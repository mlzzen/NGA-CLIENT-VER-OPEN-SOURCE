package nosc.activity.login

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import gov.anzong.androidnga.activity.BaseActivity
import gov.anzong.androidnga.arouter.ARouterConstants
import gov.anzong.androidnga.fragment.BaseFragment

@Route(path = ARouterConstants.ACTIVITY_LOGIN)
class LoginActivity : BaseActivity() {
    private lateinit var mLoginFragment: BaseFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mLoginFragment = LoginWebFragment()
        supportFragmentManager.beginTransaction().replace(android.R.id.content, mLoginFragment).commit()
        supportActionBar?.let{ actionBar ->
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowHomeEnabled(true)
            actionBar.subtitle = LoginWebFragment.URL_LOGIN
        }
    }

    override fun onBackPressed() {
        if (!mLoginFragment.onBackPressed()) {
            super.onBackPressed()
        }
    }

    override fun finish() {
        setResult(RESULT_OK)
        super.finish()
    }
}