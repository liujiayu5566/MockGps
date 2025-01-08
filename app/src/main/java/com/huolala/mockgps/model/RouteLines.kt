package com.huolala.mockgps.model

import com.baidu.mapapi.model.LatLng

/**
 * @author jiayu.liu
 */
data class RouteLines(
    /**
     * 点串经纬度集合
     */
    val route: ArrayList<LatLng>
)
