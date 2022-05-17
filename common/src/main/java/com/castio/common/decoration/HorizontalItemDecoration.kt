package com.castio.common.decoration

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.RecyclerView
import com.castio.common.R
import com.castio.common.utils.DensityUtils

/**
 * 横向recycleView分割线LinearLayoutManager
 * @author jiayu.liu
 */
class HorizontalItemDecoration constructor(
    context: Context,
    top: Float,
    bottom: Float
) : RecyclerView.ItemDecoration() {
    private val mDivider: Drawable =
        context.resources.getDrawable(R.drawable.shape_item_line_horizontall, null)
    private val top: Int = DensityUtils.dp2px(context, top)
    private val bottom: Int = DensityUtils.dp2px(context, bottom)

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val top = parent.paddingTop
        val bottom = parent.height - parent.paddingBottom
        val childCount = parent.childCount
        //最后一个item不画分割线
        for (i in 0 until childCount - 1) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams

            val left = child.left + params.leftMargin
            val right = left + mDivider.intrinsicWidth
            mDivider.setBounds(left, top + this.top, right, bottom + this.bottom)
            mDivider.draw(c)
        }
    }

}