package com.castiel.common.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.text.style.ReplacementSpan
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.blankj.utilcode.util.CollectionUtils
import com.blankj.utilcode.util.SizeUtils

data class Label(
    val labelText: String,
    val textSize: Float,
    val textColor: Int,
    val bgColor: Int,
    val borderWidth: Float,
    val borderColor: Int
)

/**
 * 携带标签的TextView
 * @author jiayu.liu
 */
class ContentLabelTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {
    private var content: String = ""

    init {
        text?.let {
            content = it.toString()
        }
    }


    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(text, type)
        text?.let {
            content = it.toString()
        } ?: run {
            content = ""
        }
    }


    fun text(
        content: String,
        interval: Float,
        labels: List<Label>?,
    ) {
        if (CollectionUtils.isEmpty(labels)) {
            text = content
            return
        }
        val spannable = SpannableStringBuilder(content)
        paint.measureText(content)
        val viewHeight = paint.fontMetrics.bottom - paint.fontMetrics.top
        val textHeight = paint.descent() - paint.ascent()
        for (label in labels!!) {
            // 添加空格分隔间距
            val spaceSpan = SpannableString(" ")
            spaceSpan.setSpan(
                SpaceSpan(SizeUtils.dp2px(interval)),
                0,
                spaceSpan.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.append(spaceSpan)

            // 创建自定义 Drawable
            val tagDrawable =
                TagDrawable(
                    label.labelText,
                    SizeUtils.sp2px(label.textSize).toFloat(),
                    label.textColor,
                    label.bgColor,
                    label.borderWidth,
                    label.borderColor
                )
            tagDrawable.setBounds(
                0,
                ((viewHeight - textHeight) / 2).toInt(),
                tagDrawable.intrinsicWidth,
                tagDrawable.intrinsicHeight
            )

            // 添加 ImageSpan
            val span = ImageSpan(tagDrawable, ImageSpan.ALIGN_CENTER)
            val labelSpannable = SpannableString(label.labelText)
            labelSpannable.setSpan(
                span,
                0,
                label.labelText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            spannable.append(labelSpannable)
        }

        text = spannable
    }

    /**
     * 标签间隔
     */
    class SpaceSpan(private val space: Int) : ReplacementSpan() {

        private val spacePx: Int
            get() = space

        override fun getSize(
            paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?
        ): Int {
            return spacePx // 返回空白区域的宽度（以 px 为单位）
        }

        override fun draw(
            canvas: Canvas,
            text: CharSequence?,
            start: Int,
            end: Int,
            x: Float,
            top: Int,
            y: Int,
            bottom: Int,
            paint: Paint
        ) {
            // 不需要绘制任何内容，只需留出空白区域
        }
    }

    /**
     * 标签view
     */
    class TagDrawable(
        private val text: String,
        private val textSize: Float,
        private val textColor: Int,
        private val bgColor: Int,
        private val borderWidth: Float = 0.5f,
        private val borderColor: Int
    ) : Drawable() {

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = this@TagDrawable.textSize
        }

        private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = this@TagDrawable.borderWidth
        }

        private val paddingHorizontal = SizeUtils.dp2px(4f) // 标签的内边距
        private val paddingVertical = SizeUtils.dp2px(2f) // 标签的内边距

        private val textWidth: Float
        private val textHeight: Float

        // 用于低版本兼容的 Path（避免重复创建）
        private val path = Path()

        init {
            textWidth = paint.measureText(text)
            textHeight = paint.descent() - paint.ascent()
        }

        override fun draw(canvas: Canvas) {
            val bounds = this@TagDrawable.bounds
            val cornerRadius = SizeUtils.dp2px(2f).toFloat()

            // 绘制背景
            paint.color = bgColor
            drawRoundRectCompat(canvas, bounds, cornerRadius, paint)

            // 绘制边框
            borderPaint.color = borderColor
            drawRoundRectCompat(canvas, bounds, cornerRadius, borderPaint)

            // 绘制文字
            paint.color = textColor
            val textX = bounds.left + paddingHorizontal
            val textY = bounds.centerY() - (paint.descent() + paint.ascent()) / 2
            canvas.drawText(text, textX.toFloat(), textY, paint)
        }

        private fun drawRoundRectCompat(
            canvas: Canvas,
            bounds: Rect,
            cornerRadius: Float,
            paint: Paint
        ) {
            val left = bounds.left.toFloat()
            val top = bounds.top.toFloat()
            val right = bounds.right.toFloat()
            val bottom = bounds.bottom.toFloat()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // 高版本直接绘制
                canvas.drawRoundRect(left, top, right, bottom, cornerRadius, cornerRadius, paint)
            } else {
                // 低版本使用 Path 绘制
                path.reset()
                path.addRoundRect(
                    RectF(left, top, right, bottom),
                    floatArrayOf(
                        cornerRadius,
                        cornerRadius,
                        cornerRadius,
                        cornerRadius,
                        cornerRadius,
                        cornerRadius,
                        cornerRadius,
                        cornerRadius
                    ),
                    Path.Direction.CW
                )
                canvas.drawPath(path, paint)
            }
        }

        override fun getIntrinsicWidth(): Int {
            return (textWidth + paddingHorizontal * 2).toInt()
        }

        override fun getIntrinsicHeight(): Int {
            return (textHeight + paddingVertical * 2).toInt()
        }

        override fun setAlpha(alpha: Int) {
            paint.alpha = alpha
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            paint.colorFilter = colorFilter
        }

        override fun getOpacity(): Int {
            return PixelFormat.TRANSLUCENT
        }
    }

}