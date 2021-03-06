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
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.huolala.mockgps.R
import com.huolala.mockgps.adaper.HistoryAdapter
import com.huolala.mockgps.adaper.SimpleDividerDecoration
import com.huolala.mockgps.model.MockMessageModel
import com.huolala.mockgps.model.PoiInfoModel
import com.huolala.mockgps.utils.DensityUtils
import com.huolala.mockgps.utils.MMKVUtils
import com.huolala.mockgps.widget.NaviPopupWindow
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.recycler
import kotlinx.android.synthetic.main.activity_pick.*
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
class MainActivity : AppCompatActivity(), View.OnClickListener {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        topMarginOffset = -DensityUtils.dp2px(this@MainActivity, 50f)
        topMargin = DensityUtils.dp2px(this@MainActivity, 15f)

        initView()
    }

    override fun onResume() {
        super.onResume()
        //??????????????????
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

    private fun initView() {
        adapter = HistoryAdapter()
        recycler.adapter = adapter
        recycler.itemAnimator = null
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.addItemDecoration(SimpleDividerDecoration(this, R.color.transparent, 10f))

        adapter.setOnItemClickListener(object : HistoryAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, model: MockMessageModel) {
                when (model.fromTag) {
                    0 -> {
                        setDataToView(model.locationModel)
                    }
                    1 -> {
                        setDataToView(model.startNavi)
                        setDataToView(model.endNavi)
                    }
                    else -> {}
                }
            }

            override fun onItemLongClick(view: View?, model: MockMessageModel) {
//                TODO("Not yet implemented")
            }

        })

        iv_change.setOnClickListener(this)
        iv_setting.setOnClickListener(this)
        //location
        ll_location_card.setOnClickListener(this)
        btn_start_location.setOnClickListener(this)
        //navi
        tv_navi_name_start.setOnClickListener(this)
        tv_navi_name_end.setOnClickListener(this)
        btn_start_navi.setOnClickListener(this)
        iv_navi_setting.setOnClickListener(this)

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
    }


    private fun setDataToView(model: PoiInfoModel?) {
        model?.run {
            when (fromTag) {
                0 -> {
                    tv_location_name.text = String.format(
                        "?????????%s",
                        name
                    )
                    tv_location_latlng.text = String.format(
                        "????????????%f , %f",
                        latLng?.longitude, latLng?.latitude
                    )
                    tv_location_latlng.tag = this
                }
                1 -> {
                    tv_navi_name_start.text = String.format(
                        "?????????%s",
                        name
                    )
                    tv_navi_name_start.tag = this
                }
                2 -> {
                    tv_navi_name_end.text = String.format(
                        "?????????%s",
                        name
                    )
                    tv_navi_name_end.tag = this
                }
                else -> {}
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            iv_setting -> {
                Toast.makeText(this, "????????????", Toast.LENGTH_SHORT).show()
            }
            ll_location_card -> {
                registerForActivityResult.launch(
                    Intent(
                        this@MainActivity,
                        PickMapPoiActivity::class.java
                    ).apply { putExtra("from_tag", 0) }
                )
            }
            btn_start_location -> {
                if (tv_location_latlng.tag == null) {
                    Toast.makeText(this@MainActivity, "?????????????????????null", Toast.LENGTH_SHORT).show()
                    return
                }
                //??????????????????
                checkFloatWindow().let {
                    if (!it) return
                    val locationModel = tv_location_latlng.tag as PoiInfoModel?
                    val model = MockMessageModel(
                        locationModel = locationModel,
                        fromTag = 0,
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
                    collapsingToolbar.title = "????????????"
                } else {
                    ll_location_card.visibility = View.VISIBLE
                    ll_navi_card.visibility = View.GONE
                    collapsingToolbar.title = "????????????"
                }
                getHistoryData()
            }
            tv_navi_name_start -> {
                registerForActivityResult.launch(
                    Intent(
                        this@MainActivity,
                        PickMapPoiActivity::class.java
                    ).apply { putExtra("from_tag", 1) }
                )
            }
            tv_navi_name_end -> {
                registerForActivityResult.launch(
                    Intent(
                        this@MainActivity,
                        PickMapPoiActivity::class.java
                    ).apply { putExtra("from_tag", 2) }
                )
            }
            btn_start_navi -> {
                if (tv_navi_name_start.tag == null || tv_navi_name_end.tag == null) {
                    Toast.makeText(this@MainActivity, "?????????????????????null", Toast.LENGTH_SHORT).show()
                    return
                }
                checkFloatWindow().let {
                    if (!it) return
                    val startNavi = tv_navi_name_start.tag as PoiInfoModel?
                    val endNavi = tv_navi_name_end.tag as PoiInfoModel?
                    val model = MockMessageModel(
                        startNavi = startNavi,
                        endNavi = endNavi,
                        fromTag = 1,
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
                //????????????
                NaviPopupWindow(this).apply {
                    show(iv_navi_setting)
                }
            }
            else -> {}
        }
    }


    private fun checkFloatWindow(): Boolean {
        //?????????????????????
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(applicationContext)) {
                //??????Activity???????????????
                setFloatWindowDialog()
                return false
            }
        }
        return true
    }

    //??????????????????????????????
    @RequiresApi(Build.VERSION_CODES.M)
    private fun setFloatWindowDialog() {
        AlertDialog.Builder(this)
            .setTitle("??????")
            .setMessage("??????????????????????????????????????????App???????????????")
            .setPositiveButton(
                "??????"
            ) { _, _ ->
                try {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.setNegativeButton(
                "??????"
            ) { _, _ -> }
            .show()
    }

}