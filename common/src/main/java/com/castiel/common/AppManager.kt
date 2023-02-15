package com.castiel.common

import android.app.Application
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.LogUtils
import com.tencent.bugly.crashreport.CrashReport
import com.tencent.mmkv.MMKV

class AppManager {
    private var context: Application? = null

    companion object {
        val instance: AppManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            AppManager()
        }
    }

    fun init(context: Application?) {
        if (context == null)
            throw NullPointerException("Application isn't null")
        if (this.context != null) {
            throw Exception("AppManager Already initialized")
        }
        this.context = context
        MMKV.initialize(context)
        if (BuildConfig.DEBUG) {
            ARouter.openLog()
            ARouter.openDebug()
        }
        ARouter.init(context)
        CrashReport.initCrashReport(context, "8d25df392e", BuildConfig.DEBUG)
        LogUtils.getConfig().isLogSwitch = BuildConfig.DEBUG
        LogUtils.getConfig().globalTag = "castiel"
    }
}