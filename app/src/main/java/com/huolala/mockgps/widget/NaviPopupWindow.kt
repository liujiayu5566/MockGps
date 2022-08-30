package com.huolala.mockgps.widget

import android.content.Context
import android.widget.PopupWindow
import android.graphics.drawable.BitmapDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.blankj.utilcode.util.ConvertUtils
import com.huolala.mockgps.R
import com.huolala.mockgps.utils.MMKVUtils
import com.xw.repo.BubbleSeekBar


/**
 * @author jiayu.liu
 */
class NaviPopupWindow(context: Context) : PopupWindow(context) {
    private var tvSpeed: AppCompatTextView? = null
    private var seekBar: BubbleSeekBar? = null

    init {
        width = ConvertUtils.dp2px(200f)
        height = ViewGroup.LayoutParams.WRAP_CONTENT
        isFocusable = true
        //点击 back 键的时候，窗口会自动消失
        setBackgroundDrawable(BitmapDrawable())

        LayoutInflater.from(context).inflate(R.layout.popupwindow_navi_setting, null, false).apply {
            contentView = this
            tvSpeed = findViewById(R.id.tv_speed)
            seekBar = findViewById(R.id.seekbar)
            seekBar?.onProgressChangedListener = object : BubbleSeekBar.OnProgressChangedListener {
                override fun onProgressChanged(
                    bubbleSeekBar: BubbleSeekBar?,
                    progress: Int,
                    progressFloat: Float,
                    fromUser: Boolean
                ) {
                }

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
                    tvSpeed?.text = String.format("当前速度：$progress km/h")
                    MMKVUtils.setSpeed(progress)
                }

            }
        }
    }

    fun show(view: View, gravity: Int = Gravity.BOTTOM) {
        MMKVUtils.getSpeed().let {
            tvSpeed?.text = String.format("当前速度：$it km/h")
            seekBar?.setProgress(it.toFloat())
        }
        //设置窗口显示位置, 后面两个0 是表示偏移量，可以自由设置
        showAsDropDown(view, 0, 1, gravity)
        //更新窗口状态
        update()
    }

}