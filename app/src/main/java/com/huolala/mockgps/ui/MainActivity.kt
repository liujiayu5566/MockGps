package com.huolala.mockgps.ui

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.baidu.mapapi.search.route.DrivingRouteLine
import com.blankj.utilcode.util.ClickUtils
import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.ToastUtils
import com.castiel.common.base.BaseActivity
import com.google.android.material.appbar.AppBarLayout
import com.huolala.mockgps.R
import com.huolala.mockgps.adaper.HistoryAdapter
import com.huolala.mockgps.adaper.SimpleDividerDecoration
import com.huolala.mockgps.databinding.ActivityMainBinding
import com.huolala.mockgps.manager.SearchManager
import com.huolala.mockgps.model.MockMessageModel
import com.huolala.mockgps.model.NaviType
import com.huolala.mockgps.model.PoiInfoModel
import com.huolala.mockgps.model.PoiInfoType
import com.huolala.mockgps.utils.MMKVUtils
import com.huolala.mockgps.utils.Utils
import com.huolala.mockgps.utils.WarnDialogUtils
import com.huolala.mockgps.viewmodel.HomeViewModel
import com.huolala.mockgps.widget.MapSelectDialog
import com.huolala.mockgps.widget.NaviPopupWindow
import kotlinx.android.synthetic.main.activity_main.appBarLayout
import kotlinx.android.synthetic.main.activity_main.collapsingToolbar
import kotlinx.android.synthetic.main.activity_main.iv_app_update
import kotlinx.android.synthetic.main.activity_main.iv_change
import kotlinx.android.synthetic.main.activity_main.iv_expand
import kotlinx.android.synthetic.main.activity_main.ll_card
import kotlinx.android.synthetic.main.activity_main.recycler
import kotlinx.android.synthetic.main.layout_location_card.btn_start_location
import kotlinx.android.synthetic.main.layout_location_card.ll_location_card
import kotlinx.android.synthetic.main.layout_location_card.tv_location_latlng
import kotlinx.android.synthetic.main.layout_location_card.tv_location_name
import kotlinx.android.synthetic.main.layout_navi_card.btn_start_navi
import kotlinx.android.synthetic.main.layout_navi_card.iv_navi_setting
import kotlinx.android.synthetic.main.layout_navi_card.ll_navi_card
import kotlinx.android.synthetic.main.layout_navi_card.radio_multi_route
import kotlinx.android.synthetic.main.layout_navi_card.tv_navi_name_end
import kotlinx.android.synthetic.main.layout_navi_card.tv_navi_name_start
import kotlin.math.abs
import kotlin.math.roundToInt


/**
 * @author jiayu.liu
 */
class MainActivity : BaseActivity<ActivityMainBinding, HomeViewModel>(), View.OnClickListener {
    private var topMarginOffset: Int = 0
    private var topMargin: Int = 0
    private lateinit var adapter: HistoryAdapter
    private var mMapSelectDialog: MapSelectDialog? = null
    private var locationAlwaysView: View? = null
    private val mSearchManagerListener = object : SearchManager.SearchManagerListener {
        override fun onDrivingRouteResultLines(routeLines: List<DrivingRouteLine>?) {
            viewModel.loading.value = false
            if (routeLines?.isEmpty() != false) {
                ToastUtils.showShort("路线规划数据获取失败,请检测网络or数据是否正确!")
                return
            }
            val startNavi = tv_navi_name_start.tag as PoiInfoModel?
            val endNavi = tv_navi_name_end.tag as PoiInfoModel?
            val model = MockMessageModel(
                startNavi = startNavi,
                endNavi = endNavi,
                naviType = NaviType.NAVI,
                speed = MMKVUtils.getSpeed(),
                uid = (startNavi?.uid ?: "") + (endNavi?.uid ?: "")
            )
            if (routeLines.size == 1) {
                goToMockLocation(routeLines[0], model)
            } else {
                mMapSelectDialog = MapSelectDialog(
                    this@MainActivity,
                    routeLines,
                    startNavi?.latLng,
                    endNavi?.latLng
                ).apply {
                    listener = object : MapSelectDialog.MapSelectDialogListener {
                        override fun onSelectLine(routeLine: DrivingRouteLine) {
                            goToMockLocation(routeLine, model)
                            mMapSelectDialog = null
                        }
                    }
                    show()
                }
            }

        }
    }

