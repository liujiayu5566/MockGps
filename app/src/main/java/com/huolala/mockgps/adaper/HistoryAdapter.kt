package com.huolala.mockgps.adaper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.huolala.mockgps.R
import com.huolala.mockgps.model.MockMessageModel
import com.huolala.mockgps.model.NaviType
import kotlinx.android.synthetic.main.item_history.view.*
import java.util.concurrent.CopyOnWriteArrayList


/**
 * @author jiayu.liu
 */
class HistoryAdapter : ListAdapter<MockMessageModel, HistoryAdapter.ViewHolder>(object :
    DiffUtil.ItemCallback<MockMessageModel>() {
    override fun areItemsTheSame(oldItem: MockMessageModel, newItem: MockMessageModel): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: MockMessageModel, newItem: MockMessageModel): Boolean {
        return oldItem.uid == newItem.uid
    }
}) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_history, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = getItem(position)
        when (model.naviType) {
            NaviType.LOCATION -> {
                holder.itemView.tv_poi_name.text = model.locationModel?.name ?: ""
                holder.itemView.tv_info.text = String.format(
                    "经纬度：%f , %f",
                    model.locationModel?.latLng?.longitude ?: 0f,
                    model.locationModel?.latLng?.latitude ?: 0f
                )
            }
            else -> {
                holder.itemView.tv_poi_name.text = String.format(
                    "%s -> %s",
                    model.startNavi?.name ?: "",
                    model.endNavi?.name ?: ""
                )
                holder.itemView.tv_info.text = String.format(
                    "经纬度：%f , %f -> %f , %f",
                    model.startNavi?.latLng?.longitude ?: 0f,
                    model.startNavi?.latLng?.latitude ?: 0f,
                    model.endNavi?.latLng?.longitude ?: 0f,
                    model.endNavi?.latLng?.latitude ?: 0f
                )
            }
        }
        mOnItemClickListener?.run {
            with(holder.itemView) {
                setOnClickListener {
                    onItemClick(it, model)
                }
                setOnLongClickListener {
                    onItemLongClick(it, model)
                    true
                }
            }
        }
    }

    fun setData(listData: CopyOnWriteArrayList<MockMessageModel>?) {
        submitList(listData)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    private var mOnItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(mOnItemClickListener: OnItemClickListener?) {
        this.mOnItemClickListener = mOnItemClickListener
    }

    interface OnItemClickListener {

        fun onItemClick(view: View?, model: MockMessageModel)

        fun onItemLongClick(view: View?, model: MockMessageModel)
    }
}