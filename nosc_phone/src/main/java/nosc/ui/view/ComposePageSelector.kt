package nosc.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.database.DataSetObserver
import android.util.AttributeSet
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.unit.sp
import androidx.viewpager.widget.ViewPager
import nosc.ui.NOSCTheme

/**
 * @author Yricky
 * @date 2022/3/31
 */
class ComposePageSelector @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AbstractComposeView(context, attrs, defStyle) {
    private var _viewPager:ViewPager? = null
    private val onPageChangeListener: ViewPager.OnPageChangeListener
    private val dataObserver: DataSetObserver

    private var currentPage by mutableStateOf("")
    private var totalPage by mutableStateOf("")


    init {
        onPageChangeListener = object :ViewPager.OnPageChangeListener{
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            @SuppressLint("SetTextI18n")
            override fun onPageSelected(position: Int) {
                currentPage = "${position+1}"
                totalPage = "${_viewPager?.adapter?.count}"
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

    @Composable
    override fun Content() {
        NOSCTheme {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center){
                Text(text = "$currentPage/$totalPage", color = MaterialTheme.colors.onBackground, fontSize = 22.sp)
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