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
    const val LOCATION_QUIVER_KEY: String = "LOCATION_QUIVER_KEY"
    private const val MAX_SIZE: Int = 10

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

    fun getSettingModel(): SettingModel {
        val json = defaultMMKV.getString(LOCATION_QUIVER_KEY, "")
        if (json?.isNotEmpty() == true) {
            return GsonUtils.fromJson(json, SettingModel::class.java)
        }
        return SettingModel()
    }


    /**
     * 设置定位震动模式开关
     */
    fun setLocationQuiver(switch: Boolean) {
        try {
            val settingModel = SettingModel(isLocationQuiver = switch)
            defaultMMKV.putString(LOCATION_QUIVER_KEY, GsonUtils.toJson(settingModel))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 获取定位震动模式
     */
    fun isLocationQuiver(): Boolean {
        val json = defaultMMKV.getString(LOCATION_QUIVER_KEY, "")
        if (json?.isNotEmpty() == true) {
            return try {
                val settingModel = GsonUtils.fromJson(json, SettingModel::class.java)
                settingModel.isLocationQuiver
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
        return false
    }

}