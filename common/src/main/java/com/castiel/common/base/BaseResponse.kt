package com.castiel.common.base

data class BaseResponse<T>(
    val errorCode: Int,
    val errorMsg: String,
    val data: T?
) {

}