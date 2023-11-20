package com.huolala.mockgps.utils

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import java.lang.Exception

/**
 * @author jiayu.liu
 */
object WarnDialogUtils {

    /**
     * 悬浮窗提示
     */
    fun setFloatWindowDialog(context: Context?) {
        if (context == null) {
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return
        }
        AlertDialog.Builder(context)
            .setTitle("警告")
            .setMessage("需要开启悬浮窗，否则容易导致App被系统回收")
            .setPositiveButton(
                "开启"
            ) { _, _ ->
                try {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    context.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.setNegativeButton(
                "取消"
            ) { _, _ -> }
            .show()
    }

}