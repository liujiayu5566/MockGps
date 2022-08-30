package com.castiel.common.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ClickUtils

abstract class BaseListAdapter<T, VH : RecyclerView.ViewHolder>(callback: DiffUtil.ItemCallback<T>) :
    ListAdapter<T, VH>(callback) {

    var clickListener: OnItemClickListener<T>? = null
    var clickLongListener: OnItemLongClickListener<T>? = null

    override fun onBindViewHolder(holder: VH, position: Int) {
        clickListener?.run {
            ClickUtils.applySingleDebouncing(holder.itemView) {
                onItemClick(it, getItem(position), position)
            }
        }
        clickLongListener?.run {
            holder.itemView.setOnLongClickListener { v ->
                onItemLongClick(
                    v,
                    getItem(position),
                    position
                )
                true
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