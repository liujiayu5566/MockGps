package com.huolala.mockgps.listener

import android.animation.ValueAnimator
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import com.blankj.utilcode.util.ScreenUtils


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
    private var x: Int = 0
    private var y: Int = 0
    private val mScreenWidth = ScreenUtils.getScreenWidth()
    private val mScreenHeight = ScreenUtils.getScreenHeight()
    private var animator: ValueAnimator? = null
    var callBack: FloatingTouchCallBack? = null


    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        var curTouchView = view
        if (touchView != null) {
            curTouchView = touchView
        }
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                animator?.cancel()
                x = event.rawX.toInt()
                y = event.rawY.toInt()
                callBack?.onActionDown()
            }

            MotionEvent.ACTION_MOVE -> {
                val nowX = event.rawX.toInt()
                val nowY = event.rawY.toInt()
                if (nowX == 0 && nowY == 0) {
                    return true
                }
                val movedX = nowX - x
                val movedY = nowY - y
                x = nowX
                y = nowY
                val viewWidth = curTouchView?.width ?: 0
                layoutParams.run {
                    x += movedX
                    y += movedY
                    x = if (x <= 0) 0 else x
                    x = if (x >= mScreenWidth - viewWidth)
                        mScreenWidth - viewWidth else x
                    y = if (y <= 0) 0 else y
                    y = if (y >= mScreenHeight) mScreenHeight else y
                }
                // 更新悬浮窗控件布局
                windowManager.updateViewLayout(curTouchView, layoutParams)
            }

            MotionEvent.ACTION_UP -> {
                if (!isAutoMove) {
                    return true
                }
                val viewWidth = curTouchView?.width ?: 0
                var nowX = event.rawX.toInt()
                val movedToX: Int
                if (nowX <= mScreenWidth / 2) {
                    nowX -= viewWidth
                    movedToX = 0
                } else {
                    movedToX = mScreenWidth - viewWidth
                }
                callBack?.onActionUp(movedToX == 0)
                animMoveView(
                    nowX,
                    movedToX,
                    curTouchView
                )

            }
        }
        return true
    }

    private fun animMoveView(nowX: Int, x: Int, view: View?) {
        animator = ValueAnimator.ofInt(nowX, x)
        animator?.duration = 200
        animator?.interpolator = LinearInterpolator()
        animator?.addUpdateListener {
            val animatedValue = it.animatedValue
            layoutParams.x = animatedValue as Int
            // 更新悬浮窗控件布局
            windowManager.updateViewLayout(view, layoutParams)
        }
        animator?.start()
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