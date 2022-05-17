package com.castio.common.base

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ClickUtils
import com.blankj.utilcode.util.LogUtils

abstract class BaseListAdapter<T, VH : RecyclerView.ViewHolder> :
    ListAdapter<T, VH>(DiffCallback<T>()) {

    var clickListener: OnItemClickListener<T>? = null
    var clickLongListener: OnItemLongClickListener<T>? = null

    companion object {
        class DiffCallback<T> : DiffUtil.ItemCallback<T>() {
            override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
                return oldItem === newItem
            }
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        clickListener?.run {
            ClickUtils.applyGlobalDebouncing(holder.itemView) {
                onItemClick(it, getItem(position), position)
            }
        }
        clickLongListener?.run {
            ClickUtils.applyGlobalDebouncing(holder.itemView) {
                onItemLongClick(it, getItem(position), position)
            }
        }
        onBindViewHolderModel(holder, position)
    }

    abstract fun onBindViewHolderModel(holder: VH, position: Int)

    interface OnItemClickListener<T> {
        fun onItemClick(view: View?, t: T, position: Int)
    }

    interface OnItemLongClickListener<T> {
        fun onItemLongClick(view: View?, t: T, position: Int)
    }
}