package com.castio.common.base

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.launcher.ARouter
import com.castio.common.R
import com.castio.common.dialog.LoadingDialog
import com.castio.common.utils.ToastUtils
import com.castio.common.widget.MultiStateView

abstract class BaseFragment<V : ViewDataBinding, VM : BaseViewModel> : Fragment() {
    protected lateinit var mContext: Activity

    private var isDataInit = false
    protected lateinit var dataBinding: V
    protected val viewModel: VM by lazy { ViewModelProvider(this)[this.initViewModel()] }

    protected abstract fun initViewModel(): Class<VM>
    protected abstract fun initViewModelId(): Int?
    protected abstract fun getLayoutId(): Int
    protected abstract fun initView()
    protected abstract fun initData()
    protected abstract fun initObserver()

    private var loading: LoadingDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContext = activity!!
        dataBinding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false) as V
        dataBinding.lifecycleOwner = this
        ARouter.getInstance().inject(this)
        val initViewModelId = initViewModelId()
        initViewModelId?.let { dataBinding.setVariable(initViewModelId, viewModel) }
        return dataBinding.root
    }

    protected open fun setStatusBar() {
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        loading = LoadingDialog(mContext)
        initView()
        initObserver()
        addObserver()
        setStatusBar()
    }

    override fun onResume() {
        super.onResume()
        if (!isDataInit) {
            initData()
            isDataInit = true
        }
    }

    private fun addObserver() {
        val stateView: MultiStateView? =
            dataBinding.root.findViewById(R.id.state_view)
        stateView?.let {
            stateView.getView(MultiStateView.ViewState.ERROR)?.findViewById<TextView>(R.id.retry)
                ?.setOnClickListener {
                    viewModel.toast.value = "重试"
                    initData()
                }
            viewModel.state.observe(this, Observer {
                stateView.viewState = it
            })
        }
        viewModel.toast.observe(this, Observer {
            showToast(it)
        })
        viewModel.loading.observe(this, Observer {
            if (it) {
                loading?.show()
            } else {
                loading?.dismiss()
            }
        })
    }

    private fun showToast(msg: String?) {
        msg?.let { ToastUtils.showToast(mContext, msg) }
    }

    override fun onPause() {
        super.onPause()
        loading?.dismiss()
    }
}