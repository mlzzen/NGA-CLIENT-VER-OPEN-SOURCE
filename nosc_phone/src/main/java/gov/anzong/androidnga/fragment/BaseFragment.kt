package gov.anzong.androidnga.fragment

import androidx.annotation.LayoutRes
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import gov.anzong.androidnga.R
import gov.anzong.androidnga.activity.BaseActivity

open class BaseFragment : Fragment {

    private val mActivityViewModelProvider: ViewModelProvider by lazy { ViewModelProvider(activity as FragmentActivity) }

    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId)
    constructor() : super()

    protected fun initToolbar() {
        val toolbar: Toolbar? = view?.findViewById(R.id.toolbar)
        if (toolbar != null && activity != null) {
            (activity as BaseActivity).setupToolbar(toolbar)
        }
    }

    protected fun setTitle(resId:Int){
        activity?.title = getString(resId)
    }

    protected fun setTitle(title: String) {
        activity?.title = title
    }

    fun getActivityViewModelProvider(): ViewModelProvider {
        return mActivityViewModelProvider
    }

    open fun finish() {
        activity?.finish()
    }

    protected open fun setupToolbar(toolbar: Toolbar?) {
        (activity as? BaseActivity)?.setupToolbar(toolbar)
    }

    open fun setResult(resultCode: Int) {
        activity?.setResult(resultCode)
    }

    open fun onBackPressed(): Boolean {
        return false
    }
}
