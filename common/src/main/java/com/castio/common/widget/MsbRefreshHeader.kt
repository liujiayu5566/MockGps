package com.castio.common.widget

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.castio.common.R
import com.scwang.smart.refresh.layout.api.RefreshHeader
import com.scwang.smart.refresh.layout.api.RefreshKernel
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.constant.RefreshState
import com.scwang.smart.refresh.layout.constant.SpinnerStyle

class MsbRefreshHeader(context: Context) : LinearLayout(context), RefreshHeader {


    init {
//        setBackgroundColor(ContextCompat.getColor(context, R.color.white))
        gravity = Gravity.CENTER_HORIZONTAL;
        addView(LayoutInflater.from(context).inflate(R.layout.layout_header_view, this, false))
    }

    override fun getSpinnerStyle(): SpinnerStyle {
        return SpinnerStyle.Translate
    }

    override fun onFinish(refreshLayout: RefreshLayout, success: Boolean): Int {
        return 0
    }

    override fun onInitialized(kernel: RefreshKernel, height: Int, maxDragHeight: Int) {

    }

    override fun onHorizontalDrag(percentX: Float, offsetX: Int, offsetMax: Int) {

    }

    override fun onReleased(refreshLayout: RefreshLayout, height: Int, maxDragHeight: Int) {

    }

    override fun getView(): View {
        return this
    }

    override fun setPrimaryColors(vararg colors: Int) {

    }

    override fun onStartAnimator(refreshLayout: RefreshLayout, height: Int, maxDragHeight: Int) {

    }

    override fun onStateChanged(
        refreshLayout: RefreshLayout,
        oldState: RefreshState,
        newState: RefreshState
    ) {

    }

    override fun onMoving(
        isDragging: Boolean,
        percent: Float,
        offset: Int,
        height: Int,
        maxDragHeight: Int
    ) {

    }

    override fun isSupportHorizontalDrag(): Boolean {
        return false
    }
}