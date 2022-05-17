package com.castio.common.decoration

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.castio.common.R
import com.castio.common.utils.DensityUtils

/**
 * 竖向recycleView分割线LinearLayoutManager
 * @author jiayu.liu
 */
class VerticalItemDecoration constructor(
    context: Context,
    left: Float,
    right: Float
) : ItemDecoration() {
    private val mDivider: Drawable =
        context.resources.getDrawable(R.drawable.shape_item_line_verticall, null)
    private val left: Int = DensityUtils.dp2px(context, left)
    private val right: Int = DensityUtils.dp2px(context, right)

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight
        val childCount = parent.childCount
        //最后一个item不画分割线
        for (i in 0 until childCount - 1) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin
            val bottom = top + mDivider.intrinsicHeight
            mDivider.setBounds(left + this.left, top, right - this.right, bottom)
            mDivider.draw(c)
        }
    }

}