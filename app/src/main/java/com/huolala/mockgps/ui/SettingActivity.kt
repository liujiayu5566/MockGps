package com.huolala.mockgps.ui

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.castiel.common.base.BaseActivity
import com.castiel.common.base.BaseViewModel
import com.castiel.common.recycler.decoration.VerticalItemDecoration
import com.huolala.mockgps.R
import com.huolala.mockgps.adaper.SettingAdapter
import com.huolala.mockgps.databinding.ActivitySettingBinding
import com.huolala.mockgps.model.SettingMsgModel
import com.huolala.mockgps.utils.MMKVUtils

/**
 * @author jiayu.liu
 */
class SettingActivity : BaseActivity<ActivitySettingBinding, BaseViewModel>() {
    private var mAdapter: SettingAdapter? = null
    private val mTitle = arrayOf(
        "模拟定位震动功能"
    )
    private val mMsg = arrayOf(
        "定位开启后，自动微调参数(方向、经纬度等)"
    )

    override fun initViewModel(): Class<BaseViewModel> {
        return BaseViewModel::class.java
    }

    override fun getLayout(): Int {
        return R.layout.activity_setting
    }

    override fun initView() {
        dataBinding.recycler.layoutManager = LinearLayoutManager(this)
        mAdapter = SettingAdapter()
        dataBinding.recycler.adapter = mAdapter
        dataBinding.recycler.addItemDecoration(
            VerticalItemDecoration(
                this,
                R.drawable.shape_item_line_verticall,
                10f,
                0f
            )
        )

        mAdapter?.listener = object : SettingAdapter.OnItemListener {
            override fun onItemSwitch(view: View?, model: SettingMsgModel, isChecked: Boolean) {
                when (model.title) {
                    mTitle[0] -> MMKVUtils.setLocationQuiver(isChecked)
                    else -> {}
                }
            }

        }
    }

    override fun initData() {
        val settingModel = MMKVUtils.getSettingModel()
        mAdapter?.submitList(
            mutableListOf(
                SettingMsgModel(
                    title = mTitle[0],
                    msg = mMsg[0],
                    isSwitch = settingModel.isLocationQuiver
                )
            )
        )
    }

    override fun initObserver() {
    }


}