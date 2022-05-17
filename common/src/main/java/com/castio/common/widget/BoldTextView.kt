package com.castio.common.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.castio.common.R

/**
 * 粗体
 */
class BoldTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {
    private var mStriking = 0.5f
    override fun onDraw(canvas: Canvas) {
        //获取当前控件的画笔
        val paint = paint
        //设置画笔的描边宽度值
        paint.strokeWidth = mStriking
        paint.style = Paint.Style.FILL_AND_STROKE
        super.onDraw(canvas)
    }

    init {
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.BoldTextView, defStyleAttr, 0)
        val striking = typedArray.getInt(R.styleable.BoldTextView_striking, 0).toFloat()
        if (striking > 0.0f) {
            this.mStriking = striking
        }
        typedArray.recycle()
    }
}