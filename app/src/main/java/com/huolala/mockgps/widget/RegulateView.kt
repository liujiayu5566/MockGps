package com.huolala.mockgps.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import com.blankj.utilcode.util.ClickUtils
import com.huolala.mockgps.R
import com.huolala.mockgps.utils.HandlerUtils
import okhttp3.internal.format

class RegulateView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    LinearLayout(context, attrs, defStyleAttr) {

    /**
     * 默认值
     */
    private var valueTextStr = 5

    /**
     * 单位
     */
    private var unitTextStr = "米"

    /**
     * 是否颜色警示
     */
    private var isColorWarn = false

    /**
     * 使用倍数增加
     */
    private var useMultiple = false

    /**
     * 最小调整阈值
     */
    private var minThreshold = 1

    /**
     * 使用倍数增长 默认：1倍
     */
    private var useMultipleNum = 1

    /**
     * 最小阈值 默认为：最小调整阈值
     */
    private var minValue = minThreshold

    /**
     * 最大阈值  确认导航信息后传入
     */
    var maxValue = Int.MAX_VALUE

    /**
     * 处于长按之后等待阶段   该阶段无法使用#setCurValue(Int)方法
     */
    var isLongClickWait = false

    private val valueTextView: AppCompatTextView

    /**
     * 8s后没有点击  恢复更新setCurValue
     */
    private val runnable: Runnable = Runnable {
        isLongClickWait = false
    }

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
        minValue = typedArray
            .getInteger(R.styleable.RegulateView_minValue, minThreshold)
        unitTextStr = typedArray.getString(R.styleable.RegulateView_unit) ?: unitTextStr
        isColorWarn = typedArray.getBoolean(R.styleable.RegulateView_isColorWarn, false)
        useMultiple = typedArray.getBoolean(R.styleable.RegulateView_useMultiple, false)
        typedArray.recycle()

        LayoutInflater.from(context).inflate(R.layout.layout_regulate_value, this, true).apply {
            valueTextView = findViewById(R.id.tv_value)
            setFormatStr()

            val addView = findViewById<LongClickImageButton>(R.id.iv_add)
            ClickUtils.applySingleDebouncing(addView, 200) {
                addValue()
                HandlerUtils.INSTANCE.postDelayed(runnable, 8 * 1000)
            }
            addView.listener = object : LongClickImageButton.LongClickListener {
                private var count = 0

                override fun touchLongClickEvent() {
                    if (useMultiple) {
                        count += 1
                        when (count) {
                            10 -> {
                                useMultipleNum = count
                                addView.setDelayMillis(500L)
                            }

                            else -> {
                                if (count > 10 && count % 5 == 0) {
                                    useMultipleNum *= 2
                                }
                            }
                        }
                    }
                    addValue()
                }

                override fun touchLongClickFinish() {
                    if (useMultiple) {
                        useMultipleNum = 1
                        count = 0
                        addView.setDelayMillis(150L)
                    }
                    HandlerUtils.INSTANCE.postDelayed(runnable, 8 * 1000)
                }
            }

            val subtractView = findViewById<LongClickImageButton>(R.id.iv_subtract)
            ClickUtils.applySingleDebouncing(subtractView, 200) {
                subtractValue()
                HandlerUtils.INSTANCE.postDelayed(runnable, 8 * 1000)
            }
            subtractView.listener = object : LongClickImageButton.LongClickListener {
                private var count = 0
                override fun touchLongClickEvent() {
                    if (useMultiple) {
                        count += 1
                        when (count) {
                            10 -> {
                                useMultipleNum = count
                                subtractView.setDelayMillis(500L)
                            }

                            else -> {
                                if (count > 10 && count % 5 == 0) {
                                    useMultipleNum *= 2
                                }
                            }
                        }
                    }
                    subtractValue()
                }

                override fun touchLongClickFinish() {
                    if (useMultiple) {
                        useMultipleNum = 1
                        count = 0
                        subtractView.setDelayMillis(150L)
                    }
                    HandlerUtils.INSTANCE.postDelayed(runnable, 8 * 1000)
                }
            }
        }
        gravity = Gravity.CENTER_VERTICAL
    }

    /**
     * 减少
     */
    private fun subtractValue() {
        valueTextStr -= minThreshold * useMultipleNum
        updateValue()
    }

    /**
     * 增加
     */
    private fun addValue() {
        valueTextStr += minThreshold * useMultipleNum
        updateValue()
    }

    private fun updateValue() {
        isLongClickWait = true
        HandlerUtils.INSTANCE.removeCallbacks(runnable)
        if (isColorWarn) {
            valueTextView.setTextColor(Color.RED)
        }
        setFormatStr()
    }

    fun getCurValue(): Int {
        return valueTextStr
    }

    fun updateCurValue(value: Int) {
        if (isLongClickWait) {
            return
        }
        valueTextStr = value
        if (isColorWarn) {
            valueTextView.setTextColor(Color.WHITE)
        }
        setFormatStr()
    }

    /**
     * 清空颜色提醒状态
     */
    fun clearLongClickWait() {
        isLongClickWait = false
        HandlerUtils.INSTANCE.removeCallbacks(runnable)
        if (isColorWarn) {
            valueTextView.setTextColor(Color.WHITE)
        }
    }

    private fun setFormatStr() {
        valueTextStr = if (valueTextStr < minValue) minValue else valueTextStr
        valueTextStr = if (valueTextStr > maxValue) maxValue else valueTextStr
        valueTextView.text = format("%d$unitTextStr", valueTextStr)
    }
}