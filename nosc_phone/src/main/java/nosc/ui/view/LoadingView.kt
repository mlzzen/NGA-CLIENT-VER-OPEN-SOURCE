package nosc.ui.view

import android.content.Context
import android.util.AttributeSet
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.unit.dp
import nosc.ui.NOSCTheme
import sp.phone.util.StringUtils

/**
 * @author Yricky
 * @date 2022/3/31
 */
class LoadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AbstractComposeView(context, attrs, defStyle) {
    @Composable
    override fun Content() {
        NOSCTheme {
            LoadingContent(StringUtils.getSaying())
        }
    }
}

@Composable
private fun LoadingContent(saying:String = "爱国，敬业，诚信，友善"){
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background), contentAlignment = Alignment.Center){
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MaterialTheme.colors.secondary, modifier = Modifier
                .size(64.dp)
                .padding(4.dp))
            Text(text = saying, color = MaterialTheme.colors.onBackground)
        }
    }
}