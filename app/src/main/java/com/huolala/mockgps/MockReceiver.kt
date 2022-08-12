package com.huolala.mockgps

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.text.TextUtils
import android.widget.Toast
import com.baidu.mapapi.model.LatLng
import com.castio.common.utils.ToastUtils
import com.huolala.mockgps.model.MockMessageModel
import com.huolala.mockgps.model.PoiInfoModel
import com.huolala.mockgps.server.GpsAndFloatingService
import com.huolala.mockgps.ui.MockLocationActivity
import com.huolala.mockgps.utils.MMKVUtils
import com.huolala.mockgps.utils.Utils

/**
 * @author jiayu.liu
 */
class MockReceiver : BroadcastReceiver() {

    var MOCK_ACTION = "com.huolala.mockgps.navi"

    override fun onReceive(context: Context?, intent: Intent) {
        if (context == null) {
            return
        }
        val action = intent.action
        if (MOCK_ACTION != action) {
            return
        }
        val start = intent.getStringExtra("start")
        val end = intent.getStringExtra("end")
        ToastUtils.showToast(context, "mockGps接收到模拟定位广播-> start=$start , end=$end")
        Utils.checkFloatWindow(context).let {
            if (!it) {
                ToastUtils.showToast(context, "悬浮窗权限未开启，请返回app开启权限！")
                return
            }

            if (TextUtils.isEmpty(start) || TextUtils.isEmpty(end)) {
                return
            }
            var startLatLng: LatLng? = null
            var endLatLng: LatLng? = null

            try {
                start!!.split(",").apply {
                    startLatLng = LatLng(get(1).toDouble(), get(0).toDouble())
                }
                end!!.split(",").apply {
                    endLatLng = LatLng(get(1).toDouble(), get(0).toDouble())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (startLatLng == null || endLatLng == null) {
                return
            }

            val model = MockMessageModel(
                startNavi = PoiInfoModel().apply {
                    fromTag = 1
                    uid = ""
                    name = ""
                    latLng = startLatLng
                },
                endNavi = PoiInfoModel().apply {
                    fromTag = 2
                    uid = ""
                    name = ""
                    latLng = endLatLng
                },
                fromTag = 1,
                speed = MMKVUtils.getSpeed(),
                uid = ""
            )
            startMockServer(context, model)
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
        context.startService(Intent(context, GpsAndFloatingService::class.java).apply {
            parcelable?.let {
                putExtras(
                    Bundle().apply {
                        putParcelable("info", it)
                    })
            }
        })
    }


}