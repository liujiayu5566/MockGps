package com.huolala.mockgps.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.baidu.mapapi.map.*
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.core.SearchResult
import com.baidu.mapapi.search.geocode.*
import com.huolala.mockgps.model.PoiInfoModel

import androidx.recyclerview.widget.LinearLayoutManager
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener
import com.baidu.mapapi.search.sug.SuggestionResult
import com.baidu.mapapi.search.sug.SuggestionSearch
import com.baidu.mapapi.search.sug.SuggestionSearchOption
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ToastUtils
import com.castiel.common.base.BaseActivity
import com.castiel.common.base.BaseViewModel
import com.huolala.mockgps.R
import com.huolala.mockgps.adaper.PoiListAdapter
import com.huolala.mockgps.adaper.SimpleDividerDecoration
import com.huolala.mockgps.databinding.ActivityPickBinding
import com.huolala.mockgps.manager.FollowMode
import com.huolala.mockgps.manager.MapLocationManager
import com.huolala.mockgps.model.PoiInfoType
import com.huolala.mockgps.widget.InputLatLngDialog
import kotlinx.android.synthetic.main.activity_pick.*
import java.lang.ref.WeakReference


/**
 * @author jiayu.liu
 */
class PickMapPoiActivity : BaseActivity<ActivityPickBinding, BaseViewModel>(),
    View.OnClickListener {
    private val REVERSE_GEO_CODE = 0
    private val DEFAULT_DELAYED: Long = 100
    private lateinit var mBaiduMap: BaiduMap
    private lateinit var mCoder: GeoCoder
    private var mInputLatLngDialog: InputLatLngDialog? = null
    private var poiListAdapter: PoiListAdapter = PoiListAdapter()
    private var mPoiInfoModel: PoiInfoModel? = null
    private var mSuggestionSearch: SuggestionSearch = SuggestionSearch.newInstance()
    private var mHandler: PickMapPoiHandler? = null
    private var mapLocationManager: MapLocationManager? = null
    private var mIndex = -1

    @PoiInfoType
    private var poiInfoType: Int = PoiInfoType.DEFAULT

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

    private fun reverseGeoCode(latLng: LatLng?) {
        latLng?.let {
            mCoder.reverseGeoCode(
                ReverseGeoCodeOption()
                    .location(it)
                    .newVersion(1)
                    .radius(500)
            )
        }
    }

    override fun initViewModel(): Class<BaseViewModel> {
        return BaseViewModel::class.java
    }

    override fun getLayout(): Int {
        return R.layout.activity_pick
    }

    override fun initView() {
        dataBinding.clicklistener = this
        dataBinding.isShowSearch = false

        mHandler = PickMapPoiHandler(this)
        poiInfoType =
            intent?.run { getIntExtra("from_tag", PoiInfoType.DEFAULT) } ?: PoiInfoType.DEFAULT
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
                    this@PickMapPoiActivity.mPoiInfoModel = PoiInfoModel(
                        pt,
                        poiInfo.uid,
                        key,
                        poiInfoType,
                        city
                    )
                    tv_poi_name.text = key
                    tv_lonlat.text = pt?.toString()
                    editViewShow(false)
                    mHandler = null
                    changeCenterLatLng(pt.latitude, pt.longitude)
                    dataBinding.city = city
                }
            }
        })

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
        initMap()
    }

    override fun initData() {
        mIndex = intent.getIntExtra("index", -1)
    }

    override fun initObserver() {
    }

    private fun initMap() {
        mBaiduMap = mapview.map

        mBaiduMap.uiSettings?.run {
            isRotateGesturesEnabled = false
            isOverlookingGesturesEnabled = false
            setEnlargeCenterWithDoubleClickEnable(true)
        }

        mCoder = GeoCoder.newInstance()
        mCoder.setOnGetGeoCodeResultListener(object : OnGetGeoCoderResultListener {
            override fun onGetGeoCodeResult(geoCodeResult: GeoCodeResult?) {

            }

            override fun onGetReverseGeoCodeResult(reverseGeoCodeResult: ReverseGeoCodeResult?) {
                reverseGeoCodeResult?.run {
                    if (error != SearchResult.ERRORNO.NO_ERROR) {
                        Toast.makeText(
                            this@PickMapPoiActivity,
                            "逆地理编码失败",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                    var name: String? = null

                    poiRegionsInfoList?.get(0)?.run {
                        name = regionName
                    }

                    if (TextUtils.isEmpty(name)) {
                        name =
                            if (!TextUtils.isEmpty(sematicDescription)) sematicDescription
                            else if (!TextUtils.isEmpty(address)) address else "未知地址"
                    }
                    dataBinding.city = addressDetail?.city ?: "北京市"
                    mPoiInfoModel = PoiInfoModel(
                        location,
                        location.toString(),
                        name,
                        poiInfoType,
                        addressDetail?.city ?: "北京市"
                    )
                    tv_poi_name.text = name
                    tv_lonlat.text = location?.toString()
                }
            }
        })

        var follow = FollowMode.MODE_SINGLE

        intent.getParcelableExtra<PoiInfoModel>("model")?.run model@{
            mPoiInfoModel = this
            latLng?.run {
                mPoiInfoModel = this@model
                tv_poi_name.text = this@model.name
                tv_lonlat.text = this@model.latLng.toString()
                editViewShow(false)
                mHandler = null
                changeCenterLatLng(latitude, longitude)
                follow = FollowMode.MODE_NONE
                dataBinding.city = this@model.city
            }
        }

        //设置locationClientOption
        mapLocationManager = MapLocationManager(this, mBaiduMap, follow)
        mSuggestionSearch.setOnGetSuggestionResultListener(listener)

        mBaiduMap.setOnMapStatusChangeListener(object : BaiduMap.OnMapStatusChangeListener {
            override fun onMapStatusChangeStart(mapStatus: MapStatus?) {
            }

            override fun onMapStatusChangeStart(mapStatus: MapStatus?, reason: Int) {
                mHandler?.removeMessages(REVERSE_GEO_CODE)
                editViewShow(false)
            }

            override fun onMapStatusChange(mapStatus: MapStatus?) {
            }

            override fun onMapStatusChangeFinish(mapStatus: MapStatus?) {
                val latLng = mapStatus?.target
                mBaiduMap.clear()
                mHandler?.removeMessages(REVERSE_GEO_CODE)
                mHandler?.sendMessageDelayed(Message.obtain().apply {
                    what = REVERSE_GEO_CODE
                    obj = latLng
                }, DEFAULT_DELAYED)
                //处理搜索选址还原拖图选址功能
                if (mHandler == null) {
                    mHandler = PickMapPoiHandler(this@PickMapPoiActivity)
                }
            }
        })
    }

    private fun changeCenterLatLng(latitude: Double, longitude: Double) {
        if (latitude > 0.0 && longitude > 0.0) {
            mBaiduMap.animateMapStatus(
                MapStatusUpdateFactory.newLatLngZoom(
                    LatLng(
                        latitude,
                        longitude
                    ), 16f
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()
        mapview.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapview.onPause()
        if (isFinishing) {
            destroy()
        }
    }

    private fun destroy() {
        mapLocationManager?.onDestroy()
        mSuggestionSearch.destroy()
        mCoder.destroy()
        mapview.onDestroy()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_search -> {
                et_search.setText("")
                editViewShow(true)
            }

            R.id.confirm_location -> {
                mPoiInfoModel?.let {
                    if ((it.latLng?.longitude ?: 0.0) <= 0.0 || (it.latLng?.latitude
                            ?: 0.0) <= 0.0
                    ) {
                        ToastUtils.showShort("数据异常，请重新选择！")
                        return@let
                    }
                    val intent = Intent()
                    val bundle = Bundle()
                    bundle.putParcelable(
                        "poiInfo",
                        it
                    )
                    if (mIndex != -1) {
                        bundle.putInt("index", mIndex)
                    }
                    intent.putExtras(bundle)
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }

            R.id.iv_cur_location -> {
                mBaiduMap.locationData?.run {
                    changeCenterLatLng(latitude, longitude)
                }
            }

            R.id.iv_back -> {
                finish()
            }

            R.id.tv_input -> {
                mInputLatLngDialog?.dismiss()
                mInputLatLngDialog =
                    InputLatLngDialog(this, object : InputLatLngDialog.InputLatLngDialogListener {
                        override fun onConfirm(latLng: LatLng) {
                            changeCenterLatLng(latLng.latitude, latLng.longitude)
                        }
                    }).apply { show() }
            }

            else -> {
            }
        }
    }

    private fun editViewShow(isShow: Boolean) {
        dataBinding.isShowSearch = isShow
        val layoutParams = ll_search.layoutParams
        layoutParams?.run {
            width =
                if (isShow) ViewGroup.LayoutParams.MATCH_PARENT else ViewGroup.LayoutParams.WRAP_CONTENT
        }
        ll_search.layoutParams = layoutParams
        et_search.visibility = if (isShow) View.VISIBLE else View.GONE
        et_search_city.visibility = if (isShow) View.VISIBLE else View.GONE
        if (!isShow) {
            KeyboardUtils.hideSoftInput(et_search)
            recycler.visibility = View.GONE
        }
    }


    class PickMapPoiHandler(activity: PickMapPoiActivity) : Handler(Looper.getMainLooper()) {
        private var mWeakReference: WeakReference<PickMapPoiActivity>? = null

        init {
            mWeakReference = WeakReference(activity)
        }

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            mWeakReference?.get()?.let {
                when (msg.what) {
                    it.REVERSE_GEO_CODE -> {
                        it.reverseGeoCode(msg.obj as LatLng)
                    }

                    else -> {
                    }
                }
            }
        }
    }
}