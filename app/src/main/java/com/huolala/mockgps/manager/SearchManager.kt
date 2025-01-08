package com.huolala.mockgps.manager

import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.route.BikingRoutePlanOption
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
import com.huolala.mockgps.model.RouteLines

/**
 * @author jiayu.liu
 */
class SearchManager private constructor() {
    private var mSearch: RoutePlanSearch = RoutePlanSearch.newInstance()
    private var isSearchIng = false
    private var listenerList: ArrayList<SearchManagerListener> = arrayListOf()
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
                val routeLines = arrayListOf<RouteLines>()
                drivingRouteResult?.routeLines?.map {
                    val route = arrayListOf<LatLng>()
                    it.allStep.map { step ->
                        route.addAll(step.wayPoints)
                    }
                    if (route.size > 0) {
                        routeLines.add(RouteLines(route))
                    }
                }
                listenerList.map {
                    it.onRouteResultLines(routeLines)
                }
            }

            override fun onGetIndoorRouteResult(indoorRouteResult: IndoorRouteResult?) {
            }

            override fun onGetBikingRouteResult(bikingRouteResult: BikingRouteResult?) {
                isSearchIng = false
                val routeLines = arrayListOf<RouteLines>()
                bikingRouteResult?.routeLines?.map {
                    val route = arrayListOf<LatLng>()
                    it.allStep.map { step ->
                        route.addAll(step.wayPoints)
                    }
                    if (route.size > 0) {
                        routeLines.add(RouteLines(route))
                    }
                }
                listenerList.map {
                    it.onRouteResultLines(routeLines)
                }
            }
        })
    }

    fun addSearchManagerListener(listener: SearchManagerListener) {
        listenerList.add(listener)
    }

    fun removeSearchManagerListener(listener: SearchManagerListener) {
        listenerList.remove(listener)
    }

    /**
     * 驾车导航
     * @param startLatLng 起点
     * @param endLatLng 终点
     * @param multiRoute 是否多路径
     * @param wayList 途经点
     */
    fun driverSearch(
        startLatLng: LatLng?,
        endLatLng: LatLng?,
        multiRoute: Boolean,
        wayList: MutableList<LatLng>? = null,
    ) {
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
                    wayList?.let {
                        val passByList = arrayListOf<PlanNode>()
                        for (latLng in it) {
                            passByList.add(PlanNode.withLocation(latLng))
                        }
                        if (passByList.isNotEmpty()) {
                            passBy(passByList)
                        }
                    }
                    if (!multiRoute) {
                        this.policy(DrivingRoutePlanOption.DrivingPolicy.ECAR_DIS_FIRST)
                    }
                }
        )
    }

    /**
     * 骑行导航
     * @param startLatLng 起点
     * @param endLatLng 终点
     * @param wayList 途经点
     * @param ridingType 骑行类型（0：普通骑行模式，1：电动车模式）
     */
    fun bikingSearch(
        startLatLng: LatLng?,
        endLatLng: LatLng?,
        wayList: MutableList<LatLng>? = null,
        ridingType: Int = 0
    ) {
        if (startLatLng == null || endLatLng == null) {
            return
        }
        if (isSearchIng) {
            ToastUtils.showShort("路线规划中,请稍后.")
            return
        }
        isSearchIng = true
        mSearch.bikingSearch(
            BikingRoutePlanOption()
                .from(PlanNode.withLocation(startLatLng))
                .to(PlanNode.withLocation(endLatLng)).apply {
                    wayList?.let {
                        val passByList = arrayListOf<PlanNode>()
                        for (latLng in it) {
                            passByList.add(PlanNode.withLocation(latLng))
                        }
                        if (passByList.isNotEmpty()) {
                            passBy(passByList)
                        }
                    }
                    //默认普通骑行
                    ridingType(ridingType)
                }
        )
    }

    fun selectDriverLine(routeLine: RouteLines?) {
        routeLine?.let {
            this@SearchManager.polylineList.run {
                clear()
                addAll(routeLine.route)
            }
        }
    }

    interface SearchManagerListener {
        fun onRouteResultLines(routeLines: List<RouteLines>?)
    }
}