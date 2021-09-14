package gov.nosc.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.database.DataSetObserver
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.NumberPicker
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager.widget.ViewPager


/**
 * TODO: document your custom view class.
 */
class PageSelector : ConstraintLayout {
    private var _viewPager:ViewPager? = null
    private val currentPage:TextView
    private val totalPage:TextView
    private val onPageChangeListener:ViewPager.OnPageChangeListener
    private val dataObserver:DataSetObserver
    constructor(context: Context) : this(context,null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs,0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ){
        LayoutInflater.from(context).inflate(R.layout.layout_page_selector,this)
        currentPage = findViewById(R.id.tv_page_current)
        totalPage = findViewById(R.id.tv_page_total)
        onPageChangeListener = object :ViewPager.OnPageChangeListener{
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            @SuppressLint("SetTextI18n")
            override fun onPageSelected(position: Int) {
                currentPage.text = "${position+1}"
                totalPage.text = "${_viewPager?.adapter?.count}"
            }
            override fun onPageScrollStateChanged(state: Int) {}
        }
        dataObserver = object :DataSetObserver(){
            override fun onChanged() {
                _viewPager?.apply {
                    onPageChangeListener.onPageSelected(currentItem)
                }
            }
        }
    }

    fun bindViewPager(viewPager: ViewPager?){
        _viewPager?.removeOnPageChangeListener(onPageChangeListener)
        _viewPager?.adapter?.unregisterDataSetObserver(dataObserver)
        viewPager?.addOnPageChangeListener(onPageChangeListener)
        viewPager?.adapter?.registerDataSetObserver(dataObserver)
        _viewPager = viewPager
        onPageChangeListener.onPageSelected(_viewPager?.currentItem ?: return)
    }

}