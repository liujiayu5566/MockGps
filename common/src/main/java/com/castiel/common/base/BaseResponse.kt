package com.castiel.common.base

data class BaseResponse<T>(
    val code: Int,
    val message: String,
    val data: T?
) {

}