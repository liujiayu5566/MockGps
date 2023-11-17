package com.huolala.mockgps.manager

import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.route.BikingRouteResult
import com.baidu.mapapi.search.route.DrivingRouteLine
import com.baidu.mapapi.search.route.DrivingRoutePlanOption
import com.baidu.mapapi.search.route.DrivingRouteResult
import com.baidu.mapapi.search.route.IndoorRouteResult
import com.baidu.mapapi.search.route.MassTransitRouteResult
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener
import com.baidu.mapapi.search.route.PlanNode
import com.baidu.mapapi.search.route.RoutePlanSearch
import com.baidu.mapapi.search.route.TransitRouteResult
import com.baidu.mapapi.search.route.WalkingRouteResult
import com.blankj.utilcode.util.ToastUtils
import com.huolala.mockgps.utils.Utils

/**
 * @author jiayu.liu
 */
class SearchManager private constructor() {
    private var mSearch: RoutePlanSearch = RoutePlanSearch.newInstance()
    private var isSearchIng = false
    var listener: SearchManagerListener? = null
    var polylineList: ArrayList<LatLng> = arrayListOf()


    companion object {
        val INSTANCE: SearchManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            SearchManager()
        }
    }

    init {
        mSearch.setOnGetRoutePlanResultListener(object : OnGetRoutePlanResultListener {
            override fun onGetWalkingRouteResult(walkingRouteResult: WalkingRouteResult?) {
            }

            override fun onGetTransitRouteResult(transitRouteResult: TransitRouteResult?) {
            }

            override fun onGetMassTransitRouteResult(massTransitRouteResult: MassTransitRouteResult?) {
            }

            override fun onGetDrivingRouteResult(drivingRouteResult: DrivingRouteResult?) {
                isSearchIng = false
                listener?.onDrivingRouteResultLines(drivingRouteResult?.routeLines)

            }

            override fun onGetIndoorRouteResult(indoorRouteResult: IndoorRouteResult?) {
            }

            override fun onGetBikingRouteResult(bikingRouteResult: BikingRouteResult?) {
            }
        })
    }

    fun driverSearch(startLatLng: LatLng?, endLatLng: LatLng?, multiroute: Boolean) {
        if (startLatLng == null || endLatLng == null) {
            return
        }
        if (isSearchIng) {
            ToastUtils.showShort("路线规划中,请稍后.")
            return
        }
        isSearchIng = true
        mSearch.drivingSearch(
            DrivingRoutePlanOption()
                .from(PlanNode.withLocation(startLatLng))
                .to(PlanNode.withLocation(endLatLng)).apply {
                    if (!multiroute) {
                        this.policy(DrivingRoutePlanOption.DrivingPolicy.ECAR_DIS_FIRST)
                    }
                }
        )
    }

    fun selectDriverLine(routeLine: DrivingRouteLine?) {
        routeLine?.let {
            val polylineList = arrayListOf<LatLng>()
            for (step in it.allStep) {
                if (step.wayPoints != null && step.wayPoints.isNotEmpty()) {
                    polylineList.addAll(step.wayPoints)
                }
            }
            this@SearchManager.polylineList.run {
                clear()
                addAll(polylineList)
            }
        }
    }

    interface SearchManagerListener {
        fun onDrivingRouteResultLines(routeLines: List<DrivingRouteLine>?)
    }
}