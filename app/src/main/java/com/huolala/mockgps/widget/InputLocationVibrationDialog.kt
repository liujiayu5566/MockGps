package com.huolala.mockgps.widget

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.ScreenUtils
import com.huolala.mockgps.R
import com.huolala.mockgps.databinding.DialogInputLocationVibrationBinding
import com.huolala.mockgps.utils.MMKVUtils

/**
 * @author jiayu.liu
 */
class InputLocationVibrationDialog(context: Context, val locationVibrationValue: Int) :
    Dialog(context) {

    init {
        DataBindingUtil.bind<DialogInputLocationVibrationBinding>(
            LayoutInflater.from(context)
                .inflate(R.layout.dialog_input_location_vibration, null, false)
        )?.let { dataBinding ->
            setContentView(dataBinding.root)
            window?.run {
                setBackgroundDrawableResource(R.color.transparent)
                val lp = attributes
                lp.width = ScreenUtils.getScreenWidth() - ConvertUtils.dp2px(20f)
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
                lp.y = -ConvertUtils.px2dp(50f)
                attributes = lp
            }
            dataBinding.seekbar.setProgress(locationVibrationValue.toFloat())

            dataBinding.clickListener = View.OnClickListener {
                MMKVUtils.setLocationVibrationValue(dataBinding.seekbar.progress)
                dismiss()
            }
        }
    }

}