package com.castiel.common

import android.app.Application
import com.castiel.common.widget.MsbRefreshFooter
import com.castiel.common.widget.MsbRefreshHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout


open class BaseApp : Application() {

    override fun onCreate() {
        super.onCreate()
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, layout ->
            MsbRefreshHeader(
                context
            )
        }
        //设置全局的Footer构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreator { context, layout ->
            MsbRefreshFooter(
                context
            )
        }
        AppManager.instance.init(this)
    }
}