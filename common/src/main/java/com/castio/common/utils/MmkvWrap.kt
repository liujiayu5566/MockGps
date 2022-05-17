package com.castio.common.utils

import com.castio.common.Constants
import com.tencent.mmkv.MMKV

class MmkvWrap {
    private var mMmkv: MMKV = MMKV.defaultMMKV(MMKV.MULTI_PROCESS_MODE, Constants.MMKV_PACKAGE)

    companion object {
        val instance = MmkvWrapHolder.holder.mMmkv
    }

    private object MmkvWrapHolder {
        val holder = MmkvWrap()
    }


}