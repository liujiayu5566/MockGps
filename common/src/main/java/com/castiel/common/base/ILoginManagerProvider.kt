package com.castiel.common.base

import com.alibaba.android.arouter.facade.template.IProvider

interface ILoginManagerProvider : IProvider {
    fun logout()

    fun isLogin(): Boolean

    fun goLogin()
}