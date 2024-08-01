package com.huolala.mockgps

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.route.DrivingRouteLine
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.huolala.mockgps.manager.FloatingViewManger
import com.huolala.mockgps.manager.SearchManager
import com.huolala.mockgps.manager.utils.MapConvertUtils
import com.huolala.mockgps.manager.utils.MapDrawUtils
import com.huolala.mockgps.model.MockMessageModel
import com.huolala.mockgps.model.NaviType
import com.huolala.mockgps.model.PoiInfoModel
import com.huolala.mockgps.model.PoiInfoType
import com.huolala.mockgps.server.GpsService
import com.huolala.mockgps.utils.LocationUtils
import com.huolala.mockgps.utils.MMKVUtils
import com.huolala.mockgps.utils.Utils
import kotlinx.android.synthetic.main.layout_navi_card.radio_multi_route

/**
 * @author jiayu.liu
 *
 *  Intent intentBroadcast = new Intent();
 *  intentBroadcast.setAction("com.huolala.mockgps.navi");
 *  intentBroadcast.putExtra("start", "116.419431,40.028795");
 *  intentBroadcast.putExtra("end", "116.409816,40.05139");
 *  //bd09   gps84   gcj02
 *  intentBroadcast.putExtra("type", "gcj02");
 *  sendBroadcast(intentBroadcast);
 *
 */
class MockReceiver : BroadcastReceiver() {

    companion object {
        var MOCK_ACTION = "com.huolala.mockgps.navi"
    }

    override fun onReceive(context: Context?, intent: Intent) {
        if (context == null) {
            return
        }
        val action = intent.action
        if (MOCK_ACTION != action) {
            return
        }
        val event = intent.getStringExtra("event")
        when (event) {
            "stopNavi" -> {
                FloatingViewManger.INSTANCE.stopMockFromReceiver()
                return
            }
            else -> {}
        }
        val start = intent.getStringExtra("start")
        val end = intent.getStringExtra("end")
        val type = intent.getStringExtra("type") ?: LocationUtils.gcj02
        LogUtils.dTag("mock", "mockGps接收到模拟定位广播-> start=$start , end=$end , type=$type")
        Utils.checkFloatWindow(context).let {
            if (!it) {
                ToastUtils.showShort("悬浮窗权限未开启，请返回app开启权限！")
                return
            }

            if (TextUtils.isEmpty(start) || TextUtils.isEmpty(end)) {
                return
            }
            var startLatLng: LatLng? = null
            var endLatLng: LatLng? = null

            try {
                start!!.split(",").apply {
                    startLatLng = when (type) {
                        LocationUtils.bd09 -> {
                            val bd09ToGcj02 =
                                LocationUtils.bd09ToGcj02(get(0).toDouble(), get(1).toDouble())
                            LatLng(bd09ToGcj02[1], bd09ToGcj02[0])
                        }

                        LocationUtils.gps84 -> {
                            val wgs84ToGcj02 =
                                LocationUtils.wgs84ToGcj02(get(0).toDouble(), get(1).toDouble())
                            LatLng(wgs84ToGcj02[1], wgs84ToGcj02[0])
                        }

                        else -> {
                            LatLng(get(1).toDouble(), get(0).toDouble())
                        }
                    }
                }
                end!!.split(",").apply {
                    endLatLng = when (type) {
                        LocationUtils.bd09 -> {
                            val wgs84ToGcj02 =
                                LocationUtils.bd09ToGcj02(get(0).toDouble(), get(1).toDouble())
                            LatLng(wgs84ToGcj02[1], wgs84ToGcj02[0])
                        }

                        LocationUtils.gps84 -> {
                            val wgs84ToGcj02 =
                                LocationUtils.wgs84ToGcj02(get(0).toDouble(), get(1).toDouble())
                            LatLng(wgs84ToGcj02[1], wgs84ToGcj02[0])
                        }

                        else -> {
                            LatLng(get(1).toDouble(), get(0).toDouble())
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (startLatLng == null || endLatLng == null) {
                return
            }

            val model = MockMessageModel(
                startNavi = PoiInfoModel().apply {
                    poiInfoType = PoiInfoType.NAVI_START
                    uid = ""
                    name = ""
                    latLng = startLatLng
                },
                endNavi = PoiInfoModel().apply {
                    poiInfoType = PoiInfoType.NAVI_END
                    uid = ""
                    name = ""
                    latLng = endLatLng
                },
                naviType = NaviType.NAVI,
                speed = MMKVUtils.getSpeed(),
                uid = ""
            )

            SearchManager.INSTANCE.addSearchManagerListener(object : SearchManager.SearchManagerListener {
                override fun onDrivingRouteResultLines(routeLines: List<DrivingRouteLine>?) {
                    if (routeLines?.isEmpty() != false) {
                        ToastUtils.showShort("路线规划数据获取失败,请检测网络or数据是否正确!")
                        return
                    }
                    //使用第一条路线
                    SearchManager.INSTANCE.selectDriverLine(routeLines[0])
                    startMockServer(context, model)
                    SearchManager.INSTANCE.removeSearchManagerListener(this)
                }
            })

            SearchManager.INSTANCE.driverSearch(
                startLatLng,
                endLatLng,
                true
            )
        }
    }


    private fun startMockServer(context: Context, parcelable: MockMessageModel?) {
        //判断  为null先启动服务  悬浮窗需要
        parcelable?.run {
            if (!Utils.isAllowMockLocation(context)) {
                Toast.makeText(
                    context,
                    "将本应用设置为\"模拟位置信息应用\"，否则无法正常使用",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
        }
        //启动服务  定位以及悬浮窗
        context.startService(Intent(context, GpsService::class.java).apply {
            parcelable?.let {
                putExtras(
                    Bundle().apply {
                        putParcelable("info", it)
                    })
            }
        })
    }


}