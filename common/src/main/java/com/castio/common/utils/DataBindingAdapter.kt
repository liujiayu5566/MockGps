package com.castio.common.utils

import android.graphics.drawable.Drawable
import android.text.TextUtils
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.castio.common.R

object DataBindingAdapter {

    @JvmStatic
    @BindingAdapter("url", "error", "placeholder", "options", requireAll = false)
    fun imageViewUrl(
        imageView: AppCompatImageView,
        url: String,
        error: Drawable?,
        placeholder: Drawable?,
        options: RequestOptions?,
    ) {
        if (TextUtils.isEmpty(url)) return
        val glide = Glide.with(imageView)
            .load(url)
        error?.let {
            glide.error(it)
        }
        placeholder?.let {
            glide.placeholder(it)
        }
        options?.let {
            glide.apply(options)
        }
        glide.into(imageView)
    }


    @JvmStatic
    @BindingAdapter("resource", "error", "placeholder", "options", requireAll = false)
    fun imageViewResource(
        imageView: AppCompatImageView,
        resource: Drawable,
        error: Int?,
        placeholder: Int?,
        options: RequestOptions?,
    ) {
        val glide = Glide.with(imageView)
            .load(resource)
        error?.let {
            glide.error(it)
        }
        placeholder?.let {
            glide.placeholder(it)
        }
        options?.let {
            glide.apply(options)
        }
        glide.into(imageView)
    }
}
