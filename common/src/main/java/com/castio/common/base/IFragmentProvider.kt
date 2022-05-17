package com.castio.common.base

import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.template.IProvider

interface IFragmentProvider : IProvider {
    fun getFragment(): Fragment
}