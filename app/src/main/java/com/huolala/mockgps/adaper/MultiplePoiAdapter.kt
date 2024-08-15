package com.huolala.mockgps.adaper

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.huolala.mockgps.R
import com.huolala.mockgps.databinding.ItemNaviPoiCardBinding
import com.huolala.mockgps.model.PoiInfoModel

/**
 * @author jiayu.liu
 */
class MultiplePoiAdapter : RecyclerView.Adapter<MultiplePoiAdapter.ViewHolder>() {
    var list: MutableList<PoiInfoModel> = arrayListOf()
    var clickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_navi_poi_card, parent, false)
        return ViewHolder(DataBindingUtil.bind(view)!!)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = list[position]
        val str = when (position) {
            0 -> {
                viewHolder.binding.isWay = false
                "起点"
            }

            list.size - 1 -> {
                viewHolder.binding.isWay = false
                "终点"
            }

            else -> {
                viewHolder.binding.isWay = true
                "途经"
            }
        }

        viewHolder.binding.ivRemove.setOnClickListener {
            if (viewHolder.binding.getIsWay() == true) {
                list.removeAt(position)
                notifyDataSetChanged()
            } else {
                clickListener?.onItemClick(viewHolder.binding.root, position)
            }
        }

        viewHolder.binding.poiName = "$str：${item.name}"


        clickListener?.let { listener ->
            viewHolder.binding.root.setOnClickListener {
                listener.onItemClick(viewHolder.binding.root, position)
            }
            viewHolder.binding.ivMove.setOnTouchListener { _, event ->
                if (event?.action == MotionEvent.ACTION_DOWN) {
                    listener.onItemMove()
                    true
                } else {
                    false
                }
            }

        }
    }

    fun submitList(list: List<PoiInfoModel>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    fun currentList(): MutableList<PoiInfoModel> {
        return list
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(val binding: ItemNaviPoiCardBinding) : RecyclerView.ViewHolder(binding.root)


    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)

        fun onItemMove()
    }
}
