package com.huolala.mockgps.ui

import android.content.Intent
import android.graphics.Rect
import android.text.TextUtils
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatTextView
import com.baidu.mapapi.map.*
import com.baidu.mapapi.search.route.*
import com.blankj.utilcode.util.*
import com.castiel.common.base.BaseActivity
import com.castiel.common.base.BaseViewModel
import com.huolala.mockgps.R
import com.huolala.mockgps.databinding.ActivityCalculateRouteBinding
import com.huolala.mockgps.manager.FollowMode
import com.huolala.mockgps.manager.MapLocationManager
import com.huolala.mockgps.manager.SearchManager
import com.huolala.mockgps.manager.utils.MapConvertUtils
import com.huolala.mockgps.manager.utils.MapDrawUtils
import com.huolala.mockgps.model.PoiInfoModel
import com.huolala.mockgps.model.PoiInfoType
import java.io.File
import kotlin.collections.ArrayList


/**
 * @author jiayu.liu
 */
class CalculateRouteActivity : BaseActivity<ActivityCalculateRouteBinding, BaseViewModel>(),
    View.OnClickListener {
    private lateinit var mBaiduMap: BaiduMap
    private var mapLocationManager: MapLocationManager? = null
    private val mDefaultPadding = ConvertUtils.dp2px(50f)
    private var mainIndex = 0

    /**
     * 算路成功的路线
     */
    private var routeLines: ArrayList<DrivingRouteLine> = arrayListOf()
    private val mSearchManagerListener  = object : SearchManager.SearchManagerListener {
        override fun onDrivingRouteResultLines(routeLines: List<DrivingRouteLine>?) {
            viewModel.loading.value = false
            if (routeLines?.isEmpty() != false) {
                ToastUtils.showShort("路线规划数据获取失败,请检测网络or数据是否正确!")
                return
            }
            this@CalculateRouteActivity.routeLines = routeLines as ArrayList<DrivingRouteLine>
            mBaiduMap.let {
                (dataBinding.tvStart.tag as PoiInfoModel?)?.latLng?.let { start ->
                    MapDrawUtils.drawMarkerToMap(it, start, "marker_start.png")
                }
                (dataBinding.tvEnd.tag as PoiInfoModel?)?.latLng?.let { end ->
                    MapDrawUtils.drawMarkerToMap(it, end, "marker_end.png")
                }

                routeLines.mapIndexed { index, line ->
                    MapDrawUtils.drawLineToMap(
                        it,
                        MapConvertUtils.convertLatLngList(line),
                        Rect(
                            mDefaultPadding,
                            mDefaultPadding + dataBinding.clPanel.height,
                            mDefaultPadding,
                            mDefaultPadding
                        ),
                        index == mainIndex
                    )
                }
            }
            dataBinding.fileName = "${dataBinding.tvStart.text}-${dataBinding.tvEnd.text}"
        }
    }

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

    override fun initViewModel(): Class<BaseViewModel> {
        return BaseViewModel::class.java
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

        mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomBy(16f))

        mapLocationManager = MapLocationManager(this, mBaiduMap, FollowMode.MODE_SINGLE)
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
                routeLines.clear()
                viewModel.loading.value = true
                SearchManager.INSTANCE.driverSearch(stNode?.location, enNode?.location, true)
            }

            dataBinding.btnChange -> {
                if (routeLines.isEmpty()) {
                    ToastUtils.showShort("没有路线切换")
                    return
                }
                mBaiduMap.clear()

                mainIndex = ++mainIndex % routeLines.size
                mBaiduMap.let {
                    routeLines.mapIndexed { index, line ->
                        MapDrawUtils.drawLineToMap(
                            it,
                            MapConvertUtils.convertLatLngList(line),
                            Rect(
                                mDefaultPadding,
                                mDefaultPadding + dataBinding.clPanel.height,
                                mDefaultPadding,
                                mDefaultPadding
                            ),
                            index == mainIndex
                        )
                    }
                }
                dataBinding.fileName = "${dataBinding.tvStart.text}-${dataBinding.tvEnd.text}"
            }

            dataBinding.btnSaveFile -> {
                if (routeLines.isEmpty() || mainIndex < 0 || mainIndex >= routeLines.size) {
                    ToastUtils.showShort("数据列表为null！，无法保存")
                    return
                }
                val convertLatLngList = MapConvertUtils.convertLatLngList(routeLines[mainIndex])
                val builder = StringBuilder()
                convertLatLngList.map {
                    builder.append(it.longitude).append(",").append(it.latitude).append(";")
                }
                if (!TextUtils.isEmpty(builder)) {
                    val file = File(getExternalFilesDir("nav_path"), "${dataBinding.fileName}.txt")
                    if (FileUtils.isFileExists(file)) {
                        ToastUtils.showShort("文件已经存在！请重命名文件名称")
                        return
                    }
                    FileIOUtils.writeFileFromString(
                        file,
                        builder.toString()
                    ).let {
                        ToastUtils.showShort(if (it) "保存成功" else "保存失败")
                        if (it) {
                            finish()
                        }
                    }
                }
            }

            else -> {
            }
        }
    }

    override fun onResume() {
        super.onResume()
        SearchManager.INSTANCE.addSearchManagerListener(mSearchManagerListener)
        dataBinding.mapview.onResume()
    }

    override fun onPause() {
        super.onPause()
        SearchManager.INSTANCE.removeSearchManagerListener(mSearchManagerListener)
        dataBinding.mapview.onPause()
        if (isFinishing) {
            destroy()
        }
    }

    private fun destroy() {
        mapLocationManager?.onDestroy()
        dataBinding.mapview.onDestroy()
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
}