package com.castiel.common.utils

import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import com.blankj.utilcode.util.ClickUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

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

    @JvmStatic
    @BindingAdapter("clickListener", requireAll = false)
    fun viewClick(view: View, clickListener: View.OnClickListener?) {
        if (clickListener == null) {
            throw IllegalArgumentException("clickListener is null")
        }
        ClickUtils.applySingleDebouncing(view, clickListener)
    }
}
