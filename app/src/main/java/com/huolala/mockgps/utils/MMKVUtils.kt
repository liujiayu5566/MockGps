package com.huolala.mockgps.utils

import com.blankj.utilcode.util.GsonUtils
import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import com.google.gson.reflect.TypeToken
import com.huolala.mockgps.model.MockMessageModel
import com.huolala.mockgps.model.SettingModel
import java.lang.reflect.Type
import java.util.concurrent.CopyOnWriteArrayList


/**
 * @author jiayu.liu
 */
object MMKVUtils {
    private var defaultMMKV: MMKV = MMKV.defaultMMKV()
    const val LOCATION_LIST_KEY: String = "LOCATION_LIST_KEY"
    const val MULTIPLE_NAVI_LIST_KEY: String = "Multiple_NAVI_LIST_KEY"
    const val NAVI_SPEED_KEY: String = "NAVI_SPEED_KEY"
    private const val MAX_SIZE: Int = 10

    //设置配置
    const val KEY_SETTING: String = "KEY_SETTING"

    //模拟定位震动功能开关
    const val KEY_LOCATION_VIBRATION: String = "KEY_LOCATION_VIBRATION"

    //模拟定位震动功能范围，默认：10m
    const val KEY_LOCATION_VIBRATION_VALUE: String = "KEY_LOCATION_VIBRATION_VALUE"

    //模拟定位震动功能频率，默认：10s
    const val KEY_LOCATION_FREQUENCY_VALUE: String = "key_location_frequency_value"

    //模拟导航绑路开关
    const val KEY_NAVI_ROUTE_BINDING: String = "KEY_NAVI_ROUTE_BINDING"


    fun saveLocationData(data: MockMessageModel) {
        saveDataList(LOCATION_LIST_KEY, data)
    }

    fun saveNaviData(data: MockMessageModel) {
        saveDataList(MULTIPLE_NAVI_LIST_KEY, data)
    }

    private fun saveDataList(key: String, data: MockMessageModel) {
        getDataList(key)?.run {
            checkMockMessageModelIsExist(this, data)
            add(0, data)

            defaultMMKV.putString(
                key, Gson().toJson(
                    if (size > MAX_SIZE) {
                        subList(0, MAX_SIZE)
                    } else this
                )
            )
        } ?: kotlin.run {
            defaultMMKV.putString(key, Gson().toJson(arrayListOf(data)))
        }
    }

    fun getDataList(key: String): CopyOnWriteArrayList<MockMessageModel>? {
        val naviStr = defaultMMKV.getString(key, "")
        if (naviStr?.isNotEmpty() == true) {
            val type: Type = object : TypeToken<CopyOnWriteArrayList<MockMessageModel?>?>() {}.type
            return Gson().fromJson(naviStr, type)
        }
        return null
    }

    fun clearDataList(key: String) {
        defaultMMKV.remove(key)
    }

    private fun checkMockMessageModelIsExist(
        dataList: CopyOnWriteArrayList<MockMessageModel>,
        data: MockMessageModel
    ) {
        for (model in dataList) {
            if (data.uid.equals(model.uid)) {
                dataList.remove(model)
                return
            }
        }
    }

    /**
     * 设置导航速度
     */
    fun setSpeed(speed: Int) {
        defaultMMKV.putInt(NAVI_SPEED_KEY, speed)
    }

    /**
     * 获取导航速度
     */
    fun getSpeed(): Int {
        return defaultMMKV.getInt(NAVI_SPEED_KEY, 60)
    }

    /**
     * 引导页展示标识
     */
    fun setGuideVisible(visible: Boolean) {
        defaultMMKV.putBoolean("isGuideVisible", visible)
    }

    /**
     * 获取引导页展示标识
     */
    fun isGuideVisible(): Boolean {
        return defaultMMKV.getBoolean("isGuideVisible", false)
    }


    /**
     * 导航按钮引导展示标识
     */
    fun setNaviGuideVisible(visible: Boolean) {
        defaultMMKV.putBoolean("isNaviGuideVisible", visible)
    }

    /**
     * 获取导航按钮引导展示标识
     */
    fun isNaviGuideVisible(): Boolean {
        return defaultMMKV.getBoolean("isNaviGuideVisible", false)
    }

    fun getSettingModel(): SettingModel {
        val json = defaultMMKV.getString(KEY_SETTING, "")
        if (json?.isNotEmpty() == true) {
            return GsonUtils.fromJson(json, SettingModel::class.java)
        }
        return SettingModel()
    }

    fun saveSettingConfig(key: String, switch: Boolean) {
        try {
            val settingModel = getSettingModel()
            when (key) {
                KEY_LOCATION_VIBRATION -> settingModel.isLocationQuiver = switch
                KEY_NAVI_ROUTE_BINDING -> settingModel.isNaviRouteBinding = switch
                else -> {}
            }
            defaultMMKV.putString(KEY_SETTING, GsonUtils.toJson(settingModel))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    /**
     * 获取模拟定位震动模式开关
     */
    fun isLocationVibrationSwitch(): Boolean {
        return try {
            val settingModel = getSettingModel()
            settingModel.isLocationQuiver
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 设置模拟定位震动模式参数
     */
    fun setLocationVibrationValue(value: Int) {
        defaultMMKV.putInt(KEY_LOCATION_VIBRATION_VALUE, value)
    }

    /**
     * 获取模拟定位震动模式参数
     */
    fun getLocationVibrationValue(): Int {
        return defaultMMKV.getInt(KEY_LOCATION_VIBRATION_VALUE, 10)
    }

    fun setLocationFrequencyValue(value: Int) {
        defaultMMKV.putInt(KEY_LOCATION_FREQUENCY_VALUE, value)
    }

    fun getLocationFrequencyValue(): Int {
        return defaultMMKV.getInt(KEY_LOCATION_FREQUENCY_VALUE, 1)
    }

    /**
     * 获取模拟导航绑路优化开关
     */
    fun isNaviRouteBindingSwitch(): Boolean {
        return try {
            val settingModel = getSettingModel()
            settingModel.isNaviRouteBinding
        } catch (e: Exception) {
            e.printStackTrace()
            true
        }
    }

}