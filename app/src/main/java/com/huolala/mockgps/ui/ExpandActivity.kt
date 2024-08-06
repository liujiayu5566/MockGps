package com.huolala.mockgps.ui

import android.app.Dialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ClickUtils
import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.ToastUtils
import com.castiel.common.base.BaseActivity
import com.castiel.common.base.BaseListAdapter
import com.castiel.common.base.BaseViewModel
import com.castiel.common.recycler.decoration.VerticalItemDecoration
import com.huolala.mockgps.R
import com.huolala.mockgps.adaper.ExpandAdapter
import com.huolala.mockgps.databinding.ActivityExpandBinding
import com.huolala.mockgps.databinding.DialogHintBinding
import com.huolala.mockgps.model.ExpandModel
import com.huolala.mockgps.widget.HintDialog

/**
 * @author jiayu.liu
 */
class ExpandActivity : BaseActivity<ActivityExpandBinding, BaseViewModel>() {

    private var mExpandAdapter = ExpandAdapter()
    private val titles = arrayOf(
        "外部app启动模拟导航",
        "模拟导航数据导入",
        "github地址",
        "免责声明",
    )
    private val describes = arrayOf(
        "通过广播形式发送起终点信息",
        "路径：/storage/emulated/0/Android/data/com.huolala.mockgps/files/nav_path",
        "https://github.com/liujiayu5566/MockGps",
        "此应用仅限开发学习和开发使用，软件的发布和使用均不收取任何费用。拒绝任何人或任何实体进行出售、重新修改后分发，严禁用于商业谋利用途。项目维护者对软件的滥用不承担任何责任。",
    )
    private val navigation = arrayOf(
        null,
        FileMockActivity::class.java,
        null,
        null,
    )
    private val mExpandData: ArrayList<ExpandModel> = arrayListOf()

    override fun initViewModel(): Class<BaseViewModel> {
        return BaseViewModel::class.java
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
                navigation[position]?.let {
                    startActivity(Intent(this@ExpandActivity, navigation[position]))
                } ?: run {
                    when (t.title) {
                        "外部app启动模拟导航" -> {
                            HintDialog(
                                this@ExpandActivity,
                                "外部广播",
                                getString(R.string.receiver_hint)
                            ).show()
                        }

                        "github地址" -> {
                            ClipboardUtils.copyText("https://github.com/liujiayu5566/MockGps")
                            ToastUtils.showShort("复制成功")
                        }
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