package com.huolala.mockgps.utils

import android.os.Handler
import android.os.Looper

class HandlerUtils private constructor() : Handler(Looper.getMainLooper()) {

    companion object {
        val INSTANCE: HandlerUtils by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            HandlerUtils()
        }
    }

}