package com.castio.common.widget

import android.content.Context
import android.graphics.Paint
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.blankj.utilcode.util.LogUtils

/**
 * 中英文混排自动换行
 * 需要设置maxWidth才能做到适配
 *
 * @author jiayu.liu
 */
open class MixedTextView : AppCompatTextView {
    private var mEnabled = true

    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    )

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!, attrs, defStyle
    )

    fun setAutoSplitEnabled(enabled: Boolean) {
        mEnabled = enabled
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
    }

    override fun setText(text: CharSequence, type: BufferType) {
        if (mEnabled) {
            val newText = autoSplitText(text)
            if (!TextUtils.isEmpty(newText)) {
                super.setText(newText, type)
            }
        } else {
            super.setText(text, type)
        }
    }

    private fun autoSplitText(text: CharSequence): String {
        if (TextUtils.isEmpty(text)) {
            return ""
        }
        //原始文本
        val rawText = text.toString()
        //paint，包含字体等信息
        val tvPaint: Paint = paint
        //控件可用宽度
        val maxWidth: Int = maxWidth

        val tvWidth = (maxWidth - paddingLeft - paddingRight).toFloat()

        //将原始文本按行拆分
        val rawTextLines = rawText.replace("\r".toRegex(), "").split("\n").toTypedArray()
        val sbNewText = StringBuilder()
        for (rawTextLine in rawTextLines) {
            if (tvPaint.measureText(rawTextLine) <= tvWidth) {
                //如果整行宽度在控件可用宽度之内，就不处理了
                sbNewText.append(rawTextLine)
            } else {
                //如果整行宽度超过控件可用宽度，则按字符测量，在超过可用宽度的前一个字符处手动换行
                var lineWidth = 0f
                var cnt = 0
                while (cnt != rawTextLine.length) {
                    val ch = rawTextLine[cnt]
                    lineWidth += tvPaint.measureText(ch.toString())
                    if (lineWidth <= tvWidth) {
                        sbNewText.append(ch)
                    } else {
                        sbNewText.append("\n")
                        lineWidth = 0f
                        --cnt
                    }
                    ++cnt
                }
            }
            sbNewText.append("\n")
        }

        //把结尾多余的\n去掉
        if (!rawText.endsWith("\n")) {
            sbNewText.deleteCharAt(sbNewText.length - 1)
        }
        return sbNewText.toString()
    }
}