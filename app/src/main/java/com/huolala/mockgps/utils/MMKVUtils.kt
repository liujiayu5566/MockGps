package com.huolala.mockgps.utils

import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import com.google.gson.reflect.TypeToken
import com.huolala.mockgps.model.MockMessageModel
import java.lang.reflect.Type
import java.util.concurrent.CopyOnWriteArrayList


/**
 * @author jiayu.liu
 */
object MMKVUtils {
    private var defaultMMKV: MMKV = MMKV.defaultMMKV()
    val LOCATION_LIST_KEY: String = "LOCATION_LIST_KEY"
    val NAVI_LIST_KEY: String = "NAVI_LIST_KEY"
    private val MAX_SIZE: Int = 10

    fun saveLocationData(data: MockMessageModel) {
        getDataList(LOCATION_LIST_KEY)?.run {
            checkMockMessageModelIsExist(this, data)
            add(0, data)
            defaultMMKV.putString(
                LOCATION_LIST_KEY, Gson().toJson(
                    if (size > MAX_SIZE) {
                        subList(0, MAX_SIZE)
                    } else this
                )
            )
        } ?: kotlin.run {
            defaultMMKV.putString(LOCATION_LIST_KEY, Gson().toJson(arrayListOf(data)))
        }
    }

    fun saveNaviData(data: MockMessageModel) {
        getDataList(NAVI_LIST_KEY)?.run {
            checkMockMessageModelIsExist(this, data)
            add(0, data)

            defaultMMKV.putString(
                NAVI_LIST_KEY, Gson().toJson(
                    if (size > MAX_SIZE) {
                        subList(0, MAX_SIZE)
                    } else this
                )
            )
        } ?: kotlin.run {
            defaultMMKV.putString(NAVI_LIST_KEY, Gson().toJson(arrayListOf(data)))
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
}