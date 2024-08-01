package com.huolala.mockgps.manager

import android.content.Context
import android.graphics.Color
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.MapStatus
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.MyLocationConfiguration
import com.baidu.mapapi.map.MyLocationData
import com.baidu.mapapi.model.LatLng


/**
 * 定位小蓝点显示控制类
 * @author jiayu.liu
 */

enum class FollowMode {
    MODE_SINGLE,//单次调整中心点
    MODE_PERSISTENT,//跟随调整中心点
    MODE_NONE//不调整
}

/**
 * 来源
 */
enum class Source {
    FLOATING,//悬浮窗
    OTHER,//其他
}

class MapLocationManager(
    context: Context,
    private var baiduMap: BaiduMap,
    follow: FollowMode,
    private val source: Source = Source.OTHER
) {
    private var mLocationClient: LocationClient
    private var isZoom = false
    private val myLocationListener = object : BDAbstractLocationListener() {
        override fun onReceiveLocation(location: BDLocation?) {
            //mapView 销毁后不在处理新接收的位置
            if (location == null) {
                return
            }
            baiduMap.locationData?.run {
                //如果相等 不能更新
                if (latitude == location.latitude && longitude == location.longitude) {
                    return
                }
            }
            val locData = MyLocationData.Builder()
                .accuracy(location.radius)
                .speed(location.speed)
                .direction(location.direction)
                .latitude(location.latitude)
                .longitude(location.longitude)
                .build()
            baiduMap.setMyLocationData(locData)
            //更新中心点
            when (follow) {
                FollowMode.MODE_SINGLE -> {
                    if (!isZoom) {
                        zoom(locData)
                        isZoom = true
                    }
                }

                FollowMode.MODE_PERSISTENT ->
                    zoom(locData)

                else -> {

                }
            }
        }
    }

    fun zoom(locData: MyLocationData) {
        baiduMap.animateMapStatus(
            MapStatusUpdateFactory.newLatLngZoom(
                LatLng(
                    locData.latitude,
                    locData.longitude
                ), if (source == Source.FLOATING) 18f else 16f
            )
        )
    }

    fun setLocationMode(mode: MyLocationConfiguration.LocationMode = MyLocationConfiguration.LocationMode.NORMAL) {
        MyLocationConfiguration(
            mode,
            true,
            null,
            Color.TRANSPARENT,
            Color.TRANSPARENT
        ).apply {
            if (mode == MyLocationConfiguration.LocationMode.NORMAL) {
                baiduMap.setMapStatus(
                    MapStatusUpdateFactory.newMapStatus(
                        MapStatus.Builder().overlook(0f).rotate(0f).build()
                    )
                )
            }
            baiduMap.setMyLocationConfiguration(this)
        }
    }

    init {
        setLocationMode(MyLocationConfiguration.LocationMode.NORMAL)

        mLocationClient = LocationClient(context)
        baiduMap.isMyLocationEnabled = true

        //通过LocationClientOption设置LocationClient相关参数
        val option = LocationClientOption()
        option.setScanSpan(1000)
        option.setNeedDeviceDirect(true)
        option.isOpenGnss = true
        option.setIsNeedAltitude(true)
        option.setOpenAutoNotifyMode(1000, 0, LocationClientOption.LOC_SENSITIVITY_MIDDLE)
        //设置locationClientOption
        mLocationClient.locOption = option

        mLocationClient.registerLocationListener(myLocationListener)
        //开启地图定位图层
        mLocationClient.start()
    }

    fun onDestroy() {
        mLocationClient.unRegisterLocationListener(myLocationListener)
        mLocationClient.stop()
        baiduMap.isMyLocationEnabled = false
    }

}