package com.huolala.mockgps.widget

import android.app.Service
import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageButton
import com.blankj.utilcode.util.Utils
import java.lang.ref.WeakReference

/**
 * @author jiayu.liu
 */
class LongClickImageButton : AppCompatImageButton {
    var listener: LongClickListener? = null
    private var vibratorManager: VibratorManager? = null
    private var vibrator: Vibrator? = null
    private var isLong = false

    companion object {
        const val LONG_CLICK_CODE = 1001
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val handler: LongClick = LongClick(this)

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            vibratorManager =
                Utils.getApp().getSystemService(Service.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        } else {
            vibrator = Utils.getApp().getSystemService(Service.VIBRATOR_SERVICE) as Vibrator
        }
        setOnLongClickListener {
            isLong = true
            triggerVibrator()
            handler.sendEmptyMessage(LONG_CLICK_CODE)
            return@setOnLongClickListener true
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_UP -> {
                if (isLong) {
                    handler.removeCallbacksAndMessages(null)
                    listener?.touchLongClickFinish()
                    isLong = false
                }
            }

            else -> {}
        }
        return super.onTouchEvent(event)
    }

    fun onLongClickCallBack() {
        if (handler.delayMillis > 150) {
            triggerVibrator()
        }
        listener?.touchLongClickEvent()
    }

    private fun triggerVibrator() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            vibratorManager?.vibrate(
                CombinedVibration.createParallel(
                    VibrationEffect.createOneShot(
                        30,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            )
        } else {
            vibrator?.vibrate(30)
        }
    }

    fun setDelayMillis(delayMillis: Long) {
        handler.delayMillis = delayMillis
    }

    class LongClick(view: LongClickImageButton) : Handler(Looper.getMainLooper()) {
        private var reference: WeakReference<LongClickImageButton>? = null
        var delayMillis = 150L

        init {
            reference = WeakReference<LongClickImageButton>(view)
        }


        override fun handleMessage(msg: Message) {
            reference?.get()?.run {
                onLongClickCallBack()
            }
            //150ms后重复
            sendEmptyMessageDelayed(LONG_CLICK_CODE, delayMillis)
        }
    }

    interface LongClickListener {
        fun touchLongClickEvent()

        fun touchLongClickFinish()
    }


}