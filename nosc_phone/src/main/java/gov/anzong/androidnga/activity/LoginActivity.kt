package gov.anzong.androidnga.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import gov.anzong.androidnga.arouter.ARouterConstants
import gov.anzong.androidnga.fragment.BaseFragment
import gov.anzong.androidnga.fragment.LoginWebFragment

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