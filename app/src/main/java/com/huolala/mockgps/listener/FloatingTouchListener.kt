package com.huolala.mockgps.listener

import android.animation.ValueAnimator
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import com.blankj.utilcode.util.ScreenUtils


/**
 * @author jiayu.liu
 */
class FloatingTouchListener(
    private var windowManager: WindowManager,
    private var layoutParams: WindowManager.LayoutParams,
    private var isAutoMove: Boolean = false
) : View.OnTouchListener {
    private var x: Int = 0
    private var y: Int = 0
    private val mScreenWidth = ScreenUtils.getScreenWidth()
    private val mScreenHeight = ScreenUtils.getScreenHeight()
    private var animator: ValueAnimator? = null
    var callBack: FloatingTouchCallBack? = null


    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
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
                val viewWidth = view?.width ?: 0
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
                windowManager.updateViewLayout(view, layoutParams)
            }

            MotionEvent.ACTION_UP -> {
                if (!isAutoMove) {
                    return true
                }
                val viewWidth = view?.width ?: 0
                var nowX = event.rawX.toInt() - viewWidth / 2
                println("nowX:$nowX")
                val movedToX: Int
                if (nowX + viewWidth / 2 <= mScreenWidth / 2) {
                    nowX -= viewWidth / 2
                    movedToX = 0
                } else {
                    nowX += viewWidth / 2
                    movedToX = mScreenWidth - viewWidth
                }
                animMoveView(
                    nowX,
                    movedToX,
                    view
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
            println("animatedValue: $animatedValue")
            layoutParams.x = animatedValue as Int
            // 更新悬浮窗控件布局
            windowManager.updateViewLayout(view, layoutParams)
            if (animatedValue == x) {
                callBack?.onActionUp(x == 0)
            }
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