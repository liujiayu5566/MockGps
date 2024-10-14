package com.huolala.mockgps.server

import android.app.Service
import android.content.Intent
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.*
import com.baidu.mapapi.model.LatLng
import com.blankj.utilcode.util.ToastUtils
import com.huolala.mockgps.manager.FloatingViewManger
import com.huolala.mockgps.manager.SearchManager
import com.huolala.mockgps.model.MockMessageModel
import com.huolala.mockgps.model.NaviType
import com.huolala.mockgps.model.PoiInfoModel
import com.huolala.mockgps.model.PoiInfoType
import com.huolala.mockgps.utils.CalculationLogLatDistance
import com.huolala.mockgps.utils.LocationUtils
import com.huolala.mockgps.utils.MMKVUtils
import com.huolala.mockgps.utils.RouteBindingUtils
import com.huolala.mockgps.utils.Utils
import kotlin.random.Random

/**
 * @author jiayu.liu
 */
class GpsService : Service() {
    companion object {
        private const val START_MOCK_LOCATION = 1001
        private const val START_MOCK_NAVI = 1002
        private const val START_MOCK_FILE_NAVI = 1003
    }

    private var locationManager: LocationManager? = null
    private var isStart = false
    private lateinit var handle: Handler
    private var model: MockMessageModel? = null
    private var index = 0
    private val providerStr: String = LocationManager.GPS_PROVIDER
    private val networkStr: String = LocationManager.NETWORK_PROVIDER
    private var bearing: Float = 1.0f

    /**
     * 米/S
     */
    private var mSpeed: Float = 60 / 3.6f
    private var mCurrentLocation: LatLng? = null
    private var naviType: Int = NaviType.NONE
    private var isLocationAdjust = false

    /**
     * 模拟导航点更新间隔  单位：ms  小于等于1000ms
     */
    private val mNaviUpdateValue = 1000L

