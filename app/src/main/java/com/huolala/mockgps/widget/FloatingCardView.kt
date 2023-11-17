package com.huolala.mockgps.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout


/**
 * @author jiayu.liu
 */
class FloatingCardView : FrameLayout {
    constructor(context: Context) : super(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        outlineProvider = FloatingViewOutlineProvider()
        clipToOutline = true
    }


    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return true
    }

    inner class FloatingViewOutlineProvider : ViewOutlineProvider() {
        override fun getOutline(view: View?, outline: Outline?) {
            outline?.setOval(0, 0, view?.width ?: 0, view?.height ?: 0)
        }
    }
}