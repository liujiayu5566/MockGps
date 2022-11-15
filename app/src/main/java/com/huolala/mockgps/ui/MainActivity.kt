package com.huolala.mockgps.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ClickUtils
import com.blankj.utilcode.util.ConvertUtils
import com.castiel.common.base.BaseActivity
import com.castiel.common.base.BaseViewModel
import com.google.android.material.appbar.AppBarLayout
import com.huolala.mockgps.R
import com.huolala.mockgps.adaper.HistoryAdapter
import com.huolala.mockgps.adaper.SimpleDividerDecoration
import com.huolala.mockgps.databinding.ActivityMainBinding
import com.huolala.mockgps.model.MockMessageModel
import com.huolala.mockgps.model.NaviType
import com.huolala.mockgps.model.PoiInfoModel
import com.huolala.mockgps.model.PoiInfoType
import com.huolala.mockgps.utils.DialogUtils
import com.huolala.mockgps.utils.MMKVUtils
import com.huolala.mockgps.utils.Utils
import com.huolala.mockgps.widget.NaviPopupWindow
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.recycler
import kotlinx.android.synthetic.main.layout_location_card.*
import kotlinx.android.synthetic.main.layout_location_card.tv_location_latlng
import kotlinx.android.synthetic.main.layout_navi_card.*
import kotlinx.android.synthetic.main.layout_navi_card.tv_navi_name_end
import kotlinx.android.synthetic.main.layout_navi_card.tv_navi_name_start
import java.lang.Exception
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * @author jiayu.liu
 */
class MainActivity : BaseActivity<ActivityMainBinding, BaseViewModel>(), View.OnClickListener {
    private var topMarginOffset: Int = 0
    private var topMargin: Int = 0
    private lateinit var adapter: HistoryAdapter

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
                println("$topMarginOffsetValue , $topMarginValue, $scale")
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
    }


    override fun initViewModel(): Class<BaseViewModel> {
        return BaseViewModel::class.java
    }

    override fun getLayout(): Int {
        return R.layout.activity_main
    }

    override fun initData() {
    }

    override fun initObserver() {
    }

    override fun onResume() {
        super.onResume()
        //获取历史数据
        getHistoryData()
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
                    Toast.makeText(this@MainActivity, "模拟位置不能为null", Toast.LENGTH_SHORT).show()
                    return
                }
                //启动模拟导航
                Utils.checkFloatWindow(this).let {
                    if (!it) {
                        DialogUtils.setFloatWindowDialog(this@MainActivity)
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
                    Toast.makeText(this@MainActivity, "模拟位置不能为null", Toast.LENGTH_SHORT).show()
                    return
                }
                Utils.checkFloatWindow(this).let {
                    if (!it) {
                        DialogUtils.setFloatWindowDialog(this@MainActivity)
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
                    val intent = Intent(this, MockLocationActivity::class.java)
                    intent.putExtra("model", model)
                    startActivity(intent)

                    MMKVUtils.saveNaviData(model)
                }
            }
            iv_navi_setting -> {
                //导航设置
                NaviPopupWindow(this).apply {
                    show(iv_navi_setting)
                }
            }
            else -> {
            }
        }
    }


}