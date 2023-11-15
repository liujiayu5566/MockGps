package com.huolala.mockgps.manager

import android.app.Service
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.Utils
import com.huolala.mockgps.R
import com.huolala.mockgps.listener.FloatingTouchListener
import kotlinx.android.synthetic.main.layout_floating.view.*

/**
 * @author jiayu.liu
 */
class FloatingViewManger private constructor() {
    private lateinit var view: View
    private var windowManager: WindowManager =
        Utils.getApp().getSystemService(Service.WINDOW_SERVICE) as WindowManager

    private val mScreenWidth = ScreenUtils.getScreenWidth()
    private val mScreenHeight = ScreenUtils.getScreenHeight()
    private var isAddToWindow = false
    var listener: FloatingViewListener? = null

    companion object {
        val INSTANCE: FloatingViewManger by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            FloatingViewManger()
        }
    }

    fun addFloatViewToWindow() {
        if (isAddToWindow) {
            return
        }
        view = LayoutInflater.from(Utils.getApp()).inflate(R.layout.layout_floating, null)

        //播放
        view.startAndPause.setOnClickListener {
//            view.isSelected = isStart
//            if (!isStart) {
//                removeGps()
//                handle.removeCallbacksAndMessages(null)
//            } else {
//                mockLocation()
//            }
            listener?.let {
                if (!view.isSelected) {
                    it.reStart()
                } else {
                    it.pause()
                }
            }
        }


//        view.setOnClickListener(object : ClickUtils.OnMultiClickListener(2, 300) {
//            override fun onTriggerClick(v: View?) {
//                AppUtils.launchApp(packageName)
//            }
//
//            override fun onBeforeTriggerClick(v: View?, count: Int) {
//            }
//        })

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
            || Settings.canDrawOverlays(Utils.getApp())
        ) {
            isAddToWindow = true
            val layoutParams = getLayoutParams()
            windowManager.addView(view, layoutParams)
            view.setOnTouchListener(FloatingTouchListener(windowManager, layoutParams))
        }
    }

    private fun getLayoutParams(): WindowManager.LayoutParams {
        val params = WindowManager.LayoutParams()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            params.type = WindowManager.LayoutParams.TYPE_PHONE
        }
        params.packageName = AppUtils.getAppPackageName()
        params.width = WindowManager.LayoutParams.WRAP_CONTENT
        params.height = WindowManager.LayoutParams.WRAP_CONTENT
        params.gravity = Gravity.LEFT or Gravity.TOP
        params.x = mScreenWidth - view.width
        params.y = mScreenHeight / 2
        //焦点问题  透明度
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        params.format = PixelFormat.TRANSPARENT
        return params
    }

    interface FloatingViewListener {
        fun pause()

        fun reStart()
    }
}