package com.castio.common

import android.app.Application
import android.content.Context
import com.castio.common.widget.MsbRefreshFooter
import com.castio.common.widget.MsbRefreshHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshFooter
import com.scwang.smart.refresh.layout.api.RefreshHeader
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.DefaultRefreshFooterCreator
import com.scwang.smart.refresh.layout.listener.DefaultRefreshHeaderCreator


open class BaseApp : Application() {

    override fun onCreate() {
        super.onCreate()
        SmartRefreshLayout.setDefaultRefreshHeaderCreator(object : DefaultRefreshHeaderCreator {
            override fun createRefreshHeader(
                context: Context,
                layout: RefreshLayout
            ): RefreshHeader {
                return MsbRefreshHeader(context)
            }

        })
        //设置全局的Footer构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreator(object : DefaultRefreshFooterCreator {
            override fun createRefreshFooter(
                context: Context,
                layout: RefreshLayout
            ): RefreshFooter {
                return MsbRefreshFooter(context)
            }

        })
        AppManager.instance.init(this)
    }
}