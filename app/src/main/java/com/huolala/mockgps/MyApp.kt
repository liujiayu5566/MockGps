package com.huolala.mockgps

import android.app.Application
import com.baidu.mapapi.CoordType
import com.baidu.mapapi.SDKInitializer
import com.castiel.common.AppManager

/**
 * @author jiayu.liu
 */
class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        AppManager.instance.init(this)
        SDKInitializer.initialize(this)
        SDKInitializer.setCoordType(CoordType.GCJ02);
    }
}