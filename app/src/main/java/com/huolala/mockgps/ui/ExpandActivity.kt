package com.huolala.mockgps.ui

import android.content.Intent
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.KeyboardUtils
import com.castiel.common.base.BaseActivity
import com.castiel.common.base.BaseListAdapter
import com.castiel.common.base.BaseViewModel
import com.castiel.common.decoration.VerticalItemDecoration
import com.huolala.mockgps.R
import com.huolala.mockgps.adaper.ExpandAdapter
import com.huolala.mockgps.databinding.ActivityExpandBinding
import com.huolala.mockgps.model.ExpandModel

/**
 * @author jiayu.liu
 */
class ExpandActivity : BaseActivity<ActivityExpandBinding, BaseViewModel>() {

    private var mExpandAdapter = ExpandAdapter()
    private val titles = arrayOf(
        "模拟导航数据导入"
    );
    private val describes = arrayOf(
        "路径：/storage/emulated/0/Android/data/com.huolala.mockgps/files/nav_path"
    );
    private val mExpandData: ArrayList<ExpandModel> = arrayListOf()

    override fun initViewModel(): Class<BaseViewModel> {
        return BaseViewModel::class.java
    }

    override fun initViewModelId(): Int? {
        return null
    }

    override fun getLayout(): Int {
        return R.layout.activity_expand
    }


    override fun initView() {
        dataBinding.recycler.layoutManager = LinearLayoutManager(this);
        dataBinding.recycler.adapter = mExpandAdapter;
        dataBinding.recycler.addItemDecoration(
            VerticalItemDecoration(
                this,
                R.drawable.shape_item_line_verticall,
                10f,
                0f
            )
        )
        mExpandAdapter.clickListener = object : BaseListAdapter.OnItemClickListener<ExpandModel> {
            override fun onItemClick(view: View?, t: ExpandModel, position: Int) {
                when (t.title) {
                    titles[0] -> {//模拟导航文件导入
                        startActivity(Intent(this@ExpandActivity, FileMockActivity::class.java))
                    }
                    else -> {
                    }
                }
            }
        }

    }

    override fun initData() {
        //初始化数据
        for (i in titles.indices) {
            mExpandData.add(
                ExpandModel(
                    titles[i],
                    describes[i]
                )
            )
        }
        mExpandAdapter.submitList(mExpandData)
    }

    override fun initObserver() {
    }



}