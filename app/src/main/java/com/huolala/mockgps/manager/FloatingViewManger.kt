package com.huolala.mockgps.manager

import android.annotation.SuppressLint
import android.app.Service
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import com.baidu.mapapi.model.LatLng
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.ClickUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.Utils
import com.huolala.mockgps.R
import com.huolala.mockgps.listener.FloatingTouchListener
import com.huolala.mockgps.model.NaviType
import com.huolala.mockgps.utils.CalculationLogLatDistance
import com.huolala.mockgps.utils.HandlerUtils
import com.huolala.mockgps.widget.RockerView
import kotlinx.android.synthetic.main.layout_floating.view.*
import kotlinx.android.synthetic.main.layout_floating_location_adjust.view.*
import okhttp3.internal.format
import java.text.Format

/**
 * @author jiayu.liu
 */
class FloatingViewManger private constructor() {
    private var view: View? = null
    private var locationAdjust: View? = null
    private var windowManager: WindowManager =
        Utils.getApp().getSystemService(Service.WINDOW_SERVICE) as WindowManager

    private val mScreenWidth = ScreenUtils.getScreenWidth()
    private val mScreenHeight = ScreenUtils.getScreenHeight()
    private var curLocation: LatLng? = null
    private var isAddFloatingView = false
    private var infoViewCurVisibility: Int = View.VISIBLE
    var listener: FloatingViewListener? = null

    private val runnable: Runnable = Runnable { view?.iv_setting?.visibility = View.INVISIBLE }
    private val rockerRunnable: Runnable = Runnable {

    }

