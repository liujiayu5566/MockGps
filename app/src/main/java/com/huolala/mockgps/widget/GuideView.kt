package com.huolala.mockgps.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Constraints
import androidx.core.view.marginTop
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ClickUtils
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.SizeUtils
import com.huolala.mockgps.R
import com.huolala.mockgps.utils.MMKVUtils

/**
 * @author jiayu.liu
 */
class GuideView : ConstraintLayout {
    private var ivGuideArrows: View
    private var tvMsg: AppCompatTextView
    var listener: GuideViewListener? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        View.inflate(context, R.layout.layout_main_guide, this)
        ivGuideArrows = findViewById(R.id.iv_guide_arrows)
        tvMsg = findViewById(R.id.tv_msg)

        ClickUtils.applySingleDebouncing(
            findViewById<View>(R.id.tv_affirm)
        ) {
            (parent as ViewGroup).removeView(this@GuideView)
            listener?.onAffirm()
        }

        setBackgroundColor(ColorUtils.string2Int("#80000000"))
        isClickable = true
    }

    fun setGuideView(
        view: View?,
        gravity: Gravity = Gravity.LEFT2BOTTOM,
        msg: String = resources.getString(R.string.guide_hint),
        arrowMargin: Float = 10f
    ) {
        view?.let {
            tvMsg.text = msg

            val location = IntArray(2)
            view.getLocationOnScreen(location)
            val x = location[0]
            val y = location[1]

            addView(
                AppCompatImageView(context).apply {
                    id = R.id.guide_view
                    it.isDrawingCacheEnabled = true
                    setImageBitmap(it.drawingCache)
                },

                LayoutParams(
                    it.width,
                    it.height,
                ).apply {
                    leftToLeft = LayoutParams.PARENT_ID
                    topToTop = LayoutParams.PARENT_ID
                    marginStart = x
                    topMargin = y - BarUtils.getStatusBarHeight()
                }
            )

            when (gravity) {
                Gravity.LEFT2BOTTOM -> {
                    (ivGuideArrows.layoutParams as LayoutParams).let { layoutParams ->
                        layoutParams.topToBottom = R.id.guide_view
                        layoutParams.rightToRight = R.id.guide_view
                        layoutParams.rightMargin = it.width / 2
                        layoutParams.topMargin = SizeUtils.dp2px(arrowMargin)
                        ivGuideArrows.layoutParams = layoutParams
                    }
                    (tvMsg.layoutParams as LayoutParams).let { layoutParams ->
                        layoutParams.topToBottom = R.id.iv_guide_arrows
                        layoutParams.leftToLeft = R.id.iv_guide_arrows
                        layoutParams.rightToLeft = R.id.iv_guide_arrows
                        tvMsg.layoutParams = layoutParams
                    }
                    ivGuideArrows.scaleX = 1f
                    ivGuideArrows.scaleY = 1f
                }

                Gravity.RIGHT2BOTTOM -> {
                    (ivGuideArrows.layoutParams as LayoutParams).let { layoutParams ->
                        layoutParams.topToBottom = R.id.guide_view
                        layoutParams.leftToLeft = R.id.guide_view
                        layoutParams.leftMargin = it.width / 2
                        layoutParams.topMargin = SizeUtils.dp2px(arrowMargin)
                        ivGuideArrows.layoutParams = layoutParams
                    }
                    (tvMsg.layoutParams as LayoutParams).let { layoutParams ->
                        layoutParams.topToBottom = R.id.iv_guide_arrows
                        layoutParams.rightToRight = R.id.iv_guide_arrows
                        layoutParams.leftToRight = R.id.iv_guide_arrows
                        tvMsg.layoutParams = layoutParams
                    }
                    ivGuideArrows.scaleX = -1f
                    ivGuideArrows.scaleY = 1f
                }

                Gravity.LEFT2TOP -> {
                    (ivGuideArrows.layoutParams as LayoutParams).let { layoutParams ->
                        layoutParams.bottomToTop = R.id.guide_view
                        layoutParams.rightToRight = R.id.guide_view
                        layoutParams.rightMargin = it.width / 2
                        layoutParams.bottomMargin = SizeUtils.dp2px(arrowMargin)
                        ivGuideArrows.layoutParams = layoutParams
                    }
                    (tvMsg.layoutParams as LayoutParams).let { layoutParams ->
                        layoutParams.bottomToTop = R.id.iv_guide_arrows
                        layoutParams.leftToLeft = R.id.iv_guide_arrows
                        layoutParams.rightToLeft = R.id.iv_guide_arrows
                        tvMsg.layoutParams = layoutParams
                    }
                    ivGuideArrows.scaleY = -1f
                    ivGuideArrows.scaleX = 1f
                }

                Gravity.RIGHT2TOP -> {
                    (ivGuideArrows.layoutParams as LayoutParams).let { layoutParams ->
                        layoutParams.bottomToTop = R.id.guide_view
                        layoutParams.leftToLeft = R.id.guide_view
                        layoutParams.leftMargin = it.width / 2
                        layoutParams.bottomMargin = SizeUtils.dp2px(arrowMargin)
                        ivGuideArrows.layoutParams = layoutParams
                    }
                    (tvMsg.layoutParams as LayoutParams).let { layoutParams ->
                        layoutParams.bottomToTop = R.id.iv_guide_arrows
                        layoutParams.rightToRight = R.id.iv_guide_arrows
                        layoutParams.leftToRight = R.id.iv_guide_arrows
                        tvMsg.layoutParams = layoutParams
                    }
                    ivGuideArrows.scaleY = -1f
                    ivGuideArrows.scaleX = -1f
                }
            }
        }
    }

    enum class Gravity {
        LEFT2BOTTOM,
        RIGHT2BOTTOM,
        LEFT2TOP,
        RIGHT2TOP
    }

    interface GuideViewListener {
        fun onAffirm()
    }
}