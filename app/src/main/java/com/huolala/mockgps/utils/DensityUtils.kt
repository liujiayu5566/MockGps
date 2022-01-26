package com.huolala.mockgps.utils

import android.content.Context
import android.util.TypedValue
import java.lang.UnsupportedOperationException
import android.graphics.Bitmap

import android.app.Activity
import android.graphics.Rect

import android.util.DisplayMetrics
import android.view.View

import android.view.WindowManager
import java.lang.Exception


/**
 * @author jiayu.liu
 */
class DensityUtils private constructor() {

    companion object {
        /**
         * dp转px
         *
         * @param context
         * @param val
         * @return
         */
        fun dp2px(context: Context, dpVal: Float): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.applicationContext.resources.displayMetrics
            ).toInt()
        }

        /**
         * sp转px
         *
         * @param context
         * @param val
         * @return
         */
        fun sp2px(context: Context, spVal: Float): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                spVal, context.applicationContext.resources.displayMetrics
            ).toInt()
        }

        /**
         * px转dp
         *
         * @param context
         * @param pxVal
         * @return
         */
        fun px2dp(context: Context, pxVal: Float): Float {
            val scale: Float = context.applicationContext.resources.displayMetrics.density
            return pxVal / scale
        }

        /**
         * px转sp
         *
         * @param fontScale
         * @param pxVal
         * @return
         */
        fun px2sp(context: Context, pxVal: Float): Float {
            return pxVal / context.applicationContext.resources.displayMetrics.scaledDensity
        }

        /**
         * 获得屏幕高度
         *
         * @param context
         * @return
         */
        fun getScreenWidth(context: Context): Int {
            val wm = context.applicationContext
                .getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val outMetrics = DisplayMetrics()
            wm.defaultDisplay.getMetrics(outMetrics)
            return outMetrics.widthPixels
        }

        /**
         * 获得屏幕宽度
         *
         * @param context
         * @return
         */
        fun getScreenHeight(context: Context): Int {
            val wm = context.applicationContext
                .getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val outMetrics = DisplayMetrics()
            wm.defaultDisplay.getMetrics(outMetrics)
            return outMetrics.heightPixels
        }

        /**
         * 获得状态栏的高度
         *
         * @param context
         * @return
         */
        fun getStatusHeight(context: Context): Int {
            var statusHeight = -1
            try {
                val clazz = Class.forName("com.android.internal.R\$dimen")
                val `object` = clazz.newInstance()
                val height = clazz.getField("status_bar_height")[`object`].toString().toInt()
                statusHeight = context.applicationContext.resources.getDimensionPixelSize(height)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return statusHeight
        }

        /**
         * 获取当前屏幕截图，包含状态栏
         *
         * @param activity
         * @return
         */
        fun snapShotWithStatusBar(activity: Activity): Bitmap? {
            val view: View = activity.window.decorView
            view.setDrawingCacheEnabled(true)
            view.buildDrawingCache()
            val bmp: Bitmap = view.getDrawingCache()
            val width = getScreenWidth(activity)
            val height = getScreenHeight(activity)
            var bp: Bitmap? = null
            bp = Bitmap.createBitmap(bmp, 0, 0, width, height)
            view.destroyDrawingCache()
            return bp
        }

        /**
         * 获取当前屏幕截图，不包含状态栏
         *
         * @param activity
         * @return
         */
        fun snapShotWithoutStatusBar(activity: Activity): Bitmap? {
            val view: View = activity.window.decorView
            view.setDrawingCacheEnabled(true)
            view.buildDrawingCache()
            val bmp: Bitmap = view.getDrawingCache()
            val frame = Rect()
            activity.window.decorView.getWindowVisibleDisplayFrame(frame)
            val statusBarHeight: Int = frame.top
            val width = getScreenWidth(activity)
            val height = getScreenHeight(activity)
            var bp: Bitmap? = null
            bp = Bitmap.createBitmap(
                bmp, 0, statusBarHeight, width, height
                        - statusBarHeight
            )
            view.destroyDrawingCache()
            return bp
        }
    }

    init {
        /* cannot be instantiated */
        throw UnsupportedOperationException("cannot be instantiated")
    }
}