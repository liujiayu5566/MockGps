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

class SpeedSettingView : LinearLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    /**
     * 默认间距：5
     */
    private var speedTextStr = 5

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_speed_setting, this, true).apply {
            val speedText = findViewById<AppCompatTextView>(R.id.tv_speed)
            ClickUtils.applySingleDebouncing(findViewById<View>(R.id.iv_subtract), 200) {
                speedText.text = format("%d米", --speedTextStr)
            }
            ClickUtils.applySingleDebouncing(findViewById<View>(R.id.iv_add), 200) {
                speedText.text = format("%d米", ++speedTextStr)
            }
        }
        gravity = Gravity.CENTER_VERTICAL
    }

    fun getCurSpeed(): Int {
        return speedTextStr
    }
}