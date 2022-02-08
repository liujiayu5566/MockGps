package com.huolala.mockgps

import android.app.Application
import com.baidu.mapapi.CoordType
import com.baidu.mapapi.SDKInitializer
import com.tencent.mmkv.MMKV

/**
 * @author jiayu.liu
 */
class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        SDKInitializer.initialize(this)
        MMKV.initialize(this)
    }
}