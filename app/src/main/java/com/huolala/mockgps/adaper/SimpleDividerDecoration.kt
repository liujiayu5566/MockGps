package com.huolala.mockgps.adaper

import androidx.recyclerview.widget.RecyclerView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.blankj.utilcode.util.ConvertUtils
import com.huolala.mockgps.R


/**
 * @author jiayu.liu
 */
class SimpleDividerDecoration(
    context: Context,
    color: Int = R.color.grey,
    height: Float = 1f
) :
    ItemDecoration() {
    private val dividerHeight: Int
    private val dividerPaint: Paint = Paint()

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.bottom = dividerHeight
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        val childCount = parent.childCount
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight
        for (i in 0 until childCount - 1) {
            val view: View = parent.getChildAt(i)
            val top: Int = view.bottom
            val bottom: Int = view.bottom + dividerHeight
            c.drawRect(
                left.toFloat() + dividerHeight * 10,
                top.toFloat(),
                right.toFloat(),
                bottom.toFloat(),
                dividerPaint
            )
        }
    }

    init {
        dividerPaint.color = context.resources.getColor(color)
        dividerHeight = ConvertUtils.dp2px(height)
    }
}