package com.huolala.mockgps.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.setPadding
import com.blankj.utilcode.util.ClickUtils
import com.blankj.utilcode.util.ColorUtils
import com.huolala.mockgps.R
import com.huolala.mockgps.utils.MMKVUtils

/**
 * @author jiayu.liu
 */
class GuideView : ConstraintLayout {
    private var ivGuideArrows: View

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

        ClickUtils.applySingleDebouncing(
            findViewById<View>(R.id.tv_affirm)
        ) {
            (parent as ViewGroup).removeView(this@GuideView)
            MMKVUtils.setGuideVisible(true)
        }

        setBackgroundColor(ColorUtils.string2Int("#80000000"))
        isClickable = true
    }

    fun setGuideView(view: View?) {
        view?.let {
            addView(
                AppCompatImageView(context).apply {
                    it.isDrawingCacheEnabled = true
                    setImageBitmap(it.drawingCache)
                },
                LayoutParams(
                    it.width,
                    it.height,
                ).apply {
                    leftToLeft = LayoutParams.PARENT_ID
                    topToTop = LayoutParams.PARENT_ID
                    marginStart = it.x.toInt()
                    topMargin = it.y.toInt()
                })

            (ivGuideArrows.layoutParams as MarginLayoutParams).let { layoutParams ->
                layoutParams.rightMargin = it.width / 2
                layoutParams.topMargin = it.height
                ivGuideArrows.layoutParams = layoutParams
            }
        }
    }
}