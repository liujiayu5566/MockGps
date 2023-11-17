package com.huolala.mockgps.manager.utils

import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.route.DrivingRouteLine

/**
 * @author jiayu.liu
 */
object MapConvertUtils {
    /**
     * 导航数据转换点串
     */
    fun convertLatLngList(routeLine: DrivingRouteLine?): List<LatLng> {
        val polylineList = arrayListOf<LatLng>()
        routeLine?.let {
            for (step in it.allStep) {
                if (step.wayPoints != null && step.wayPoints.isNotEmpty()) {
                    polylineList.addAll(step.wayPoints)
                }
            }
        }
        return polylineList
    }
}