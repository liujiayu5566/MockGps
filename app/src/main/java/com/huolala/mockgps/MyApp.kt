package com.huolala.mockgps

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentFilter
import android.os.Build
import com.baidu.location.LocationClient
import com.baidu.mapapi.CoordType
import com.baidu.mapapi.SDKInitializer
import com.blankj.utilcode.util.Utils
import com.castiel.common.BaseApp
import me.weishu.reflection.Reflection
import java.lang.reflect.Field


/**
 * @author jiayu.liu
 */
class MyApp : BaseApp() {
    private lateinit var mMockReceiver: MockReceiver
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        Reflection.unseal(base)
        reflectionValueAnimator()
    }

    @SuppressLint("SoonBlockedPrivateApi")
    private fun reflectionValueAnimator() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!ValueAnimator.areAnimatorsEnabled()) {
                val field: Field = ValueAnimator::class.java.getDeclaredField("sDurationScale")
                field.isAccessible = true
                field.set(null, 1)
            }
        }
    }


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