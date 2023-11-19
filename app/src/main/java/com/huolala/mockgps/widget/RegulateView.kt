package com.huolala.mockgps.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import com.blankj.utilcode.util.ClickUtils
import com.huolala.mockgps.R
import okhttp3.internal.format

class RegulateView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    LinearLayout(context, attrs, defStyleAttr) {

    /**
     * 默认值
     */
    private var valueTextStr = 10

    /**
     * 单位
     */
    private var unitTextStr = "米"

    /**
     * 最小调整阈值
     */
    private var minThreshold = 1
    private val valueTextView: AppCompatTextView

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)


    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RegulateView)
        valueTextStr =
            typedArray
                .getInteger(R.styleable.RegulateView_defaultValue, valueTextStr)
        minThreshold =
            typedArray
                .getInteger(R.styleable.RegulateView_minThreshold, minThreshold)
        unitTextStr = typedArray.getString(R.styleable.RegulateView_unit) ?: unitTextStr
        typedArray.recycle()

        LayoutInflater.from(context).inflate(R.layout.layout_regulate_value, this, true).apply {
            valueTextView = findViewById<AppCompatTextView>(R.id.tv_value)
            setFormatStr()
            ClickUtils.applySingleDebouncing(findViewById<View>(R.id.iv_subtract), 200) {
                valueTextStr -= minThreshold
                setFormatStr()
            }
            ClickUtils.applySingleDebouncing(findViewById<View>(R.id.iv_add), 200) {
                valueTextStr += minThreshold
                setFormatStr()
            }
        }
        gravity = Gravity.CENTER_VERTICAL
    }

    fun getCurValue(): Int {
        return valueTextStr
    }

    fun setCurValue(value: Int) {
        valueTextStr = value
        setFormatStr()
    }

    private fun setFormatStr() {
        valueTextStr = if (valueTextStr <= 0) minThreshold else valueTextStr
        valueTextView.text = format("%d$unitTextStr", valueTextStr)
    }
}