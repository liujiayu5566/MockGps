package com.huolala.mockgps.manager.utils

import android.R
import android.text.TextUtils
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.MarkerOptions
import com.baidu.mapapi.map.Overlay
import com.baidu.mapapi.map.OverlayOptions
import com.baidu.mapapi.map.PolylineOptions
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.model.LatLngBounds


/**
 * @author jiayu.liu
 */
object MapDrawUtils {

    fun drawMarkerToMap(baiduMap: BaiduMap, point: LatLng, assetName: String) {
        if (TextUtils.isEmpty(assetName)) {
            return
        }
        val bitmap = BitmapDescriptorFactory
            .fromAsset(assetName)
        val option: OverlayOptions = MarkerOptions()
            .position(point)
            .icon(bitmap)
            .zIndex(2)
        baiduMap.addOverlay(option)

    }

    fun drawLineToMap(
        baiduMap: BaiduMap,
        polylineList: List<LatLng>,
        padding: Int,
        isMainLine: Boolean = true
    ): Overlay? {
        if (polylineList.isEmpty()) {
            return null
        }

        val mOverlayOptions: OverlayOptions = PolylineOptions()
            .width(30)
            .keepScale(true)
            .customTexture(
                BitmapDescriptorFactory.fromAsset(
                    if (isMainLine)
                        "navi_lbs_texture.png"
                    else
                        "navi_lbs_texture_unselected.png"
                )
            )
            .lineJoinType(PolylineOptions.LineJoinType.LineJoinRound)
            .lineCapType(PolylineOptions.LineCapType.LineCapRound)
            .points(polylineList)
            .zIndex(if (isMainLine) 1 else 0)
        val addOverlay = baiduMap.addOverlay(mOverlayOptions)

        baiduMap.animateMapStatus(
            MapStatusUpdateFactory.newLatLngBounds(
                LatLngBounds.Builder().include(polylineList).build(),
                padding,
                padding,
                padding,
                padding
            )
        )
        return addOverlay
    }
}