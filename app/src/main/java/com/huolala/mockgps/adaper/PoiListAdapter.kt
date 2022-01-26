package com.huolala.mockgps.adaper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.baidu.mapapi.search.core.PoiInfo
import com.baidu.mapapi.search.sug.SuggestionResult
import com.huolala.mockgps.R
import kotlinx.android.synthetic.main.item_poiinfo.view.*

/**
 * @author jiayu.liu
 */
class PoiListAdapter : RecyclerView.Adapter<PoiListAdapter.ViewHolder>() {
    private var poiList: ArrayList<SuggestionResult.SuggestionInfo> = arrayListOf()
    private var mOnItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_poiinfo, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val poiInfo = poiList[position]
        holder.itemView.tv_item_poi_name.text =
            String.format("city: ${poiInfo.city}    name: ${poiInfo.key}")
        holder.itemView.tv_item_poi_address.text = poiInfo.address
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener {
                mOnItemClickListener!!.onItemClick(poiInfo)
            }
        }
    }

    fun setData(list: MutableList<SuggestionResult.SuggestionInfo>?) {
        poiList.clear()
        if (list != null) {
            poiList.addAll(list)
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return poiList.size
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    fun setOnItemClickListener(mOnItemClickListener: OnItemClickListener?) {
        this.mOnItemClickListener = mOnItemClickListener
    }

    interface OnItemClickListener {
        fun onItemClick(poiInfo: SuggestionResult.SuggestionInfo)
    }


}