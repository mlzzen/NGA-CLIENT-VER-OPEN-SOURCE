package nosc.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gov.anzong.androidnga.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nosc.utils.accentColor
import nosc.utils.backgroundColor
import nosc.utils.backgroundColor2
import nosc.utils.primaryColor
import kotlin.coroutines.CoroutineContext

/**
 * @author Yricky
 * @date 2022/3/31
 */

val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(0.dp)
)

val Typography = Typography(
    body1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )
    /* Other default text styles to override
    button = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500,
        fontSize = 14.sp
    ),
    caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
    */
)

@Composable
fun NOSCTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val context = LocalContext.current

    val darkColorPalette by remember {
        mutableStateOf(
            darkColors(
                primary = Color(context.primaryColor()),
                primaryVariant = Color(context.primaryColor()),
                secondary = Color(context.accentColor()),
                onPrimary = Color.White,
                onBackground = Color.Gray,
                onSurface = Color.Gray,
                onSecondary = Color.Gray,
                background = Color(context.backgroundColor()),
                surface = Color(context.backgroundColor2())
            )
        )
    }

    val lightColorPalette by remember {
        mutableStateOf(
            lightColors(
                primary = Color(context.primaryColor()),
                primaryVariant = Color(context.primaryColor()),
                secondary = Color(context.accentColor()),
                background = Color(context.backgroundColor()),
                surface = Color(context.backgroundColor2())
            )
        )
    }

    val colors = if (darkTheme) {
        darkColorPalette
    } else {
        lightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = {
            Surface(color = MaterialTheme.colors.background) {
                content()
            }
        }
    )
}

@Composable
fun <T> loadState(
    init:T,
    key:Any? = null,
    context:CoroutineContext = Dispatchers.Default,
    loader:()->T
): State<T> {
    val state = remember {
        mutableStateOf(init)
    }
    LaunchedEffect(key){
        state.value = withContext(context){
            loader()
        }
    }
    return state
}