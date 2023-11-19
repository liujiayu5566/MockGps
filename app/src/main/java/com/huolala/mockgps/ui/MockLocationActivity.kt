package com.huolala.mockgps.ui

import android.content.Intent
import android.graphics.Rect
import android.os.*
import android.view.View
import android.widget.Toast
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.map.*
import com.baidu.mapapi.model.LatLng
import com.blankj.utilcode.util.ClickUtils
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.FileIOUtils
import com.castiel.common.base.BaseActivity
import com.castiel.common.base.BaseViewModel
import com.huolala.mockgps.R
import com.huolala.mockgps.databinding.ActivityNaviBinding
import com.huolala.mockgps.manager.FollowMode
import com.huolala.mockgps.manager.MapLocationManager
import com.huolala.mockgps.manager.utils.MapDrawUtils
import com.huolala.mockgps.manager.SearchManager
import com.huolala.mockgps.model.MockMessageModel
import com.huolala.mockgps.model.NaviType
import com.huolala.mockgps.server.GpsAndFloatingService
import com.huolala.mockgps.utils.Utils
import kotlinx.android.synthetic.main.activity_navi.*
import kotlinx.android.synthetic.main.dialog_select_navi_map.texture_mapview
import java.lang.ref.WeakReference


/**
 * @author jiayu.liu
 */
class MockLocationActivity : BaseActivity<ActivityNaviBinding, BaseViewModel>(),
    View.OnClickListener {
    private var DRAW_MAP = 0;
    private lateinit var mBaiduMap: BaiduMap
    private var mNaviType: Int = NaviType.LOCATION
    private val mPadding: Int = ConvertUtils.dp2px(50f)
    private var mapLocationManager: MapLocationManager? = null

    override fun initViewModel(): Class<BaseViewModel> {
        return BaseViewModel::class.java
    }

    override fun getLayout(): Int {
        return R.layout.activity_navi
    }

    override fun initView() {
        ClickUtils.applySingleDebouncing(iv_back, this)

        mBaiduMap = mapview.map
        mapview.showScaleControl(false)
        mapview.showZoomControls(false)
        mapview.getChildAt(1).visibility = View.GONE
        mBaiduMap.uiSettings?.isCompassEnabled = false

        mBaiduMap.setMyLocationConfiguration(
            MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.NORMAL,
                true, null
            )
        )
        mBaiduMap.setOnMapLoadedCallback {
            startMock()
        }
    }

    override fun initData() {}

    private fun startMock() {
        val model = intent.getParcelableExtra<MockMessageModel>("model")
        if (model == null) {
            pickPoiError()
            return
        }
        with(model) {
            this@MockLocationActivity.mNaviType = naviType
            //开启定位小蓝点展示
            mapLocationManager = MapLocationManager(
                this@MockLocationActivity,
                mBaiduMap,
                if (mNaviType == NaviType.LOCATION) FollowMode.MODE_PERSISTENT else FollowMode.MODE_NONE
            )
            when (naviType) {
                NaviType.LOCATION -> {
                    locationModel?.run {
                        startMockServer(model)
                    } ?: {
                        pickPoiError()
                    }
                }

                NaviType.NAVI, NaviType.NAVI_FILE -> {
                    SearchManager.INSTANCE.polylineList.let {
                        if (it.isEmpty()) {
                            pickPoiError()
                            return
                        }
                        mBaiduMap.clear()
                        startNavi?.latLng?.let { start ->
                            MapDrawUtils.drawMarkerToMap(mBaiduMap, start, "marker_start.png")
                        }
                        endNavi?.latLng?.let { end ->
                            MapDrawUtils.drawMarkerToMap(mBaiduMap, end, "marker_end.png")
                        }
                        MapDrawUtils.drawLineToMap(
                            mBaiduMap,
                            it,
                            Rect(mPadding, mPadding, mPadding, mPadding)
                        )
                        startMockServer(model)
                    }
                }

                else -> {
                }
            }
        }
    }

    override fun initObserver() {

    }

    private fun pickPoiError() {
        Toast.makeText(this, "选址数据异常，请重新选择地址再重试", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun startMockServer(parcelable: Parcelable?) {
        //判断  为null先启动服务  悬浮窗需要
        parcelable?.run {
            if (!Utils.isAllowMockLocation(this@MockLocationActivity)) {
                Toast.makeText(
                    this@MockLocationActivity,
                    "将本应用设置为\"模拟位置信息应用\"，否则无法正常使用",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
        }
        //启动服务  定位以及悬浮窗
        startService(Intent(this, GpsAndFloatingService::class.java).apply {
            parcelable?.let {
                putExtras(
                    Bundle().apply {
                        putParcelable("info", it)
                    })
            }
        })
    }

    override fun onResume() {
        super.onResume()
        mapview.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapview.onPause()
        if (isFinishing) {
            destroy()
        }
    }

    private fun destroy() {
        mapLocationManager?.onDestroy()
        mapview.onDestroy()
    }

    override fun onClick(v: View?) {
        when (v) {
            iv_back -> {
                finish()
            }

            else -> {
            }
        }
    }

    class MockLocationHandler(activity: MockLocationActivity) :
        Handler(Looper.getMainLooper()) {
        private var weakReference: WeakReference<MockLocationActivity>? = null

        init {
            weakReference = WeakReference(activity)
        }

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            weakReference?.get()?.run {
                when (msg.what) {
                    DRAW_MAP -> {
                        (msg.obj as ArrayList<LatLng>?)?.let {
                            mBaiduMap.clear()
                            MapDrawUtils.drawLineToMap(
                                mBaiduMap,
                                it,
                                Rect(mPadding, mPadding, mPadding, mPadding)
                            )
                        }

                    }

                    else -> {
                    }
                }

            }
        }

    }

}