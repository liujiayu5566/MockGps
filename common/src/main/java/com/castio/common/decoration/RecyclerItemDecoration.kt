package com.castio.common.decoration

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager
import com.castio.common.utils.DensityUtils

/**
 * recyclerview 增加间隔
 */
class RecyclerItemDecoration : RecyclerView.ItemDecoration {

    private var distance = 0
    private var indent = 0
    private var bottom = 0
    private var isFirst = true

    constructor(
        context: Context?,
        distance: Float,
        indent: Float
    ) {
        this.distance = DensityUtils.dp2px(context, distance)
        this.indent = DensityUtils.dp2px(context, indent)
    }

    //GridLayoutManager 或者  StaggeredGridLayoutManager
    constructor(
        context: Context?,
        bottom: Float
    ) {
        this.bottom = DensityUtils.dp2px(context, bottom)
        isFirst = false
    }

    //头部偏移
    fun setFirst(firstTop: Boolean) {
        isFirst = firstTop
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val layoutManager = parent.layoutManager
        val position = parent.getChildAdapterPosition(view)
        val adapter = parent.adapter
        when (layoutManager) {
            is GridLayoutManager,
            is StaggeredGridLayoutManager
            -> {
                outRect.set(0, if (position == 0 && isFirst) bottom else 0, 0, bottom)
            }
            is LinearLayoutManager -> {
                val orientation = layoutManager.orientation
                if (orientation == LinearLayoutManager.VERTICAL) {
                    outRect.set(
                        0,
                        if (position == 0 && isFirst) distance + indent else 0,
                        0,
                        if (adapter != null && position == adapter.itemCount - 1) distance + indent else distance
                    )
                } else {
                    outRect.set(
                        if (position == 0) distance + indent else 0,
                        0,
                        if (adapter != null && position == adapter.itemCount - 1) distance + indent else distance,
                        0
                    )
                }
            }
            is ChipsLayoutManager -> {
                outRect.set(0, if (position == 0 && isFirst) bottom else 0, 10, bottom)
            }
        }
    }

}