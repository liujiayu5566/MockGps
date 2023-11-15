package com.huolala.mockgps.listener

import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.blankj.utilcode.util.ScreenUtils

/**
 * @author jiayu.liu
 */
class FloatingTouchListener(
    private var windowManager: WindowManager,
    private var layoutParams: WindowManager.LayoutParams
) : View.OnTouchListener {
    private var x: Int = 0
    private var y: Int = 0
    private val mScreenWidth = ScreenUtils.getScreenWidth()
    private val mScreenHeight = ScreenUtils.getScreenHeight()

    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                x = event.rawX.toInt()
                y = event.rawY.toInt()
            }

            MotionEvent.ACTION_MOVE -> {
                val nowX = event.rawX.toInt()
                val nowY = event.rawY.toInt()
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
        }
        return false
    }
}