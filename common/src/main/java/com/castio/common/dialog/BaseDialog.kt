package com.castio.common.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.Window
import android.view.WindowManager


open class BaseDialog(context: Context) : Dialog(context) {

    /**
     * 默认无背景 居中
     */
    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        if (window != null) {
            window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window!!.setGravity(Gravity.CENTER)
            window!!.decorView.setPadding(0, 0, 0, 0)
            val layoutParams = window!!.attributes
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
            window!!.setDimAmount(0f)
            layoutParams.horizontalMargin = 0f
            window!!.attributes = layoutParams
        }
    }

}