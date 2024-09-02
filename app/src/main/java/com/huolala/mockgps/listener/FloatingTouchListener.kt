package com.huolala.mockgps.listener

import android.animation.ValueAnimator
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import com.blankj.utilcode.util.ScreenUtils
import kotlin.math.min


/**
 * 悬浮窗touch监听处理
 * @author jiayu.liu
 */
class FloatingTouchListener(
    private var windowManager: WindowManager,
    private var layoutParams: WindowManager.LayoutParams,
    private var isAutoMove: Boolean = false,
    private var touchView: View? = null //悬浮窗根布局  解决touch的View与悬浮窗根布局不一致的情况
) : View.OnTouchListener {
    private val mScreenWidth = ScreenUtils.getScreenWidth()
    private val mScreenHeight = ScreenUtils.getScreenHeight()
    var callBack: FloatingTouchCallBack? = null

    private var initialX = 0f
    private var initialY = 0f
    private var dX = 0f
    private var dY = 0f


    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        if (touchView == null) {
            return false
        }
        return when (event!!.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = layoutParams.x.toFloat()
                initialY = layoutParams.y.toFloat()
                dX = event.rawX - initialX
                dY = event.rawY - initialY
                callBack?.onActionDown()
                true
            }

            MotionEvent.ACTION_MOVE -> {
                val x: Int = (event.rawX - dX).toInt()
                val y: Int = (event.rawY - dY).toInt()
                layoutParams.x = if (x <= 0) 0 else min(
                    x.toDouble(),
                    (mScreenWidth - (touchView?.width ?: 0)).toDouble()
                ).toInt()
                layoutParams.y = if (y <= 0) 0 else min(
                    y.toDouble(),
                    (mScreenHeight - (touchView?.height ?: 0)).toDouble()
                ).toInt()

                windowManager.updateViewLayout(touchView, layoutParams)
                true
            }

            MotionEvent.ACTION_UP -> {
                if (!isAutoMove) {
                    return false
                }
                // 在手指抬起时，将View移动到屏幕边缘
                val isLeft = layoutParams.x + touchView!!.width / 2 <= mScreenWidth / 2
                layoutParams.x = if (isLeft) 0 else mScreenWidth - touchView!!.width
                windowManager.updateViewLayout(touchView, layoutParams)
                callBack?.onActionUp(isLeft)
                true
            }

            else -> false
        }
    }

    interface FloatingTouchCallBack {
        /**
         * 抬起
         */
        fun onActionUp(isLeft: Boolean)

        /**
         * 按下
         */
        fun onActionDown()
    }
}