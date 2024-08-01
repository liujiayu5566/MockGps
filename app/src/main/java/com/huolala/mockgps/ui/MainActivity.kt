package com.huolala.mockgps.ui

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
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
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.route.DrivingRouteLine
import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.ToastUtils
import com.castiel.common.base.BaseActivity
import com.google.android.material.appbar.AppBarLayout
import com.huolala.mockgps.R
import com.huolala.mockgps.adaper.MainAdapter
import com.huolala.mockgps.adaper.MultiplePoiAdapter
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
import com.huolala.mockgps.widget.GuideView
import com.huolala.mockgps.widget.MapSelectDialog
import com.huolala.mockgps.widget.NaviPopupWindow
import kotlin.math.abs
import kotlin.math.roundToInt


/**
 * @author jiayu.liu
 */
class MainActivity : BaseActivity<ActivityMainBinding, HomeViewModel>(), View.OnClickListener {
    private var topMarginOffset: Int = 0
    private var topMargin: Int = 0
    private lateinit var adapter: MainAdapter
    private var mMapSelectDialog: MapSelectDialog? = null
    private var locationAlwaysView: View? = null
    private lateinit var poiAdapter: MultiplePoiAdapter
    private val mSearchManagerListener = object : SearchManager.SearchManagerListener {
        override fun onDrivingRouteResultLines(routeLines: List<DrivingRouteLine>?) {
            viewModel.loading.value = false
            if (routeLines?.isEmpty() != false) {
                ToastUtils.showShort("路线规划数据获取失败,请检测网络or数据是否正确!")
                return
            }
            val currentList = poiAdapter.currentList()
            val startNavi = currentList[0]
            val endNavi = currentList[currentList.size - 1]
            val wayList = if (currentList.size > 2) {
                currentList.toMutableList().subList(1, currentList.size - 1)
            } else null

            val model = MockMessageModel(
                startNavi = startNavi,
                endNavi = endNavi,
                wayNaviList = wayList,
                naviType = NaviType.NAVI,
                speed = MMKVUtils.getSpeed(),
                uid = (startNavi.uid ?: "") + (endNavi.uid ?: "") + (wayList?.map { it.uid }
                    ?.joinToString("") ?: "")
            )

            if (routeLines.size == 1) {
                goToMockLocation(routeLines[0], model)
            } else {
                mMapSelectDialog = MapSelectDialog(
                    this@MainActivity,
                    routeLines,
                    startNavi.latLng,
                    endNavi.latLng,
                    wayList
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
                    val index = getIntExtra("index", -1)
                    val parcelableExtra = this.getParcelableExtra<PoiInfoModel>("poiInfo")
                    setDataToView(index, parcelableExtra)
                }
            }
        }

    override fun initView() {
        dataBinding.title = "模拟定位"
        dataBinding.clickListener = this
        topMarginOffset = -ConvertUtils.dp2px(50f)
        topMargin = ConvertUtils.dp2px(15f)

        poiAdapter = MultiplePoiAdapter().apply {
            setData(
                arrayListOf(
                    PoiInfoModel(),
                    PoiInfoModel(),
                )
            )

            clickListener = object : MultiplePoiAdapter.OnItemClickListener {
                override fun onItemClick(view: View, position: Int) {
                    registerForActivityResult.launch(
                        Intent(
                            this@MainActivity,
                            PickMapPoiActivity::class.java
                        ).apply {
                            putExtra("from_tag", PoiInfoType.DEFAULT)
                            putExtra("index", position)
                            putExtra("model", poiAdapter.currentList()[position])
                        }
                    )
                }

                override fun onItemMove() {
                    adapter.onItemMove()
                }
            }
        }
        adapter = MainAdapter(poiAdapter).apply {
            setOnItemClickListener(object : MainAdapter.OnItemClickListener {
                override fun onItemClick(view: View?, model: MockMessageModel) {
                    when (model.naviType) {
                        NaviType.LOCATION -> {
                            setDataToView(model = model.locationModel)
                            with(this@MainActivity.dataBinding) {
                                appBarLayout.setExpanded(true,true)
                                recycler.scrollToPosition(0)
                            }
                        }

                        NaviType.NAVI -> {
                            val list = arrayListOf<PoiInfoModel>()
                            list.add(model.startNavi ?: PoiInfoModel())
                            model.wayNaviList?.let {
                                list.addAll(it)
                            }
                            list.add(model.endNavi ?: PoiInfoModel())
                            poiAdapter.setData(list)

                            with(this@MainActivity.dataBinding) {
                                appBarLayout.setExpanded(true,true)
                                recycler.scrollToPosition(0)
                            }

                        }

                        else -> {
                        }
                    }
                }

                override fun onClick(v: View?) {
                    this@MainActivity.onClick(v)
                }

            })
        }
        dataBinding.recycler.adapter = adapter.apply {
            adapter.submitList(arrayListOf<MockMessageModel?>(null))
        }
        dataBinding.recycler.itemAnimator = null
        dataBinding.recycler.layoutManager = LinearLayoutManager(this)
        dataBinding.recycler.addItemDecoration(
            SimpleDividerDecoration(
                this,
                R.color.transparent,
                10f
            )
        )

        //引导
        if (!MMKVUtils.isGuideVisible()) {
            dataBinding.ivExpand.post {
                val rootView = dataBinding.root as ViewGroup
                val guideView = GuideView(this)
                guideView.setGuideView(dataBinding.ivExpand)

                rootView.addView(
                    guideView,
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                )
            }
        }

        dataBinding.appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            appBarLayout?.run {
                val scale = abs(verticalOffset * 1.0f / appBarLayout.totalScrollRange)
                val params =
                    dataBinding.recycler.layoutParams as ViewGroup.MarginLayoutParams
                val topMarginOffsetValue = (topMarginOffset * (1 - scale)).roundToInt()
                val topMarginValue = (topMargin * scale).roundToInt()
                params.topMargin = topMarginOffsetValue + topMarginValue
                dataBinding.recycler.layoutParams = params
            }
        })
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
        dataBinding.recycler.post { getHistoryData() }
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
                            intent.data = Uri.parse("package:$packageName")
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
            if (adapter.dataBinding.includeLocationCard.llLocationCard.visibility == View.VISIBLE) MMKVUtils.LOCATION_LIST_KEY
            else MMKVUtils.MULTIPLE_NAVI_LIST_KEY
        )?.let {
            it.add(0, null)
            adapter.submitList(it)
        }
    }

    private fun setDataToView(index: Int = -1, model: PoiInfoModel?) {
        model?.let {
            when (it.poiInfoType) {
                PoiInfoType.LOCATION -> {
                    adapter.dataBinding.includeLocationCard.tvLocationName.text = String.format(
                        "目标：%s",
                        it.name
                    )
                    adapter.dataBinding.includeLocationCard.tvLocationLatlng.text = String.format(
                        "经纬度：%f , %f",
                        it.latLng?.longitude, it.latLng?.latitude
                    )
                    adapter.dataBinding.includeLocationCard.tvLocationLatlng.tag = it
                }

                else -> {
                    poiAdapter.setData(
                        poiAdapter.currentList().apply {
                            set(index, it)
                        }
                    )
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            dataBinding.ivExpand -> {
                startActivity(Intent(this, ExpandActivity::class.java))
            }

            adapter.dataBinding.includeLocationCard.llLocationCard -> {
                registerForActivityResult.launch(
                    Intent(
                        this@MainActivity,
                        PickMapPoiActivity::class.java
                    ).apply {
                        putExtra("from_tag", PoiInfoType.LOCATION)
                        if (adapter.dataBinding.includeLocationCard.tvLocationLatlng.tag != null) {
                            putExtra(
                                "model",
                                adapter.dataBinding.includeLocationCard.tvLocationLatlng.tag as PoiInfoModel?
                            )
                        }
                    }
                )
            }

            adapter.dataBinding.includeLocationCard.btnStartLocation -> {
                if (adapter.dataBinding.includeLocationCard.tvLocationLatlng.tag == null) {
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
                    val locationModel =
                        adapter.dataBinding.includeLocationCard.tvLocationLatlng.tag as PoiInfoModel?
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

            dataBinding.ivChange -> {
                if (adapter.dataBinding.includeLocationCard.llLocationCard.visibility == View.VISIBLE) {
                    adapter.dataBinding.includeLocationCard.llLocationCard.visibility = View.GONE
                    adapter.dataBinding.includeNaviCard.llNaviCard.visibility = View.VISIBLE
                    dataBinding.title = "模拟导航"
                    dataBinding.isNavi = true
                } else {
                    adapter.dataBinding.includeLocationCard.llLocationCard.visibility = View.VISIBLE
                    adapter.dataBinding.includeNaviCard.llNaviCard.visibility = View.GONE
                    dataBinding.title = "模拟定位"
                    dataBinding.isNavi = false
                }
                dataBinding.appBarLayout.setExpanded(true,true)
                dataBinding.recycler.scrollToPosition(0)
                getHistoryData()
            }

            dataBinding.ivNaviAdd -> {
                poiAdapter.currentList().apply {
                    if (size >= 5) {
                        ToastUtils.showShort("最多只能添加5个")
                        return
                    }
                    this.add(size - 1, PoiInfoModel())
                    poiAdapter.setData(this)
                }

            }

            adapter.dataBinding.includeNaviCard.btnStartNavi -> {
                val currentList = poiAdapter.currentList()
                for (poiInfoModel in currentList) {
                    if (poiInfoModel.latLng == null) {
                        ToastUtils.showShort("模拟位置不能为null")
                        return
                    }
                }
                Utils.checkFloatWindow(this).let {
                    if (!it) {
                        WarnDialogUtils.setFloatWindowDialog(this@MainActivity)
                        return
                    }
                    val startNavi = currentList[0]
                    val endNavi = currentList[currentList.size - 1]
                    val wayList = arrayListOf<LatLng>()
                    if (currentList.size > 2) {
                        for ((index, poiInfoModel) in currentList.withIndex()) {
                            if (index == 0 || index == currentList.size - 1) {
                                continue
                            }
                            wayList.add(poiInfoModel.latLng!!)
                        }
                    }
                    viewModel.loading.value = true
                    SearchManager.INSTANCE.driverSearch(
                        startNavi.latLng!!,
                        endNavi.latLng!!,
                        adapter.dataBinding.includeNaviCard.radioMultiRoute.isChecked,
                        if (wayList.isNotEmpty()) wayList else null
                    )
                }
            }

            adapter.dataBinding.includeNaviCard.ivNaviSetting -> {
                //导航设置
                NaviPopupWindow(this).apply {
                    show(adapter.dataBinding.includeNaviCard.ivNaviSetting)
                }
            }

            dataBinding.ivAppUpdate -> {
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