    override fun onCreate() {
        super.onCreate()
        handle = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    START_MOCK_LOCATION -> {
                        if (isStart) {
                            (msg.obj as PoiInfoModel?)?.latLng?.let {
                                if (mCurrentLocation == it && isLocationQuiver()) {
                                    CalculationLogLatDistance.getRandomLatLng(
                                        it,
                                        MMKVUtils.getLocationVibrationValue().toDouble()
                                    ).apply {
                                        startSimulateLocation(this, true)
                                    }
                                } else {
                                    mCurrentLocation = it
                                    startSimulateLocation(it, true)
                                }
                                handle.sendMessageDelayed(Message.obtain(msg), mNaviUpdateValue)
                            }
                        }
                    }

                    START_MOCK_NAVI, START_MOCK_FILE_NAVI -> {
                        if (isStart) {
                            (msg.obj as ArrayList<*>?)?.let {
                                if (it.isEmpty()) {
                                    return
                                }
                                if (index == 0) {
                                    mCurrentLocation = it[index] as LatLng
                                    index++
                                } else if (index < it.size) {
                                    mCurrentLocation = getLatLngNext(it)
                                }
                                FloatingViewManger.INSTANCE.updateNaviInfo(index, it.size)
                                startSimulateLocation(mCurrentLocation!!, false)
                                handle.sendMessageDelayed(Message.obtain(msg), mNaviUpdateValue)
                            }
                        }
                    }

                    else -> {
                    }
                }

            }
        }
        initLocationManager()
    }

    private fun initLocationManager() {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
    }

    private fun initFloatingView() {
        //浮动窗
        FloatingViewManger.INSTANCE.run {
            if (isAddFloatingView) {
                return
            }
            addFloatViewToWindow()
            listener = object : FloatingViewManger.FloatingViewListener {
                override fun pause() {
                    removeGps()
                    handle.removeCallbacksAndMessages(null)
                }

                override fun reStart(isRest: Boolean) {
                    model?.run {
                        if (naviType == NaviType.LOCATION) {
                            mockLocation()
                        } else {
                            SearchManager.INSTANCE.polylineList.let {
                                if (it.isEmpty()) {
                                    FloatingViewManger.INSTANCE.stopMock()
                                    return
                                }
                                if (isRest || index >= it.size) {
                                    index = 0
                                }
                                sendHandler(
                                    START_MOCK_NAVI,
                                    it
                                )
                            }
                        }
                    }
                }

                override fun getNaviType(): Int {
                    return naviType
                }

                override fun changeLocation(latLng: LatLng, angle: Double) {
                    if (model == null) {
                        return
                    }
                    isLocationAdjust = true
                    bearing = angle.toFloat()
                    sendMockLocation(latLng)
                }

                override fun locationAdjustFinish() {
                    isLocationAdjust = false
                }

                override fun changeNaviInfo(index: Int, speed: Int) {
                    MMKVUtils.setSpeed(speed)
                    mSpeed = speed / 3.6f
                    //index==-1 仅调整速度
                    if (index >= 0) {
                        SearchManager.INSTANCE.polylineList.let {
                            if (index < it.size) {
                                mCurrentLocation = it[index]
                                this@GpsService.index = index
                            }
                        }
                    }
                    if (!isStart) {
                        reStart(false)
                    }
                }

                override fun switchLocation() {
                    if (model == null) {
                        return
                    }
                    //将模拟导航切换成模拟定位
                    model?.naviType = NaviType.LOCATION
                    sendMockLocation(mCurrentLocation)
                }

                /**
                 * 发送模拟定位Handler
                 */
                private fun sendMockLocation(location: LatLng?) {
                    if (location == null) {
                        ToastUtils.showShort("mock数据异常，此次模拟触发失败！")
                        return
                    }
                    PoiInfoModel(
                        latLng = location,
                        poiInfoType = PoiInfoType.LOCATION,
                    ).also {
                        model?.locationModel = it
                        sendHandler(
                            START_MOCK_LOCATION,
                            it
                        )
                    }
                }

            }
        }
    }

    fun getLatLngNext(polyline: ArrayList<*>): LatLng {
        val mSpeed = this.mSpeed / (1000.0 / mNaviUpdateValue)

        val indexLonLat = polyline[index] as LatLng
        val polyLineCount = polyline.size

        //计算当前位置到index节点的距离
        val dis = CalculationLogLatDistance.getDistance(mCurrentLocation, indexLonLat)
        //计算角度
        val yaw = CalculationLogLatDistance.getYaw(mCurrentLocation, indexLonLat)

        if (!yaw.isNaN()) {
            bearing = yaw.toFloat()
        }

        if (dis > mSpeed) {
            //距离大于speed 计算经纬度
            var location = CalculationLogLatDistance.getNextLonLat(mCurrentLocation, yaw, mSpeed)
            //绑路逻辑 优化计算经纬度的误差
            if (MMKVUtils.isNaviRouteBindingSwitch()) {
                SearchManager.INSTANCE.polylineList.let {
                    if (it.isEmpty()) {
                        return@let
                    }
                    location =
                        RouteBindingUtils.snapToPath(location, SearchManager.INSTANCE.polylineList)
                }
            }

            //计算经纬度为非法值则直接取下一阶段经纬度更新
            if (CalculationLogLatDistance.isCheckNaN(location)) {
                location = polyline[index] as LatLng
                index++
            }
            return location
        }

        //终点
        if (index >= polyLineCount - 1) {
            val latLng = polyline[polyLineCount - 1] as LatLng
            index++
            return latLng
        }
        if (dis > 0) {
            index++
            return indexLonLat
        }
        //循环递归计算经纬度
        index++
        return getLatLngNext(polyline)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //开始模拟
        if (Utils.isAllowMockLocation(this)) {
            intent?.run {
                getParcelableExtra<MockMessageModel?>("info")?.let {
                    model = it
                }
            }
        }
        initFloatingView()
        mockLocation()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun mockLocation() {
        addTestProvider()
        model?.run {
            when (naviType) {
                NaviType.LOCATION -> {
                    sendHandler(START_MOCK_LOCATION, locationModel)
                }

                NaviType.NAVI, NaviType.NAVI_FILE -> {
                    mSpeed = speed / 3.6f
                    //算路成功后 startService
                    index = 0
                    SearchManager.INSTANCE.polylineList.let {
                        sendHandler(
                            START_MOCK_NAVI,
                            it
                        )
                    }
                }

                else -> {
                }
            }
        } ?: run {
            isStart = false
        }
    }

    private fun sendHandler(code: Int, model: Any?) {
        val msg: Message = Message.obtain().apply {
            what = code
            obj = model
        }
        when (code) {
            START_MOCK_LOCATION -> this.naviType = NaviType.LOCATION
            START_MOCK_NAVI -> this.naviType = NaviType.NAVI
            START_MOCK_FILE_NAVI -> this.naviType = NaviType.NAVI_FILE
            else -> {
                this.naviType = NaviType.NONE
            }
        }

        msg.let {
            addTestProvider()
            isStart = true
            handle.removeCallbacksAndMessages(null)
            handle.sendMessage(it)
            FloatingViewManger.INSTANCE.startMock()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        FloatingViewManger.INSTANCE.onDestroy()
        handle.removeCallbacksAndMessages(null)
        removeGps()
    }


    fun startSimulateLocation(latLng: LatLng, isSingle: Boolean) {
        val pointType = model?.pointType ?: LocationUtils.gcj02
        var gps84 = doubleArrayOf(latLng.longitude, latLng.latitude)
        when (pointType) {
            LocationUtils.gcj02 -> {
                gps84 = LocationUtils.gcj02ToWGS84(latLng.longitude, latLng.latitude)
            }

            LocationUtils.bd09 -> {
                gps84 = LocationUtils.bd09ToWGS84(latLng.longitude, latLng.latitude)
            }

            else -> {}
        }

        val loc = Location(providerStr)

        loc.accuracy = 1.0f
        loc.bearing =
            if (isSingle && isLocationQuiver())
                Random.nextInt(0, 360).toFloat()
            else bearing
        loc.speed = if (isSingle) 0F else mSpeed
        loc.longitude = gps84[0]
        loc.latitude = gps84[1]
        loc.altitude = Math.random() * 10
        loc.time = System.currentTimeMillis()
        loc.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
        //通知悬浮窗当前位置
        FloatingViewManger.INSTANCE.setCurLocation(latLng)

        mockGps(loc)
    }

    private fun removeGps() {
        try {
            locationManager?.run {
                setTestProviderEnabled(providerStr, false)
                setTestProviderEnabled(networkStr, false)
                removeTestProvider(providerStr)
                removeTestProvider(networkStr)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isStart = false
            //先通知stopMock  再修改naviType为NaviType.NONE
            FloatingViewManger.INSTANCE.stopMock()
            naviType = NaviType.NONE
        }
    }

    private fun mockGps(location: Location) {
        locationManager?.run {
            try {
                setTestProviderLocation(providerStr, location)
                //network
                location.run {
                    provider = networkStr
                    setTestProviderLocation(networkStr, location)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 是否处于定位震动状态
     */
    private fun isLocationQuiver(): Boolean {
        return MMKVUtils.isLocationVibrationSwitch() && !isLocationAdjust
    }

    private fun addTestProvider() {
        locationManager?.run {
            try {
                var powerUsageMedium = Criteria.POWER_LOW
                var accuracyCoarse = Criteria.ACCURACY_FINE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    powerUsageMedium = ProviderProperties.POWER_USAGE_LOW
                    accuracyCoarse = ProviderProperties.ACCURACY_FINE
                }
                addTestProvider(
                    providerStr,
                    false,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    powerUsageMedium,
                    accuracyCoarse
                )
                if (!isProviderEnabled(providerStr)) {
                    setTestProviderEnabled(providerStr, true)
                }
                addTestProvider(
                    networkStr,
                    true,
                    false,
                    true,
                    true,
                    true,
                    true,
                    true,
                    powerUsageMedium,
                    accuracyCoarse
                )
                //network
                if (!isProviderEnabled(networkStr)) {
                    setTestProviderEnabled(networkStr, true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}