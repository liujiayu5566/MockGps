package com.huolala.mockgps.widget

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.blankj.utilcode.util.ClickUtils
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.ToastUtils
import com.huolala.mockgps.R
import com.huolala.mockgps.databinding.DialogInputSpeedBinding
import com.huolala.mockgps.utils.MMKVUtils
import kotlinx.android.synthetic.main.dialog_input_speed.view.*

/**
 * @author jiayu.liu
 */
class InputSpeed(context: Context) : Dialog(context) {
    init {
        DataBindingUtil.bind<DialogInputSpeedBinding>(
            LayoutInflater.from(context)
                .inflate(R.layout.dialog_input_speed, null, false)
        )?.let { it ->
            setContentView(it.root)
            window?.run {
                setBackgroundDrawableResource(R.color.transparent);
                val lp = attributes;
                lp.width = ScreenUtils.getScreenWidth() - ConvertUtils.dp2px(20f)
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
                attributes = lp
            }
            ClickUtils.applySingleDebouncing(it.btnConfirm) { view ->
                try {
                    val speed = it.editSpeed.text.toString().toInt()
                    if (speed <= 0 || speed > Int.MAX_VALUE) {
                        ToastUtils.showShort("请输入有效数值(大于0)")
                    }
                    MMKVUtils.setSpeed(speed)
                } catch (e: Exception) {
                    e.printStackTrace()
                    ToastUtils.showShort("存储异常，设置失败")
                }
                dismiss()
            }
        }
    }
}