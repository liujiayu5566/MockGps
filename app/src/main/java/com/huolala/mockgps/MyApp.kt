package com.huolala.mockgps

import android.app.Application
import android.content.IntentFilter
import com.baidu.location.LocationClient
import com.baidu.mapapi.CoordType
import com.baidu.mapapi.SDKInitializer
import com.blankj.utilcode.util.Utils
import com.castiel.common.AppManager
import com.castiel.common.BaseApp

/**
 * @author jiayu.liu
 */
class MyApp : BaseApp() {
    private lateinit var mMockReceiver: MockReceiver

    override fun onCreate() {
        super.onCreate()

        Utils.init(this)
        SDKInitializer.setAgreePrivacy(this, true)
        LocationClient.setAgreePrivacy(true);
        try {
            SDKInitializer.initialize(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        SDKInitializer.setCoordType(CoordType.GCJ02);
        initReceiver()
    }


    private fun initReceiver() {
        mMockReceiver = MockReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.huolala.mockgps.navi")
        registerReceiver(mMockReceiver, intentFilter)
//        mMockReceiver is missing RECEIVER_EXPORTED or RECEIVER_NOT_EXPORTED flag for unprotected broadcasts registered for com.huolala.mockgps.navi
    }
}