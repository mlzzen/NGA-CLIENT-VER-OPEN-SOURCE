package sp.phone.param

import android.text.TextUtils
import android.util.Base64
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import gov.anzong.androidnga.R
import gov.anzong.androidnga.base.util.ContextUtils
import nosc.api.constants.ApiConstants
import sp.phone.mvp.model.entity.ThreadPageInfo
import sp.phone.util.StringUtils
import java.math.BigInteger
import java.util.*

object TopicTitleHelper {
    private fun AnnotatedString.Builder.handleOldFormat(misc: String, titleLength: Int) {
        if (misc == "~1~~" || misc == "~~~1") {
            addStyle(
                SpanStyle(fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold),
                0,
                titleLength
            )
        } else {
            val miscArray = misc.lowercase().split("~".toRegex()).toTypedArray()
            for (aMiscArray in miscArray) {
                when (aMiscArray) {
                    "green" -> SpanStyle(color = Color(ContextUtils.getColor(R.color.title_green)))
                    "blue" -> SpanStyle(color = Color(ContextUtils.getColor(R.color.title_blue)))
                    "red" -> SpanStyle(color = Color(ContextUtils.getColor(R.color.title_red)))
                    "orange" -> SpanStyle(color = Color(ContextUtils.getColor(R.color.title_orange)))
                    "sliver" -> SpanStyle(color = Color(ContextUtils.getColor(R.color.silver)))
                    "b" -> SpanStyle(fontWeight = FontWeight.Bold)
                    "i" -> SpanStyle(fontStyle = FontStyle.Italic)
                    "u" -> SpanStyle(textDecoration = TextDecoration.Underline)
                    else -> null
                }?.let{
                    addStyle(
                        it,
                        0,
                        titleLength
                    )
                }


            }
        }
    }

    private fun AnnotatedString.Builder.handleNewFormat(misc: String, titleLength: Int) {
        val bytes = Base64.decode(misc, Base64.DEFAULT)
        if (bytes != null) {
            var pos = 0
            while (pos < bytes.size) {
                // 1 表示主题bit数据
                if (bytes[pos] == 1.toByte()) {
                    val miscStr = StringUtils.toBinaryArray(bytes).substring(8)
                    val miscValue = BigInteger(miscStr, 2).toInt()
                    when {
                        miscValue and ApiConstants.MASK_FONT_GREEN == ApiConstants.MASK_FONT_GREEN -> SpanStyle(color = Color(ContextUtils.getColor(R.color.title_green)))
                        miscValue and ApiConstants.MASK_FONT_BLUE == ApiConstants.MASK_FONT_BLUE -> SpanStyle(color = Color(ContextUtils.getColor(R.color.title_blue)))
                        miscValue and ApiConstants.MASK_FONT_RED == ApiConstants.MASK_FONT_RED -> SpanStyle(color = Color(ContextUtils.getColor(R.color.title_red)))
                        miscValue and ApiConstants.MASK_FONT_ORANGE == ApiConstants.MASK_FONT_ORANGE -> SpanStyle(color = Color(ContextUtils.getColor(R.color.title_orange)))
                        miscValue and ApiConstants.MASK_FONT_SILVER == ApiConstants.MASK_FONT_SILVER -> SpanStyle(color = Color(ContextUtils.getColor(R.color.silver)))
                        else -> null
                    }?.let {
                        addStyle(
                            it,
                            0,
                            titleLength
                        )
                    }
                    addStyle(SpanStyle(
                        fontWeight = if(miscValue and ApiConstants.MASK_FONT_BOLD == ApiConstants.MASK_FONT_BOLD) FontWeight.Bold else null,
                        fontStyle = if(miscValue and ApiConstants.MASK_FONT_ITALIC == ApiConstants.MASK_FONT_ITALIC) FontStyle.Italic else null
                    ),0,titleLength)

                    if (miscValue and ApiConstants.MASK_FONT_UNDERLINE == ApiConstants.MASK_FONT_UNDERLINE) {
                        addStyle(
                            SpanStyle(textDecoration = TextDecoration.Underline),
                            0,
                            titleLength
                        )
                    }
                }
                pos += 4
            }
        }
    }

    fun handleTitleFormat(entry: ThreadPageInfo): AnnotatedString {
        return buildAnnotatedString {
            val title = StringUtils.removeBrTag(StringUtils.unEscapeHtml(entry.subject))
            append(title)
            val type = entry.type
            val titleLength = title.length
            if (type and ApiConstants.MASK_TYPE_ATTACHMENT == ApiConstants.MASK_TYPE_ATTACHMENT) {
                val typeStr = " +"
                append(buildAnnotatedString {
                    append(typeStr)
                    addStyle(
                        SpanStyle(Color(ContextUtils.getColor(R.color.title_orange))),0, typeStr.length,
                    )
                })


            }
            if (type and ApiConstants.MASK_TYPE_LOCK == ApiConstants.MASK_TYPE_LOCK) {
                val typeStr = " [锁定]"
                append(buildAnnotatedString {
                    append(typeStr)
                    addStyle(
                        SpanStyle(Color(ContextUtils.getColor(R.color.title_red))),0, typeStr.length,
                    )
                })
            }
            if (type and ApiConstants.MASK_TYPE_ASSEMBLE == ApiConstants.MASK_TYPE_ASSEMBLE) {
                val typeStr = " [锁定]"
                append(buildAnnotatedString {
                    append(typeStr)
                    addStyle(
                        SpanStyle(Color(ContextUtils.getColor(R.color.title_blue))),0, typeStr.length,
                    )
                })
            }
            if (!TextUtils.isEmpty(entry.topicMisc)) {
                val misc = entry.topicMisc
                // ~ 开头的为旧格式
                if (misc.startsWith("~")) {
                    handleOldFormat( misc, titleLength)
                } else {
                    handleNewFormat( misc, titleLength)
                }
            }
            if (!TextUtils.isEmpty(entry.board)) {
                append(buildAnnotatedString {
                    val str = "  [${entry.board}]"
                    append(str)
                    addStyle(
                        SpanStyle(Color(ContextUtils.getColor(R.color.text_color_disabled))),0, str.length,
                    )
                })
            }
        }

    }
}