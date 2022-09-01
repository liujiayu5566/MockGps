package com.huolala.mockgps.ui

import android.content.Intent
import android.text.TextUtils
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatTextView
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.map.*
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.model.LatLngBounds
import com.baidu.mapapi.search.route.*
import com.blankj.utilcode.util.*
import com.castiel.common.base.BaseActivity
import com.castiel.common.base.BaseViewModel
import com.huolala.mockgps.R
import com.huolala.mockgps.databinding.ActivityCalculateRouteBinding
import com.huolala.mockgps.model.PoiInfoModel
import com.huolala.mockgps.model.PoiInfoType
import java.io.File
import kotlin.collections.ArrayList


/**
 * @author jiayu.liu
 */
class CalculateRouteActivity : BaseActivity<ActivityCalculateRouteBinding, BaseViewModel>(),
    View.OnClickListener {
    private lateinit var mLocationClient: LocationClient
    private lateinit var mBaiduMap: BaiduMap
    private val mDefaultPadding = 50
    private var mSearch: RoutePlanSearch = RoutePlanSearch.newInstance()

    /**
     * 算路成功的路线
     */
    private var mPaths: ArrayList<ArrayList<LatLng>> = arrayListOf()

    private var registerForActivityResultToStartPoi =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                it.data?.run {
                    setDataToView(getParcelableExtra<PoiInfoModel>("poiInfo"), dataBinding.tvStart)
                }
            }
        }


    private var registerForActivityResultToEndPoi =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                it.data?.run {
                    setDataToView(getParcelableExtra<PoiInfoModel>("poiInfo"), dataBinding.tvEnd)
                }
            }
        }

    //注册LocationListener监听器
    private val myLocationListener = object : BDAbstractLocationListener() {
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

    override fun initViewModel(): Class<BaseViewModel> {
        return BaseViewModel::class.java
    }

    override fun initViewModelId(): Int? {
        return null
    }

    override fun getLayout(): Int {
        return R.layout.activity_calculate_route
    }

    override fun initView() {
        initMap()

        System.currentTimeMillis()

        ClickUtils.applySingleDebouncing(dataBinding.tvStart, this)
        ClickUtils.applySingleDebouncing(dataBinding.tvEnd, this)
        ClickUtils.applySingleDebouncing(dataBinding.btnChange, this)
        ClickUtils.applySingleDebouncing(dataBinding.btnStartRoute, this)
        ClickUtils.applySingleDebouncing(dataBinding.btnSaveFile, this)
    }


    private fun initMap() {

        mBaiduMap = dataBinding.mapview.map
        mBaiduMap.isMyLocationEnabled = true

        mBaiduMap.setMyLocationConfiguration(
            MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.NORMAL,
                true, null
            )
        )

        mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomBy(16f))

        mLocationClient = LocationClient(this)

        //通过LocationClientOption设置LocationClient相关参数
        val option = LocationClientOption()
        option.isOpenGps = true // 打开gps
        option.setOnceLocation(true) // 设置是否进行单次定位，单次定位时调用start之后会默认返回一次定位结果

        //设置locationClientOption
        mLocationClient.locOption = option

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
                //选址主路线  默认第一个为主路线
                var isMaster = true
                drivingRouteResult?.routeLines?.let {
                    it.map { data ->
                        val polylineList = arrayListOf<LatLng>()
                        for (step in data.allStep) {
                            if (step.wayPoints != null && step.wayPoints.isNotEmpty()) {
                                polylineList.addAll(step.wayPoints)
                            }
                        }
                        drawLineToMap(polylineList, isMaster)
                        isMaster = false
                    }
                    dataBinding.fileName = System.currentTimeMillis().toString()
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


    override fun initData() {

    }


    override fun initObserver() {
    }

    override fun onClick(v: View?) {
        when (v) {
            dataBinding.tvStart -> {
                registerForActivityResultToStartPoi.launch(Intent(
                    this@CalculateRouteActivity,
                    PickMapPoiActivity::class.java
                ).apply {
                    putExtra("from_tag", PoiInfoType.DEFAULT)
                    if (dataBinding.tvStart.tag != null) {
                        putExtra("model", dataBinding.tvStart.tag as PoiInfoModel?)
                    }
                })
            }
            dataBinding.tvEnd -> {
                registerForActivityResultToEndPoi.launch(Intent(
                    this@CalculateRouteActivity,
                    PickMapPoiActivity::class.java
                ).apply {
                    putExtra("from_tag", PoiInfoType.DEFAULT)
                    if (dataBinding.tvEnd.tag != null) {
                        putExtra("model", dataBinding.tvEnd.tag as PoiInfoModel?)
                    }
                })
            }
            dataBinding.btnStartRoute -> {
                var stNode: PlanNode? = null
                (dataBinding.tvStart.tag as PoiInfoModel?)?.run {
                    stNode = PlanNode.withLocation(latLng);
                }

                var enNode: PlanNode? = null
                (dataBinding.tvEnd.tag as PoiInfoModel?)?.run {
                    enNode = PlanNode.withLocation(latLng);
                }

                if (stNode == null || enNode == null) {
                    ToastUtils.showShort("起终点不能null")
                    return
                }
                mBaiduMap.clear()
                mPaths.clear()
                mSearch.drivingSearch(
                    DrivingRoutePlanOption()
                        .from(stNode)
                        .to(enNode)
                )
            }
            dataBinding.btnChange -> {
                if (mPaths.isEmpty()) {
                    ToastUtils.showShort("没有路线切换")
                    return
                }
                mBaiduMap.clear()
                var isMaster = true
                ArrayList(mPaths).let {
                    mPaths.clear()
                    it.map { data ->
                        drawLineToMap(data, isMaster)
                        isMaster = false
                    }
                }
                dataBinding.fileName = System.currentTimeMillis().toString()
            }
            dataBinding.btnSaveFile -> {
                if (mPaths.isEmpty()) {
                    ToastUtils.showShort("数据列表为null！，无法保存")
                    return
                }
                val builder = StringBuilder()
                mPaths[mPaths.size - 1].map {
                    builder.append(it.longitude).append(",").append(it.latitude).append(";")
                }
                if (!TextUtils.isEmpty(builder)) {
                    viewModel.loading.value = true
                    val file = File(getExternalFilesDir("nav_path"), "${dataBinding.fileName}.txt")
                    if (FileUtils.isFileExists(file)) {
                        ToastUtils.showShort("文件已经存在！请重命名文件名称")
                        return
                    }
                    FileIOUtils.writeFileFromString(
                        file,
                        builder.toString()
                    ).let {
                        viewModel.loading.value = false
                        ToastUtils.showShort(if (it) "保存成功" else "保存失败")
                    }
                }
            }
            else -> {
            }
        }
    }

    override fun onResume() {
        super.onResume()
        dataBinding.mapview.onResume()
    }

    override fun onPause() {
        super.onPause()
        dataBinding.mapview.onPause()
        if (isFinishing) {
            destroy()
        }
    }

    private fun destroy() {
        mLocationClient.unRegisterLocationListener(myLocationListener)
        mLocationClient.stop()
        mBaiduMap.isMyLocationEnabled = false
        dataBinding.mapview.onDestroy()
        mSearch.destroy()
    }


    private fun setDataToView(model: PoiInfoModel?, view: AppCompatTextView) {
        model?.run {
            when (poiInfoType) {
                PoiInfoType.DEFAULT -> {
                    view.text = String.format(
                        "%s",
                        name
                    )
                    view.tag = this
                }
                else -> {
                }
            }
        }
    }

    private fun drawLineToMap(polylineList: ArrayList<LatLng>?, isMaster: Boolean = true) {
        if (polylineList == null || polylineList.size == 0) {
            return
        }

        val color: Int
        if (isMaster) {
            color = 0xFF00FF00.toInt()
            mPaths.add(polylineList)
        } else {
            color = 0xFF0000FF.toInt()
            mPaths.add(mPaths.size - 1, polylineList)
        }

        val mOverlayOptions: OverlayOptions = PolylineOptions()
            .width(15)
            .color(color)
            .zIndex(if (isMaster) 1 else 0)
            .lineCapType(PolylineOptions.LineCapType.LineCapRound)
            .points(polylineList)
        mBaiduMap.addOverlay(mOverlayOptions)
        if (isMaster) {
            mBaiduMap.animateMapStatus(
                MapStatusUpdateFactory.newLatLngBounds(
                    LatLngBounds.Builder().include(polylineList).build(),
                    mDefaultPadding,
                    if (dataBinding.clPanel.height == 0)
                        mDefaultPadding
                    else
                        dataBinding.clPanel.height + mDefaultPadding,
                    mDefaultPadding,
                    mDefaultPadding
                )
            )
        }
    }
}