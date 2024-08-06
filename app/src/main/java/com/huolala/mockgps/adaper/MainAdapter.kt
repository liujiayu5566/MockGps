package com.huolala.mockgps.adaper

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.huolala.mockgps.R
import com.huolala.mockgps.databinding.LayoutMainCardHeaderBinding
import com.huolala.mockgps.model.MockMessageModel
import com.huolala.mockgps.model.NaviType
import com.huolala.mockgps.model.PoiInfoModel
import com.huolala.mockgps.model.PoiInfoType
import kotlinx.android.synthetic.main.item_history.view.tv_info
import kotlinx.android.synthetic.main.item_history.view.tv_poi_name
import java.util.ArrayList
import java.util.Collections


/**
 * @author jiayu.liu
 */
@Suppress("UNREACHABLE_CODE")
class MainAdapter(val headerAdapter: MultiplePoiAdapter) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    lateinit var dataBinding: LayoutMainCardHeaderBinding
    private var list: ArrayList<MockMessageModel?> = arrayListOf()
    private var headerItemMove = false
    private val HEADER_VIEW_TYPE = 0
    private val ITEM_VIEW_TYPE = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == HEADER_VIEW_TYPE) {
            dataBinding = DataBindingUtil.bind(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_main_card_header, parent, false)
            )!!
            initHeaderView(parent.context, dataBinding)
            HeaderViewHolder(
                dataBinding
            )
        } else {
            ViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_history, parent, false)
            )
        }
    }

    private fun initHeaderView(context: Context, dataBinding: LayoutMainCardHeaderBinding) {
        dataBinding.includeNaviCard.recyclerNavi.isNestedScrollingEnabled = false

        dataBinding.includeNaviCard.recyclerNavi.layoutManager = LinearLayoutManager(context)

        dataBinding.includeNaviCard.recyclerNavi.adapter = headerAdapter

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {

            override fun isItemViewSwipeEnabled(): Boolean {
                return true
            }

            override fun isLongPressDragEnabled(): Boolean {
                return headerItemMove
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                val currentList = headerAdapter.currentList()
                Collections.swap(currentList, fromPosition, toPosition)
                updatePoiInfoType(fromPosition, currentList)
                updatePoiInfoType(toPosition, currentList)
                headerAdapter.notifyItemMoved(fromPosition, toPosition)
                return true
            }

            private fun updatePoiInfoType(
                position: Int,
                currentList: MutableList<PoiInfoModel>
            ) {
                when (position) {
                    0 -> currentList[position].poiInfoType = PoiInfoType.NAVI_START

                    currentList.size - 1 -> currentList[position].poiInfoType =
                        PoiInfoType.NAVI_END

                    else -> currentList[position].poiInfoType = PoiInfoType.DEFAULT
                }
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

            }

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)
                headerItemMove = false
                headerAdapter.notifyDataSetChanged()
            }

        })
        itemTouchHelper.attachToRecyclerView(dataBinding.includeNaviCard.recyclerNavi)

    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            return HEADER_VIEW_TYPE
        } else {
            return ITEM_VIEW_TYPE
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            holder.dataBinding.clickListener = mOnItemClickListener
        } else {
            val model = list[position]
            when (model!!.naviType) {
                NaviType.LOCATION -> {
                    holder.itemView.tv_poi_name.text = model.locationModel?.name ?: ""
                    holder.itemView.tv_info.text = String.format(
                        "经纬度：%f , %f",
                        model.locationModel?.latLng?.longitude ?: 0f,
                        model.locationModel?.latLng?.latitude ?: 0f
                    )
                }

                else -> {
                    val format = StringBuffer().append("${model.startNavi?.name ?: ""} -> ")
                    model.wayNaviList?.let {
                        for (poiInfoModel in it) {
                            format.append("${poiInfoModel.name ?: ""} -> ")
                        }
                    }
                    format.append(model.endNavi?.name ?: "")
                    holder.itemView.tv_poi_name.text = format.toString()
                    holder.itemView.tv_info.text = ""
                }
            }
            mOnItemClickListener?.let { listener ->
                holder.itemView.setOnClickListener {
                    listener.onItemClick(it, model)
                }
            }
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class HeaderViewHolder(val dataBinding: LayoutMainCardHeaderBinding) :
        RecyclerView.ViewHolder(dataBinding.root)

    private var mOnItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(mOnItemClickListener: OnItemClickListener?) {
        this.mOnItemClickListener = mOnItemClickListener
    }

    fun onItemMove() {
        headerItemMove = true
    }

    fun submitList(list: List<MockMessageModel?>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    interface OnItemClickListener : View.OnClickListener {
        fun onItemClick(view: View?, model: MockMessageModel)
    }
}