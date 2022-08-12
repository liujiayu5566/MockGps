package com.huolala.mockgps.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationProvider

import android.location.Criteria

import android.location.LocationManager

import android.os.Build
import android.provider.Settings
import com.baidu.mapapi.model.LatLng
import java.lang.IllegalArgumentException
import kotlin.collections.ArrayList


/**
 * @author jiayu.liu
 */
object Utils {

    @SuppressLint("WrongConstant")
    fun isAllowMockLocation(context: Context): Boolean {
        var canMockPosition: Boolean
        if (Build.VERSION.SDK_INT <= 22) { //6.0以下
            canMockPosition = Settings.Secure.getInt(
                context.applicationContext.getContentResolver(),
                Settings.Secure.ALLOW_MOCK_LOCATION,
                0
            ) !== 0
        } else {
            try {
                val locationManager =
                    context.applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager //获得LocationManager引用
                val providerStr = LocationManager.GPS_PROVIDER
                val provider = locationManager.getProvider(providerStr)
                if (provider != null) {
                    locationManager.addTestProvider(
                        provider.name,
                        provider.requiresNetwork(),
                        provider.requiresSatellite(),
                        provider.requiresCell(),
                        provider.hasMonetaryCost(),
                        provider.supportsAltitude(),
                        provider.supportsSpeed(),
                        provider.supportsBearing(),
                        provider.powerRequirement,
                        provider.accuracy
                    )
                } else {
                    locationManager.addTestProvider(
                        providerStr,
                        true,
                        true,
                        false,
                        false,
                        true,
                        true,
                        true,
                        Criteria.POWER_HIGH,
                        Criteria.ACCURACY_FINE
                    )
                }
                locationManager.setTestProviderEnabled(providerStr, true)
                locationManager.setTestProviderStatus(
                    providerStr,
                    LocationProvider.AVAILABLE,
                    null,
                    System.currentTimeMillis()
                )
                // 模拟位置可用
                canMockPosition = true
                locationManager.setTestProviderEnabled(providerStr, false)
                locationManager.removeTestProvider(providerStr)
            } catch (e: IllegalArgumentException) {
                canMockPosition = true
            } catch (e: Exception) {
                e.printStackTrace()
                canMockPosition = false
            }
        }
        return canMockPosition
    }

    /**
     * FIXME 弃用  待优化
     * @param array 原数据
     * @param speed 速度  单位：KM/H
     *
     */
    fun latLngToSpeedLatLng(array: ArrayList<LatLng>?, speed: Int): ArrayList<LatLng> {
        val latLngList = arrayListOf<LatLng>()
        if (array == null || array.isEmpty() || array.size <= 1) {
            return latLngList
        }
        //单位：M/S
        val currentSpeed: Double = speed / 3.6
        val size = array.size
        //当前经纬度
        var currentLatLng = array[0]
        latLngList.add(currentLatLng)
        //起点
        for (index in 1 until size) {
            //节点经纬度
            val latLng = array[index]
            var dis = CalculationLogLatDistance.getDistance(currentLatLng, latLng)
            var yaw = CalculationLogLatDistance.getYaw(currentLatLng, latLng)
            if (dis < currentSpeed) {
                currentLatLng = latLng
                latLngList.add(latLng)
                continue
            }
            while (dis > currentSpeed) {
                val nextLonLat =
                    CalculationLogLatDistance.getNextLonLat(currentLatLng, yaw, currentSpeed)

                dis = CalculationLogLatDistance.getDistance(currentLatLng, latLng)
                yaw = CalculationLogLatDistance.getYaw(currentLatLng, latLng)
                if (dis < currentSpeed) {
                    currentLatLng = latLng
                    latLngList.add(currentLatLng)
                } else {
                    currentLatLng = nextLonLat
                    latLngList.add(currentLatLng)
                }
            }
        }
        return latLngList
    }

}