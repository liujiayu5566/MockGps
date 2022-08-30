package com.huolala.mockgps.ui

import android.app.Dialog
import android.content.Intent
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.blankj.utilcode.util.ClickUtils
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ToastUtils
import com.castiel.common.base.BaseActivity
import com.castiel.common.base.BaseViewModel
import com.huolala.mockgps.R
import com.huolala.mockgps.databinding.ActivityFileBinding
import com.huolala.mockgps.databinding.DialogFileMockHintBinding
import com.huolala.mockgps.model.MockMessageModel
import com.huolala.mockgps.model.NaviType
import com.huolala.mockgps.widget.NaviPathDialog
import com.huolala.mockgps.widget.NaviPopupWindow

/**
 * @author jiayu.liu
 */
class FileMockActivity : BaseActivity<ActivityFileBinding, BaseViewModel>(), View.OnClickListener {

    override fun initViewModel(): Class<BaseViewModel> {
        return BaseViewModel::class.java
    }

    override fun initViewModelId(): Int? {
        return null
    }

    override fun getLayout(): Int {
        return R.layout.activity_file
    }

    override fun initView() {
        KeyboardUtils.clickBlankArea2HideSoftInput()


        ClickUtils.applySingleDebouncing(dataBinding.ivNaviSetting, this)
        ClickUtils.applySingleDebouncing(dataBinding.ivWarning, this)
        ClickUtils.applySingleDebouncing(dataBinding.btnFile, this)
        ClickUtils.applySingleDebouncing(dataBinding.btnStartNavi, this)
    }

    override fun initData() {

    }

    override fun initObserver() {

    }

    override fun onClick(v: View?) {
        when (v) {
            dataBinding.ivWarning -> {
                Dialog(this).apply {
                    DataBindingUtil.bind<DialogFileMockHintBinding>(
                        LayoutInflater.from(this@FileMockActivity)
                            .inflate(R.layout.dialog_file_mock_hint, null, false)
                    )?.let {
                        setContentView(it.root)
                        window?.run {
                            val lp = attributes;
                            lp.width = ViewGroup.LayoutParams.MATCH_PARENT
                            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
                            lp.y = -ConvertUtils.px2dp(50f)
                            attributes = lp
                        }
                        ClickUtils.applySingleDebouncing(it.btnConfirm) {
                            dismiss()
                        }
                        show()
                    }
                }
            }
            dataBinding.ivNaviSetting -> {
                NaviPopupWindow(this).apply {
                    show(dataBinding.ivNaviSetting)
                }
            }
            dataBinding.btnFile -> {
                NaviPathDialog(this).run {
                    listener = object : NaviPathDialog.NaviPathListener {
                        override fun onItemClick(path: String) {
                            if (TextUtils.isEmpty(path)) {
                                return
                            }
                            dataBinding.edFile.setText(path)
                        }
                    }
                    show()
                }
            }
            dataBinding.btnStartNavi -> {
                val text = dataBinding.edFile.text
                if (TextUtils.isEmpty(text)) {
                    ToastUtils.showShort("路径不能为null")
                    return
                }
                val model = MockMessageModel(
                    naviType = NaviType.NAVI_FILE,
                    path = text.toString()
                )
                val intent = Intent(this, MockLocationActivity::class.java)
                intent.putExtra("model", model)
                startActivity(intent)
            }
            else -> {
            }
        }
    }

}