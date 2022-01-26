package com.huolala.mockgps.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.Toast
import com.huolala.mockgps.R
import com.huolala.mockgps.server.GpsAndFloatingService

import com.baidu.location.BDLocation

import com.baidu.location.BDAbstractLocationListener

import com.baidu.location.LocationClientOption

import com.baidu.location.LocationClient
import com.baidu.mapapi.map.*
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.model.LatLngBounds
import com.baidu.mapapi.search.route.*
import com.huolala.mockgps.model.MockMessageModel
import com.huolala.mockgps.utils.Utils
import java.util.*
import kotlinx.android.synthetic.main.activity_navi.*


/**
 * @author jiayu.liu
 */
class MockLocationActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var mLocationClient: LocationClient
    private lateinit var mBaiduMap: BaiduMap
    private var mSearch: RoutePlanSearch = RoutePlanSearch.newInstance()
    private var mPolyline: Overlay? = null
    private var fromTag: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navi)
        val model = intent.getParcelableExtra<MockMessageModel>("model")
        if (model == null) {
            pickPoiError()
            return
        }
        initView()
        initMap()

        model.run {
            this@MockLocationActivity.fromTag = fromTag
            when (fromTag) {
                0 -> {
                    locationModel?.run {
                        startMockServer(model)
                    } ?: {
                        pickPoiError()
                    }
                }
                1 -> {
                    if (startNavi == null || endNavi == null) {
                        pickPoiError()
                        return
                    }
                    mSearch.drivingSearch(
                        DrivingRoutePlanOption()
                            .policy(DrivingRoutePlanOption.DrivingPolicy.ECAR_DIS_FIRST)
                            .from(PlanNode.withLocation(startNavi?.latLng))
                            .to(PlanNode.withLocation(endNavi?.latLng))
                    )
                    startMockServer(model)
                }
                else -> {}
            }
        }
    }

    private fun pickPoiError() {
        Toast.makeText(this, "选址数据异常，请重新选择地址再重试", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun initView() {

    }

    private fun initMap() {
        mBaiduMap = mapview.map
        mBaiduMap.isMyLocationEnabled = true

        mBaiduMap.setMyLocationConfiguration(
            MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.NORMAL,
                true,
                BitmapDescriptorFactory.fromResource(R.drawable.ic_my_location),
            )
        )

        mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomBy(16f))

        mLocationClient = LocationClient(this)

        //通过LocationClientOption设置LocationClient相关参数
        val option = LocationClientOption()
        option.isOpenGps = true // 打开gps
        option.setCoorType("bd09ll") // 设置坐标类型
        option.setScanSpan(1000)

        //设置locationClientOption
        mLocationClient.locOption = option

        //注册LocationListener监听器
        val myLocationListener = object : BDAbstractLocationListener() {
            override fun onReceiveLocation(location: BDLocation?) {
                //mapView 销毁后不在处理新接收的位置
                if (location == null) {
                    return
                }
                mBaiduMap.locationData?.run {
                    //如果相等 不能更新
                    if (latitude == location.latitude && longitude == location.longitude) {
                        return
                    }
                }
                val locData = MyLocationData.Builder()
                    .accuracy(location.radius) // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(location.direction).latitude(location.latitude)
                    .longitude(location.longitude).build()
                mBaiduMap.setMyLocationData(locData)
                //更新中心点
                if (fromTag == 0) {
                    mBaiduMap.animateMapStatus(
                        MapStatusUpdateFactory.newLatLngZoom(
                            LatLng(
                                locData.latitude,
                                locData.longitude
                            ), 16f
                        )
                    )
                }

            }
        }
        mLocationClient.registerLocationListener(myLocationListener)
        //开启地图定位图层
        mLocationClient.start()

        mSearch.setOnGetRoutePlanResultListener(object : OnGetRoutePlanResultListener {
            override fun onGetWalkingRouteResult(p0: WalkingRouteResult?) {
                TODO("Not yet implemented")
            }

            override fun onGetTransitRouteResult(p0: TransitRouteResult?) {
                TODO("Not yet implemented")
            }

            override fun onGetMassTransitRouteResult(p0: MassTransitRouteResult?) {
                TODO("Not yet implemented")
            }

            override fun onGetDrivingRouteResult(drivingRouteResult: DrivingRouteResult?) {
                //创建DrivingRouteOverlay实例

                drivingRouteResult?.routeLines?.get(0)?.run {
                    val polylineList = arrayListOf<LatLng>()
                    for (step in allStep) {
                        val b = Bundle()
                        b.putInt("index", allStep.indexOf(step))
                        if (step.entrance != null) {
                            polylineList.add(step.entrance.location)
                        }
                        // 最后路段绘制出口点
                        if (allStep.indexOf(step) == allStep.size - 1 && step.exit != null
                        ) {
                            polylineList.add(step.exit.location)
                        }
                    }
                    mPolyline?.let {
                        null
                    }
                    val mOverlayOptions: OverlayOptions = PolylineOptions()
                        .width(10)
                        .color(0xAAFF0000.toInt())
                        .points(polylineList)
                    mPolyline = mBaiduMap.addOverlay(mOverlayOptions)
                    mBaiduMap.animateMapStatus(
                        MapStatusUpdateFactory.newLatLngBounds(
                            LatLngBounds.Builder().include(polylineList).build(), 50, 50, 50, 50
                        )
                    )
                }
            }

            override fun onGetIndoorRouteResult(p0: IndoorRouteResult?) {
                TODO("Not yet implemented")
            }

            override fun onGetBikingRouteResult(p0: BikingRouteResult?) {
                TODO("Not yet implemented")
            }
        })
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
        if (isFinishing) {
            destroy()
        }
        super.onPause()
        mapview.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun destroy() {
        mSearch.destroy()
        mLocationClient.stop()
        mBaiduMap.isMyLocationEnabled = false
        mapview.onDestroy()
    }

    override fun onClick(v: View?) {
//        v?.run {
//
//        }
    }

}