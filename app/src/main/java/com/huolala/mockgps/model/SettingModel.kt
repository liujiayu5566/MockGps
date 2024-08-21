package com.huolala.mockgps.model

/**
 * 设置功能  开关状态
 * @author jiayu.liu
 */
data class SettingModel(
    var isLocationQuiver: Boolean = false,
    var isNaviRouteBinding: Boolean = true
)

/**
 * 设置功能 描述信息
 */
data class SettingMsgModel(
    var title: String = "",
    var msg: String = "",
    var isSwitch: Boolean = false
)