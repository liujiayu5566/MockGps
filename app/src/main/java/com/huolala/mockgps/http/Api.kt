package com.huolala.mockgps.http

import com.castiel.common.base.BaseResponse
import com.huolala.mockgps.model.AppUpdateModel
import okhttp3.RequestBody
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PartMap

/**
 * @author jiayu.liu
 */
interface Api {
    /**
     * 检测app版本
     */
    @POST("/apiv2/app/check")
    @FormUrlEncoded
    suspend fun checkAppUpdate(@FieldMap args: Map<String, String>): BaseResponse<AppUpdateModel>
}