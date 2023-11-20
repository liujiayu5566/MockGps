package com.huolala.mockgps.ui

import android.Manifest
import android.animation.ValueAnimator
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.ClickUtils
import com.castiel.common.base.BaseActivity
import com.castiel.common.base.BaseViewModel
import com.huolala.mockgps.R
import com.huolala.mockgps.databinding.ActivityGuideBinding
import com.huolala.mockgps.server.GpsAndFloatingService
import com.huolala.mockgps.utils.Utils
import kotlinx.android.synthetic.main.activity_guide.*
import java.lang.Exception

/**
 * @author jiayu.liu
 */
class GuideActivity : BaseActivity<ActivityGuideBinding, BaseViewModel>(), View.OnClickListener {
    companion object {
        private const val PERMISSION_REQUEST = 1001
    }

    private val permissions: Array<String> = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )


    override fun initViewModel(): Class<BaseViewModel> {
        return BaseViewModel::class.java
    }

    override fun getLayout(): Int {
        return R.layout.activity_guide
    }

    override fun initView() {
        ClickUtils.applySingleDebouncing(btn_go, this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            initPermission()
        }
    }

    override fun initData() {
        viewModel.toast.observe(this) {

        }
    }

    override fun initObserver() {
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initPermission(): Boolean {
        val needPermissions = arrayListOf<String>()
        permissions.map {
            if (ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED) {
                needPermissions.add(it)
            }
        }
        if (needPermissions.size == 0) {
            return true
        }

        requestPermissions(
            needPermissions.toArray(arrayOfNulls(needPermissions.size)),
            PERMISSION_REQUEST
        )
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST) {
            grantResults.map {
                if (it != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "需要权限", Toast.LENGTH_SHORT).show()
                    return
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            btn_go -> {
                //权限
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !initPermission()) {
                    return
                }
                //模拟导航设置
                if (!Utils.isAllowMockLocation(this)) {
                    AlertDialog.Builder(this)
                        .setTitle("警告")
                        .setMessage("将本应用设置为\"模拟位置信息应用\"，否则无法正常使用")
                        .setPositiveButton(
                            "设置"
                        ) { _, _ ->
                            try {
                                startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).apply {
                                    this.flags = FLAG_ACTIVITY_NEW_TASK
                                })
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }.setNegativeButton(
                            "取消"
                        ) { _, _ -> }
                        .show()
                    return
                }
                startService(Intent(this, GpsAndFloatingService::class.java))
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }

            else -> {}
        }
    }

}