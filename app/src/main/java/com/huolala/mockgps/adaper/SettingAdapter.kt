package com.huolala.mockgps.adaper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.huolala.mockgps.R
import com.huolala.mockgps.databinding.ItemSettingBinding
import com.huolala.mockgps.model.SettingMsgModel

/**
 * @author jiayu.liu
 */
class SettingAdapter : RecyclerView.Adapter<SettingAdapter.ViewHolder>() {
    private val list: MutableList<SettingMsgModel> = mutableListOf()
    var listener: OnItemListener? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ViewHolder {
        val dataBinding = DataBindingUtil.inflate<ItemSettingBinding>(
            LayoutInflater.from(viewGroup.context),
            R.layout.item_setting,
            viewGroup,
            false
        )
        return ViewHolder(dataBinding!!)
    }

    override fun getItemCount(): Int {
        return this.list.size
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val model = list[position]
        viewHolder.dataBinding.isSwitch = model.isSwitch
        viewHolder.dataBinding.title = model.title
        viewHolder.dataBinding.msg = model.msg
        viewHolder.dataBinding.listener = View.OnClickListener { listener?.onSettingClick(model) }

        viewHolder.dataBinding.swSwitch.setOnCheckedChangeListener { _, isChecked ->
            model.isSwitch = isChecked
            listener?.onItemSwitch(
                viewHolder.dataBinding,
                model,
                isChecked
            )
        }
    }

    fun submitList(list: List<SettingMsgModel>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    class ViewHolder(val dataBinding: ItemSettingBinding) :
        RecyclerView.ViewHolder(dataBinding.root)

    interface OnItemListener {
        fun onItemSwitch(
            dataBinding: ItemSettingBinding?,
            model: SettingMsgModel,
            isChecked: Boolean
        )

        fun onSettingClick(model: SettingMsgModel)
    }
}