package com.castiel.common.widget

import android.content.Context
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

/**
 * 高亮显示TextView
 * @author jiayu.liu
 */
data class TextItem(val content: String, val color: Int)

class ColoredTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    // 存储内容和颜色
    private var textItems: List<TextItem> = listOf()

    // 设置列表数据
    fun setTextItems(items: List<TextItem>) {
        textItems = items
        val spannableText = SpannableString(buildTextFromItems())

        // 为每个部分应用不同的颜色
        var currentStart = 0
        textItems.forEach {
            val end = currentStart + it.content.length
            spannableText.setSpan(
                ForegroundColorSpan(it.color),
                currentStart,
                end,
                android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            currentStart = end
        }

        // 设置颜色后的文本
        text = spannableText
    }

    // 拼接文本内容
    private fun buildTextFromItems(): String {
        return textItems.joinToString("") { it.content }
    }
}