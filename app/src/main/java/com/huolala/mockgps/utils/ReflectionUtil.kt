package com.huolala.mockgps.utils

import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField

object ReflectionUtil {

    /**
     * 修改私有成员变量的值
     */
    fun setPrivateField(instance: Any, propertyName: String, value: Any?) {
        try {
            val property = instance::class.declaredMemberProperties.find { it.name == propertyName }
            property?.let {
                it.isAccessible = true
                val javaField = it.javaField
                javaField?.isAccessible = true
                javaField?.set(instance, value)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 获取私有成员变量的值
     */
    fun getPrivateField(instance: Any, propertyName: String): Any? {
        return try {
            val property = instance::class.declaredMemberProperties.find { it.name == propertyName }
            property?.let {
                it.isAccessible = true
                val javaField = it.javaField
                javaField?.isAccessible = true
                javaField?.get(instance)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 调用私有方法
     */
    fun callPrivateMethod(instance: Any, methodName: String, vararg args: Any?): Any? {
        return try {
            val method = instance::class.declaredFunctions.find { it.name == methodName }
            method?.let {
                it.isAccessible = true
                it.call(instance, *args)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

