package com.castiel.common.utils

import com.tencent.mmkv.MMKV

class MMKVWrap private constructor() {
    private var mMMKV: MMKV = MMKV.defaultMMKV()

    companion object {
        val instance = MMKVWrapHolder.holder.mMMKV
    }

    private object MMKVWrapHolder {
        val holder = MMKVWrap()
    }
}