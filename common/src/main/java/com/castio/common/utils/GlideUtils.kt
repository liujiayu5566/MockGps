package com.castio.common.utils

import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

import com.bumptech.glide.request.RequestOptions


/**
 * @author jiayu.liu
 */
object GlideUtils {

    /**
     * 设置圆形图片
     */
    fun circleImage(): RequestOptions {
        return RequestOptions.bitmapTransform(CircleCrop())
    }


    /**
     * 设置圆角图片
     */
    fun rounderImage(roundingRadius: Int = 0): RequestOptions {
        return RequestOptions.bitmapTransform(RoundedCorners(roundingRadius))
    }

}