    companion object {
        val INSTANCE: FloatingViewManger by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            FloatingViewManger()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun addFloatViewToWindow() {
        if (isAddFloatingView) {
            return
        }
        view = LayoutInflater.from(Utils.getApp()).inflate(R.layout.layout_floating, null)

        view?.mapview?.onCreate(Utils.getApp(), null)
        view?.mapview?.showScaleControl(false)
        view?.mapview?.showZoomControls(false)
        view?.mapview?.getChildAt(1)?.visibility = View.GONE
        view?.mapview?.map?.let {
            it.uiSettings?.isCompassEnabled = false
            it.uiSettings?.setAllGesturesEnabled(false)
            it.setOnMapLoadedCallback {
                MapLocationManager(
                    Utils.getApp(),
                    it,
                    FollowMode.MODE_PERSISTENT
                )
            }
        }

        //暂停播放
        ClickUtils.applySingleDebouncing(view?.startAndPause) {
            listener?.let { listener ->
                if (!it.isSelected) {
                    listener.reStart()
                } else {
                    listener.pause()
                }
            }
        }

        //控制展示暂停面板
        ClickUtils.applySingleDebouncing(view?.iv_setting) {
            infoViewCurVisibility =
                if (view?.info?.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            view?.info?.visibility = infoViewCurVisibility
        }

        ClickUtils.applySingleDebouncing(view?.iv_adjust) {
            addAdjustLocationToWindow()
        }

        HandlerUtils.INSTANCE.postDelayed(runnable, 5000)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
            || Settings.canDrawOverlays(Utils.getApp())
        ) {
            isAddFloatingView = true
            val layoutParams = getLayoutParams()
            addToWindow(view, layoutParams)
            layoutParams.let { params ->
                view?.setOnTouchListener(FloatingTouchListener(windowManager, params, true).apply {
                    callBack = object : FloatingTouchListener.FloatingTouchCallBack {
                        override fun onActionUp(isLeft: Boolean) {
                            HandlerUtils.INSTANCE.postDelayed(runnable, 5000)

                            (view?.info?.layoutParams as ConstraintLayout.LayoutParams).let {
                                if (isLeft) {
                                    view?.info?.setBackgroundResource(R.drawable.shape_round_10_color_black_30_right)
                                    view?.info_guideline?.setGuidelinePercent(0.75f)
                                    it.leftToRight = R.id.spacer
                                    it.rightToLeft = ConstraintLayout.LayoutParams.UNSET
                                } else {
                                    view?.info?.setBackgroundResource(R.drawable.shape_round_10_color_black_30_left)
                                    view?.info_guideline?.setGuidelinePercent(0.25f)
                                    it.leftToRight = ConstraintLayout.LayoutParams.UNSET
                                    it.rightToLeft = R.id.spacer

                                }
                                view?.info?.layoutParams = it
                            }

                            (view?.card?.layoutParams as ConstraintLayout.LayoutParams).let {
                                if (isLeft) {
                                    it.rightToRight = ConstraintLayout.LayoutParams.UNSET
                                    it.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID
                                } else {
                                    it.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID
                                    it.leftToLeft = ConstraintLayout.LayoutParams.UNSET

                                }
                                view?.card?.layoutParams = it
                            }

                            view?.info?.visibility = infoViewCurVisibility
                        }

                        override fun onActionDown() {
                            HandlerUtils.INSTANCE.removeCallbacks(runnable)
                            view?.iv_setting?.visibility = View.VISIBLE
                            view?.info?.visibility = View.GONE
                        }
                    }
                })
            }
        }
    }

    /**
     * 在window中增加布局
     */
    private fun addToWindow(
        view: View?,
        layoutParams: WindowManager.LayoutParams
    ) {
        return try {
            windowManager.addView(view, layoutParams)
        } catch (e: Exception) {
            e.printStackTrace()

        }
    }

    private fun removeFromWindow(view: View) {
        try {
            windowManager.removeView(view)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 定位调整view
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun addAdjustLocationToWindow() {
        if (locationAdjust == null) {
            locationAdjust = LayoutInflater.from(Utils.getApp())
                .inflate(R.layout.layout_floating_location_adjust, null)
            locationAdjust?.rocker_view!!.setOnAngleChangeListener(object :
                RockerView.OnAngleChangeListener {
                private var currentTimeMillis: Long = 0L
                private val interval: Long = 1000L

                override fun onStart() {}

                override fun angle(angle: Double) {
                    System.currentTimeMillis().let {
                        //2s内触发一次
                        if (it - currentTimeMillis <= interval) {
                            return
                        }
                        currentTimeMillis = it
                        curLocation?.let {
                            val location =
                                CalculationLogLatDistance.getNextLonLat(
                                    curLocation,
                                    angle,
                                    locationAdjust?.speed_view!!.getCurSpeed().toDouble()
                                )
                            if (CalculationLogLatDistance.isCheckNaN(location)) {
                                ToastUtils.showShort("计算经纬度失败,请重试！")
                                return
                            }
                            listener?.changeLocation(location)
                        }
                        //2s后如果没有角度变化 则回调上次的数据
                        HandlerUtils.INSTANCE.removeCallbacks(rockerRunnable)
                        HandlerUtils.INSTANCE.postDelayed(rockerRunnable, interval)
                    }
                }

                override fun onFinish() {
                    HandlerUtils.INSTANCE.removeCallbacks(rockerRunnable)
                }

            })
            ClickUtils.applySingleDebouncing(locationAdjust?.iv_close!!) {
                removeFromWindow(locationAdjust!!)
            }
        }

        val layoutParams = getLayoutParams()
        layoutParams.x = 0
        addToWindow(locationAdjust, layoutParams)
        layoutParams.let {
            locationAdjust?.setOnTouchListener(FloatingTouchListener(windowManager, it))
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
        params.x = mScreenWidth - (view?.width ?: 0)
        params.y = mScreenHeight / 2
        //焦点问题  透明度
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        params.format = PixelFormat.TRANSPARENT
        return params
    }

    fun startMock() {
        view?.startAndPause?.isSelected = true
        when (listener?.getNaviType()) {
            NaviType.LOCATION -> {

            }

            NaviType.NAVI, NaviType.NAVI_FILE -> {

            }

            else -> {}
        }
        view?.iv_adjust?.visibility = View.VISIBLE
    }

    fun stopMock() {
        view?.startAndPause?.isSelected = false
        if (listener?.getNaviType() == NaviType.LOCATION) {
            view?.iv_adjust?.visibility = View.GONE
        }
    }

    fun setCurLocation(curLocation: LatLng?) {
        this.curLocation = curLocation
        if (curLocation == null) {
            return
        }
        //已加载到window中
        locationAdjust?.parent?.run {
            locationAdjust?.tv_info!!.text =
                format(
                    "当前位置：\nlongitude:%.6f\nlatitude:%.6f",
                    curLocation.longitude,
                    curLocation.latitude
                )
        }
    }

    interface FloatingViewListener {
        fun pause()

        fun reStart()

        fun getNaviType(): Int

        fun changeLocation(latLng: LatLng)
    }
}