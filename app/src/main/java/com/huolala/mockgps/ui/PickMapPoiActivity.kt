package com.huolala.mockgps.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.map.*
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.core.PoiInfo
import com.baidu.mapapi.search.core.SearchResult
import com.baidu.mapapi.search.geocode.*
import com.huolala.mockgps.model.PoiInfoModel

import com.huolala.mockgps.utils.KeyboardUtils

import androidx.recyclerview.widget.LinearLayoutManager
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener
import com.baidu.mapapi.search.sug.SuggestionResult
import com.baidu.mapapi.search.sug.SuggestionSearch
import com.baidu.mapapi.search.sug.SuggestionSearchOption
import com.huolala.mockgps.R
import com.huolala.mockgps.adaper.PoiListAdapter
import com.huolala.mockgps.adaper.SimpleDividerDecoration
import kotlinx.android.synthetic.main.activity_pick.*


/**
 * @author jiayu.liu
 */
class PickMapPoiActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var mLocationClient: LocationClient
    private lateinit var mBaiduMap: BaiduMap
    private lateinit var mCoder: GeoCoder
    private var poiListAdapter: PoiListAdapter = PoiListAdapter()
    private var poiInfo: PoiInfo? = null
    private var marker: Overlay? = null
    private var mSuggestionSearch: SuggestionSearch = SuggestionSearch.newInstance()
    private var mCity: String = ""

    //检索
    private val listener: OnGetSuggestionResultListener =
        OnGetSuggestionResultListener { suggestionResult -> //处理sug检索结果
            if (et_search.visibility == View.VISIBLE && !TextUtils.isEmpty(et_search.text)) {
                suggestionResult.allSuggestions?.let {
                    poiListAdapter.setData(it)
                    recycler.visibility = View.VISIBLE
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
            mCity = location.city ?: ""
            val locData = MyLocationData.Builder()
                .accuracy(location.radius) // 此处设置开发者获取到的方向信息，顺时针0-360
                .direction(location.direction).latitude(location.latitude)
                .longitude(location.longitude).build()
            mBaiduMap.setMyLocationData(locData)
            //更新中心点
            changeCenterLatLng(locData.latitude, locData.longitude)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick)
        initView()
        initMap()
    }

    private fun initView() {
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = poiListAdapter
        recycler.addItemDecoration(SimpleDividerDecoration(this))
        recycler.itemAnimator = null

        poiListAdapter.setOnItemClickListener(object : PoiListAdapter.OnItemClickListener {
            override fun onItemClick(poiInfo: SuggestionResult.SuggestionInfo) {
                poiInfo.run {
                    if (pt == null) {
                        return@run
                    }
                    this@PickMapPoiActivity.poiInfo = PoiInfo().apply {
                        name = key
                        location = pt
                    }
                    tv_poi_name.text = key
                    tv_lonlat.text = pt?.toString()
                    drawMarker(pt)
                }
            }
        })

        iv_search.setOnClickListener(this)
        confirm_location.setOnClickListener(this)

        et_search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (!TextUtils.isEmpty(s)) {
                    mSuggestionSearch.requestSuggestion(
                        SuggestionSearchOption()
                            .city(if (et_search_city.text?.isNotEmpty() == true) et_search_city.text.toString() else "中国")
                            .keyword(s.toString()) //必填
                    )
                } else {
                    poiListAdapter.setData(null)
                    recycler.visibility = View.GONE
                }

            }

        })
    }

    private fun initMap() {
        mBaiduMap = mapview.map
        mBaiduMap.isMyLocationEnabled = true

        mBaiduMap.setMyLocationConfiguration(
            MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.NORMAL,
                true,
                BitmapDescriptorFactory.fromResource(R.drawable.ic_my_location),
            )
        )

        mLocationClient = LocationClient(this)

        //通过LocationClientOption设置LocationClient相关参数
        val option = LocationClientOption()
        option.isOpenGps = true // 打开gps
        option.setCoorType("bd09ll") // 设置坐标类型
        option.setScanSpan(1000)

        //设置locationClientOption
        mLocationClient.locOption = option
        mSuggestionSearch.setOnGetSuggestionResultListener(listener)


        mLocationClient.registerLocationListener(myLocationListener)
        //开启地图定位图层
        mLocationClient.start()



        mCoder = GeoCoder.newInstance()
        mCoder.setOnGetGeoCodeResultListener(object : OnGetGeoCoderResultListener {
            override fun onGetGeoCodeResult(geoCodeResult: GeoCodeResult?) {

            }

            override fun onGetReverseGeoCodeResult(reverseGeoCodeResult: ReverseGeoCodeResult?) {
                reverseGeoCodeResult?.run {
                    if (error != SearchResult.ERRORNO.NO_ERROR) {
                        Toast.makeText(this@PickMapPoiActivity, "逆地理编码失败", Toast.LENGTH_SHORT)
                            .show()
                        return
                    }

                    //详细地址
                    reverseGeoCodeResult.poiList?.run {
                        if (!isEmpty()) {
                            poiInfo = get(0)
                            tv_poi_name.text = poiInfo?.name
                            tv_lonlat.text = poiInfo?.location?.toString()
                        }
                    }
                }
            }
        })


        mBaiduMap.setOnMapClickListener(object : BaiduMap.OnMapClickListener {

            override fun onMapClick(latLng: LatLng?) {
                latLng?.run {
                    mCoder.reverseGeoCode(
                        ReverseGeoCodeOption()
                            .location(latLng)
                            .newVersion(1)
                            .radius(500)
                    )
                    drawMarker(this)
                }
            }

            override fun onMapPoiClick(mapPoi: MapPoi?) {
                mapPoi?.run {
                    mCoder.reverseGeoCode(
                        ReverseGeoCodeOption()
                            .location(position)
                            .newVersion(1)
                            .radius(500)
                    )
                    drawMarker(position)
                }
            }

        })
    }

    private fun drawMarker(position: LatLng) {
        marker?.let {
            it.remove()
            null
        }
        //构建Marker图标
        MarkerOptions()
            .position(position)
            .icon(
                BitmapDescriptorFactory
                    .fromResource(R.drawable.ic_location)
            ).apply {
                marker = mBaiduMap.addOverlay(this)
                changeCenterLatLng(position.latitude, position.longitude)
            }
        editViewShow(false)
    }

    private fun changeCenterLatLng(latitude: Double, longitude: Double) {
        mBaiduMap.animateMapStatus(
            MapStatusUpdateFactory.newLatLngZoom(
                LatLng(
                    latitude,
                    longitude
                ), 16f
            )
        )
    }

    override fun onResume() {
        super.onResume()
        mapview.onResume()
    }

    override fun onPause() {
        if (isFinishing) {
            destroy()
        }
        super.onPause()
        mapview.onPause()
    }

    private fun destroy() {
        mSuggestionSearch.destroy()
        mCoder.destroy()
        mLocationClient.unRegisterLocationListener(myLocationListener)
        mLocationClient.stop()
        mBaiduMap.isMyLocationEnabled = false
        mapview.onDestroy()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_search -> {
                et_search.setText("")
                editViewShow(true)
            }
            R.id.confirm_location -> {
                poiInfo?.run {
                    val intent = Intent()
                    val bundle = Bundle()
                    bundle.putParcelable(
                        "poiInfo",
                        PoiInfoModel(
                            location,
                            uid,
                            name,
                            getIntent()?.run { getIntExtra("from_tag", 0) } ?: 0
                        )
                    )
                    intent.putExtras(bundle)
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
            else -> {}
        }
    }

    private fun editViewShow(isShow: Boolean) {
        val layoutParams = ll_search.layoutParams
        layoutParams?.run {
            width =
                if (isShow) ViewGroup.LayoutParams.MATCH_PARENT else ViewGroup.LayoutParams.WRAP_CONTENT
        }
        ll_search.layoutParams = layoutParams
        et_search.visibility = if (isShow) View.VISIBLE else View.GONE
        et_search_city.visibility = if (isShow) View.VISIBLE else View.GONE
        if (!isShow) {
            KeyboardUtils.hideSoftInput(this, et_search)
            recycler.visibility = View.GONE
        }
    }

}