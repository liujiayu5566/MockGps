package com.huolala.mockgps.server

import android.app.Service
import android.content.Intent
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.*
import android.provider.Settings
import android.view.*
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.route.*
import com.blankj.utilcode.util.*
import com.huolala.mockgps.manager.FloatingViewManger
import com.huolala.mockgps.manager.SearchManager
import com.huolala.mockgps.model.MockMessageModel
import com.huolala.mockgps.model.NaviType
import com.huolala.mockgps.model.PoiInfoModel
import com.huolala.mockgps.utils.CalculationLogLatDistance
import com.huolala.mockgps.utils.LocationUtils
import com.huolala.mockgps.utils.Utils
import kotlinx.android.synthetic.main.layout_floating.view.*

/**
 * @author jiayu.liu
 */
class GpsAndFloatingService : Service() {
    private val START_MOCK_LOCATION = 1001
    private val START_MOCK_LOCATION_NAVI = 1002
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
    private lateinit var mCurrentLocation: LatLng

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
//                                view.tv_progress.text = String.format("%d / %d", 0, 0)
                                startSimulateLocation(it, true)
                                handle.sendMessageDelayed(Message.obtain(msg), mNaviUpdateValue)
                            }
                        }
                    }

                    START_MOCK_LOCATION_NAVI -> {
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
//                                view.tv_progress.text = String.format("%d / %d", index, it.size)
                                startSimulateLocation(mCurrentLocation, false)
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
        initFloatingView()
    }

    private fun initLocationManager() {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
    }

    private fun initFloatingView() {
        //浮动窗
        FloatingViewManger.INSTANCE.run {
            addFloatViewToWindow()
            listener = object:FloatingViewManger.FloatingViewListener{
                override fun pause() {
                    removeGps()
                    handle.removeCallbacksAndMessages(null)
                }

                override fun reStart() {
                    SearchManager.INSTANCE.polylineList?.let {
                        sendHandler(
                            START_MOCK_LOCATION_NAVI,
                            it
                        )
                    }
                }

            }
        }
    }

    fun getLatLngNext(polyline: ArrayList<*>): LatLng {
        //根据循环间隔处理  目前按照500ms进行处理  将speed进行除2处理  speed单位:m/s
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
            var location =
                CalculationLogLatDistance.getNextLonLat(mCurrentLocation, yaw, mSpeed.toDouble())
            println("${location.latitude}  ||  ${location.longitude}")
            //计算经纬度为非法值则直接取下一阶段经纬度更新
            if (location.latitude <= 0.0 || location.longitude <= 0.0 || location.latitude.isNaN() || location.longitude.isNaN()) {
                location = polyline[index] as LatLng
                index++
                println("非法")
            } else {
                println("计算经纬度 $index ,  $mSpeed , $dis , $yaw")
            }
            return location
        }

        //终点
        if (index >= polyLineCount - 1) {
            val latLng = polyline[polyLineCount - 1] as LatLng
            index++
            println("终点")
            return latLng
        }
        if (dis > 0) {
            println("直接取下一阶段经纬 $index ,  $mSpeed , $dis , $yaw")
            index++
            return indexLonLat
        }
        //循环递归计算经纬度
        index++
        println("递归")
        return getLatLngNext(polyline)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //开始模拟
        model = null
        if (Utils.isAllowMockLocation(this)) {
            intent?.run {
                model = getParcelableExtra("info")
            }
        }
        mockLocation()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun mockLocation() {
        model?.run {
            when (naviType) {
                NaviType.LOCATION -> {
                    sendHandler(START_MOCK_LOCATION, locationModel)
                }

                NaviType.NAVI -> {
                    mSpeed = speed / 3.6f
                    //算路成功后 startService
                    index = 0
                    SearchManager.INSTANCE.polylineList?.let {
                        sendHandler(
                            START_MOCK_LOCATION_NAVI,
                            it
                        )
                    }
                }

                NaviType.NAVI_FILE -> {
                    try {
                        mSpeed = speed / 3.6f
                        val polylineList = arrayListOf<LatLng>()
                        val readFile2String = FileIOUtils.readFile2String(path)
                        readFile2String?.run {
                            split(";").run {
                                if (isNotEmpty()) {
                                    map {
                                        it.split(",").run {
                                            if (size == 2) {
                                                polylineList.add(
                                                    LatLng(
                                                        get(1).toDouble(),
                                                        get(0).toDouble()
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            index = 0
                            sendHandler(
                                START_MOCK_LOCATION_NAVI,
                                polylineList
                            )
                        } ?: kotlin.run { ToastUtils.showShort("文件无法读取") }

                    } catch (e: Exception) {
                        ToastUtils.showShort(
                            "文件解析失败，是否点串格式正确 \n" +
                                    " ${e.printStackTrace()}"
                        )
                    }
                }

                else -> {
                }
            }
        } ?: run {
            isStart = false
//            view.isSelected = isStart
        }
    }

    private fun sendHandler(code: Int, model: Any?) {
        val msg: Message = Message.obtain().apply {
            what = code
            obj = model
        }
        msg.let {
            removeGps()
            handle.removeCallbacksAndMessages(null)
            isStart = true
//            view.isSelected = isStart
            handle.sendMessage(it)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
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
        loc.bearing = bearing
        loc.speed = if (isSingle) 0F else mSpeed
        loc.longitude = gps84[0]
        loc.latitude = gps84[1]
        loc.time = System.currentTimeMillis()
        loc.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()

        mockGps(loc)
    }

    private fun removeGps() {
        try {
            locationManager?.run {
                removeTestProvider(providerStr)
                removeTestProvider(networkStr)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun mockGps(location: Location) {
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
                setTestProviderLocation(providerStr, location)
                //network
                location.run {
                    provider = networkStr
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
                    if (!isProviderEnabled(networkStr)) {
                        setTestProviderEnabled(networkStr, true)
                    }
                    setTestProviderLocation(networkStr, location)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}