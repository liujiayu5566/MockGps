package com.castiel.common.http

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class RetrofitClient private constructor() {
    private val baseUrl = ""

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder().apply {
            val okHttpClient = OkHttpClient.Builder().apply {
                readTimeout(30, TimeUnit.SECONDS)
                readTimeout(30, TimeUnit.SECONDS)
                addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            }
            client(okHttpClient.build())
            addConverterFactory(GsonConverterFactory.create())
            baseUrl(baseUrl)
        }.build()
    }

    companion object {
        val INSTANCE: RetrofitClient by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            RetrofitClient()
        }
    }


    fun <T> getApi(t: Class<T>): T {
        return retrofit.create(t)
    }
}