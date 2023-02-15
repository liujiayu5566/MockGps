package com.huolala.mockgps

import android.app.Application
import com.baidu.mapapi.CoordType
import com.baidu.mapapi.SDKInitializer
import com.castiel.common.AppManager
import com.castiel.common.BaseApp

/**
 * @author jiayu.liu
 */
class MyApp : BaseApp() {

    override fun onCreate() {
        super.onCreate()

        SDKInitializer.initialize(this)
        SDKInitializer.setCoordType(CoordType.GCJ02);
    }
}