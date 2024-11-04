package com.huolala.mockgps.widget

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.ScreenUtils
import com.huolala.mockgps.R
import com.huolala.mockgps.databinding.DialogInputLocationVibrationBinding
import com.huolala.mockgps.utils.MMKVUtils
import com.xw.repo.BubbleSeekBar

/**
 * @author jiayu.liu
 */
class InputLocationVibrationDialog(
    context: Context,
    private val locationVibrationValue: Int,
    private val locationFrequencyValue: Int
) : Dialog(context) {
    var mDismissListener: LocationVibrationDismissListener? = null

    init {
        val dataBinding = DataBindingUtil.bind<DialogInputLocationVibrationBinding>(
            LayoutInflater.from(context)
                .inflate(R.layout.dialog_input_location_vibration, null, false)
        )
        dataBinding?.let {
            setContentView(dataBinding.root)
            window?.run {
                setBackgroundDrawableResource(R.color.transparent)
                val lp = attributes
                lp.width = ScreenUtils.getScreenWidth() - ConvertUtils.dp2px(20f)
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
                lp.gravity = Gravity.CENTER
                attributes = lp
            }
            dataBinding.seekbar.setProgress(locationVibrationValue.toFloat())
            dataBinding.seekbarFrequency.setProgress(locationFrequencyValue.toFloat())

            dataBinding.radiusValue =
                "设置半径范围(1m~20m)，当前范围: ${locationVibrationValue}m"
            dataBinding.freqValue = "设置频率(1s~60s)，当前频率: ${locationFrequencyValue}s"

            dataBinding.seekbar.onProgressChangedListener = object :
                OnProgressChangedListener() {
                override fun onProgressChanged(
                    bubbleSeekBar: BubbleSeekBar?,
                    progress: Int,
                    progressFloat: Float,
                    fromUser: Boolean
                ) {
                    dataBinding.radiusValue = "设置半径范围(1m~20m)，当前范围: ${progress}m"
                }
            }

            dataBinding.seekbarFrequency.onProgressChangedListener = object :
                OnProgressChangedListener() {
                override fun onProgressChanged(
                    bubbleSeekBar: BubbleSeekBar?,
                    progress: Int,
                    progressFloat: Float,
                    fromUser: Boolean
                ) {
                    dataBinding.freqValue = "设置频率(1s~60s)，当前频率: ${progress}s"
                }
            }


            dataBinding.clickListener = View.OnClickListener {
                saveValue(dataBinding)
                dismiss()
            }

            setOnDismissListener {
                saveValue(dataBinding)
                mDismissListener?.onDismiss()
            }
        }


    }

    private fun saveValue(dataBinding: DialogInputLocationVibrationBinding) {
        MMKVUtils.setLocationVibrationValue(dataBinding.seekbar.progress)
        MMKVUtils.setLocationFrequencyValue(dataBinding.seekbarFrequency.progress)
    }

    abstract class OnProgressChangedListener : BubbleSeekBar.OnProgressChangedListener {

        override fun getProgressOnActionUp(
            bubbleSeekBar: BubbleSeekBar?,
            progress: Int,
            progressFloat: Float
        ) {
        }

        override fun getProgressOnFinally(
            bubbleSeekBar: BubbleSeekBar?,
            progress: Int,
            progressFloat: Float,
            fromUser: Boolean
        ) {
        }
    }

    interface LocationVibrationDismissListener {
        fun onDismiss()
    }

}