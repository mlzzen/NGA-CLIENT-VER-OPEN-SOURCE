package nosc.ui.view

import android.content.Context
import android.util.AttributeSet
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.res.stringResource
import gov.anzong.androidnga.R
import nosc.ui.NOSCTheme

/**
 * @author Yricky
 * @date 2022/3/31
 */
class EmptyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AbstractComposeView(context, attrs, defStyle) {
    var text by mutableStateOf(context.getString(R.string.error_load_failed))
    var extraContent:@Composable ColumnScope.()->Unit by mutableStateOf({})
    @Composable
    override fun Content() {
        NOSCTheme {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text, color = MaterialTheme.colors.onBackground, maxLines = 2)
                    extraContent(this)
                }
            }
        }
    }
}