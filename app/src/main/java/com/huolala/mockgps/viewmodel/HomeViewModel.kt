package com.huolala.mockgps.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.AppUtils
import com.castiel.common.base.BaseViewModel
import com.castiel.common.http.RetrofitClient
import com.huolala.mockgps.http.Api
import com.huolala.mockgps.model.AppUpdateModel

/**
 * @author jiayu.liu
 */
class HomeViewModel : BaseViewModel() {
    private val _updateApp = MutableLiveData<AppUpdateModel>()
    val updateApp: LiveData<AppUpdateModel> = _updateApp

    /**
     * 检测是否需要升级
     */
    fun checkAppUpdate() {
        lauch(
            {
                RetrofitClient.INSTANCE.getApi(Api::class.java)
                    .checkAppUpdate(
                        mapOf(
                            "_api_key" to "24078410e0636c07449112c2a380ae56",
                            "appKey" to "b4159c34808b3b4e3f75bdb52f4868ab"
                        )
                    )
            },
            { model ->
                val appVersionCode = AppUtils.getAppVersionCode()

                if (appVersionCode == -1) {
                    toast.value = "获取当前app版本号失败，无法检测当前版本是否是最新版"
                    return@lauch
                }
                //获取版本号
                model?.buildVersionNo?.let { buildVersionNo ->
                    //云端版本号大于当前版本
                    if (buildVersionNo.toInt() > appVersionCode) {
                        model.appURl?.let {
                            //下载接口不未null  传递到view层
                            this._updateApp.value = model
                        }
                    }
                }
            },
        )
    }

}