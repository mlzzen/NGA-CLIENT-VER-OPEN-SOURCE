package gov.anzong.androidnga.activity

import android.os.Bundle
import sp.phone.theme.ThemeManager
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem
import gov.anzong.androidnga.R
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import gov.anzong.androidnga.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import nosc.ui.NOSCTheme
import nosc.ui.loadState
import nosc.utils.ContextUtils
import nosc.utils.dateStringOf
import nosc.utils.forumDateStringOf
import nosc.utils.uxUtils.ToastUtils
import java.io.File
import java.nio.charset.Charset

class CrashReportActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setToolbarEnabled(true)
        super.onCreate(savedInstanceState)
        setContent {
            NOSCTheme {
                Column {
                    Row{
                        Image(painter = painterResource(id = R.drawable.ic_about_debug),
                            contentDescription = "")
                        Text(BuildConfig.VERSION_NAME)
                    }
                    val list by remember {
                        flow {
                            emit(
                                ContextUtils.getApplication().externalCacheDir?.listFiles { dir, name ->
                                    name.startsWith("CrashReport")
                                }?.asList()?.asReversed() ?: emptyList<File>()
                            )
                        }
                    }.collectAsState(initial = emptyList())
                    LazyColumn{
                        items(list){
                            Surface(
                                Modifier.padding(4.dp),
                                color = MaterialTheme.colors.surface,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                var expand  by remember {
                                    mutableStateOf(false)
                                }
                                val msg by loadState(init = "", context = Dispatchers.IO) {
                                    it.readText()
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateContentSize()
                                        .clickable { expand = !expand }
                                        .padding(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ){
                                    Icon(painterResource(id = R.drawable.ic_license),"")
                                    Column(Modifier.padding(4.dp)) {
                                        val info = remember {
                                            it.name.removePrefix("CrashReport_")
                                                .removeSuffix(".crash")
                                                .split("_")
                                        }
                                        Text(text = dateStringOf(info[0].toLong()))
                                        Text(text = "thread:${info[1]}", color = MaterialTheme.colors.secondary)
                                        Text(text = info[2] +
                                                if(BuildConfig.VERSION_CODE.toString() == info[2])"(Current)" else "",
                                            color = MaterialTheme.colors.secondary
                                        )
                                        Text(
                                            text = msg,
                                            maxLines = if(expand) Int.MAX_VALUE else 1
                                        )
                                    }
                                }
                            }

                        }
                    }
                }
            }

        }
    }


    private fun buildCrashCard(): MaterialAboutCard {
        val builder = MaterialAboutCard.Builder()
        builder.addItem(MaterialAboutActionItem.Builder()
            .text("版本")
            .subText(BuildConfig.VERSION_NAME)
            .icon(R.drawable.ic_about_debug)
            .setOnLongClickAction {
                throw RuntimeException("Crash test")
            }
            .build())
        ContextUtils.getApplication().externalCacheDir?.listFiles { dir, name ->
            name.startsWith("CrashReport")
        }?.forEach {
            builder.addItem(MaterialAboutActionItem.Builder()
                .text(it.name.removePrefix("CrashReport_").removeSuffix(".crash"))
                .subText(it.readText(Charset.defaultCharset()))
                .setOnLongClickAction {
                    it.delete()
                    ToastUtils.info("Deleted ${it.name}")
                }
                .icon(R.drawable.ic_license)
                .build())
        }

        return builder.build()
    }
}