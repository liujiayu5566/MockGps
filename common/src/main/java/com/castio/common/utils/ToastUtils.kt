package com.castio.common.utils

import android.content.Context
import android.os.Build
import android.widget.Toast

object ToastUtils {
    private var toast: Toast? = null

    fun showToast(
        context: Context?,
        content: String?
    ) {
        if (toast == null || Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            toast = Toast.makeText(
                context,
                content,
                Toast.LENGTH_SHORT
            )
            toast!!.show()
        } else {
            toast!!.setText(content)
            toast!!.show()
        }
    }

}