    private var registerForActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                it.data?.run {
                    val parcelableExtra = this.getParcelableExtra<PoiInfoModel>("poiInfo")
                    setDataToView(parcelableExtra)
                }
            }
        }

    override fun initView() {
        topMarginOffset = -ConvertUtils.dp2px(50f)
        topMargin = ConvertUtils.dp2px(15f)

        adapter = HistoryAdapter()
        recycler.adapter = adapter
        recycler.itemAnimator = null
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.addItemDecoration(SimpleDividerDecoration(this, R.color.transparent, 10f))

        adapter.setOnItemClickListener(object : HistoryAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, model: MockMessageModel) {
                when (model.naviType) {
                    NaviType.LOCATION -> {
                        setDataToView(model.locationModel)
                    }

                    NaviType.NAVI -> {
                        setDataToView(model.startNavi)
                        setDataToView(model.endNavi)
                    }

                    else -> {
                    }
                }
            }

            override fun onItemLongClick(view: View?, model: MockMessageModel) {
//                TODO("Not yet implemented")
            }

        })

        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            appBarLayout?.run {
                val scale = abs(verticalOffset * 1.0f / appBarLayout.totalScrollRange)
                val params = ll_card.layoutParams as ViewGroup.MarginLayoutParams
                val topMarginOffsetValue = (topMarginOffset * (1 - scale)).roundToInt()
                val topMarginValue = (topMargin * scale).roundToInt()
                params.topMargin = topMarginOffsetValue + topMarginValue
                ll_card.layoutParams = params
            }
        })

        ClickUtils.applySingleDebouncing(iv_change, this)
        ClickUtils.applySingleDebouncing(iv_expand, this)
        //location
        ClickUtils.applySingleDebouncing(ll_location_card, this)
        ClickUtils.applySingleDebouncing(btn_start_location, this)
        //navi
        ClickUtils.applySingleDebouncing(tv_navi_name_start, this)
        ClickUtils.applySingleDebouncing(tv_navi_name_end, this)
        ClickUtils.applySingleDebouncing(btn_start_navi, this)
        ClickUtils.applySingleDebouncing(iv_navi_setting, this)
        ClickUtils.applySingleDebouncing(iv_app_update, this)
    }

    private fun goToMockLocation(
        routeLine: DrivingRouteLine,
        model: MockMessageModel
    ) {
        SearchManager.INSTANCE.selectDriverLine(routeLine)
        val intent =
            Intent(this@MainActivity, MockLocationActivity::class.java)
        intent.putExtra("model", model)
        startActivity(intent)
        MMKVUtils.saveNaviData(model)
    }


    override fun initViewModel(): Class<HomeViewModel> {
        return HomeViewModel::class.java
    }

    override fun getLayout(): Int {
        return R.layout.activity_main
    }

    override fun initData() {
        //检测是否有新版本
        viewModel.checkAppUpdate()
    }

    override fun initObserver() {
        viewModel.updateApp.observe(this) {
            dataBinding.isUpdate = true
        }
    }

    override fun onResume() {
        super.onResume()
        //获取历史数据
        SearchManager.INSTANCE.addSearchManagerListener(mSearchManagerListener)
        getHistoryData()
        mMapSelectDialog?.onResume()
        //后台定位提示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!PermissionUtils.isGranted(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                if (locationAlwaysView == null) {
                    locationAlwaysView = LayoutInflater.from(this)
                        .inflate(R.layout.layout_location_always_allow, null)
                    locationAlwaysView?.let {
                        it.findViewById<View>(R.id.btn_skip)?.setOnClickListener {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            intent.data = Uri.parse("package:$packageName");
                            try {
                                startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                ToastUtils.showShort("跳转失败，请手动开启！")
                            }
                        }

                        val frameLayout = this.window.decorView as FrameLayout
                        frameLayout.addView(
                            it,
                            FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                gravity = Gravity.BOTTOM
                                val margin = ConvertUtils.dp2px(10f)
                                setMargins(margin, 0, margin, margin * 2)
                            }
                        )
                    }
                }
            } else {
                locationAlwaysView?.let {
                    (window.decorView as FrameLayout).removeView(it)
                    locationAlwaysView = null
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        SearchManager.INSTANCE.removeSearchManagerListener(mSearchManagerListener)
        mMapSelectDialog?.onPause()
    }

    private fun getHistoryData() {
        MMKVUtils.getDataList(
            if (ll_location_card.visibility == View.VISIBLE) MMKVUtils.LOCATION_LIST_KEY
            else MMKVUtils.NAVI_LIST_KEY
        ).let {
            adapter.setData(it)
        }
    }

    private fun setDataToView(model: PoiInfoModel?) {
        model?.run {
            when (poiInfoType) {
                PoiInfoType.LOCATION -> {
                    tv_location_name.text = String.format(
                        "目标：%s",
                        name
                    )
                    tv_location_latlng.text = String.format(
                        "经纬度：%f , %f",
                        latLng?.longitude, latLng?.latitude
                    )
                    tv_location_latlng.tag = this
                }

                PoiInfoType.NAVI_START -> {
                    tv_navi_name_start.text = String.format(
                        "起点：%s",
                        name
                    )
                    tv_navi_name_start.tag = this
                }

                PoiInfoType.NAVI_END -> {
                    tv_navi_name_end.text = String.format(
                        "终点：%s",
                        name
                    )
                    tv_navi_name_end.tag = this
                }

                else -> {
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            iv_expand -> {
                startActivity(Intent(this, ExpandActivity::class.java))
            }

            ll_location_card -> {
                registerForActivityResult.launch(
                    Intent(
                        this@MainActivity,
                        PickMapPoiActivity::class.java
                    ).apply {
                        putExtra("from_tag", PoiInfoType.LOCATION)
                        if (tv_location_latlng.tag != null) {
                            putExtra("model", tv_location_latlng.tag as PoiInfoModel?)
                        }
                    }
                )
            }

            btn_start_location -> {
                if (tv_location_latlng.tag == null) {
                    Toast.makeText(this@MainActivity, "模拟位置不能为null", Toast.LENGTH_SHORT)
                        .show()
                    return
                }
                //启动模拟导航
                Utils.checkFloatWindow(this).let {
                    if (!it) {
                        WarnDialogUtils.setFloatWindowDialog(this@MainActivity)
                        return
                    }
                    val locationModel = tv_location_latlng.tag as PoiInfoModel?
                    val model = MockMessageModel(
                        locationModel = locationModel,
                        naviType = NaviType.LOCATION,
                        uid = locationModel?.uid ?: ""
                    )
                    val intent = Intent(this, MockLocationActivity::class.java)
                    intent.putExtra("model", model)
                    startActivity(intent)

                    MMKVUtils.saveLocationData(model)
                }
            }

            iv_change -> {
                if (ll_location_card.visibility == View.VISIBLE) {
                    ll_location_card.visibility = View.GONE
                    ll_navi_card.visibility = View.VISIBLE
                    collapsingToolbar.title = "模拟导航"
                } else {
                    ll_location_card.visibility = View.VISIBLE
                    ll_navi_card.visibility = View.GONE
                    collapsingToolbar.title = "模拟定位"
                }
                getHistoryData()
            }

            tv_navi_name_start -> {
                registerForActivityResult.launch(
                    Intent(
                        this@MainActivity,
                        PickMapPoiActivity::class.java
                    ).apply {
                        putExtra("from_tag", PoiInfoType.NAVI_START)
                        if (tv_navi_name_start.tag != null) {
                            putExtra("model", tv_navi_name_start.tag as PoiInfoModel?)
                        }
                    }
                )
            }

            tv_navi_name_end -> {
                registerForActivityResult.launch(
                    Intent(
                        this@MainActivity,
                        PickMapPoiActivity::class.java
                    ).apply {
                        putExtra("from_tag", PoiInfoType.NAVI_END)
                        if (tv_navi_name_end.tag != null) {
                            putExtra("model", tv_navi_name_end.tag as PoiInfoModel?)
                        }
                    }
                )
            }

            btn_start_navi -> {
                if (tv_navi_name_start.tag == null || tv_navi_name_end.tag == null) {
                    ToastUtils.showShort("模拟位置不能为null")
                    return
                }
                Utils.checkFloatWindow(this).let {
                    if (!it) {
                        WarnDialogUtils.setFloatWindowDialog(this@MainActivity)
                        return
                    }
                    val startNavi = tv_navi_name_start.tag as PoiInfoModel?
                    val endNavi = tv_navi_name_end.tag as PoiInfoModel?
                    if (startNavi == null || endNavi == null) {
                        ToastUtils.showShort("模拟位置不能为null")
                        return
                    }
                    viewModel.loading.value = true
                    SearchManager.INSTANCE.driverSearch(
                        startNavi.latLng!!,
                        endNavi.latLng!!,
                        radio_multi_route.isChecked
                    )
                }
            }

            iv_navi_setting -> {
                //导航设置
                NaviPopupWindow(this).apply {
                    show(iv_navi_setting)
                }
            }

            iv_app_update -> {
                viewModel.updateApp.value?.let {
                    val dialog: AlertDialog = AlertDialog.Builder(this)
                        .setTitle("有新的版本")
                        .setMessage(
                            if (!TextUtils.isEmpty(it.buildUpdateDescription)) it.buildUpdateDescription else "修复已知问题"
                        )
                        .setPositiveButton(
                            "确定"
                        ) { _: DialogInterface?, _: Int ->
                            Utils.openBrowser(
                                this,
                                it.downloadURL
                            ) {
                                viewModel.toast.value = "跳转失败,已将下载地址复制到剪切板!"
                                ClipboardUtils.copyText(it.downloadURL)
                            }
                        }
                        .setNegativeButton("取消", null)
                        .setNeutralButton(
                            "复制下载地址"
                        ) { _, _ -> ClipboardUtils.copyText(it.downloadURL) }
                        .create()
                    dialog.setCanceledOnTouchOutside(false)
                    dialog.show()
                }
            }

            else -> {
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.addCategory(Intent.CATEGORY_HOME)
            startActivity(intent)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

}