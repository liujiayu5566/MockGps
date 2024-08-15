package com.castiel.common.base

import android.text.TextUtils
import androidx.lifecycle.*
import com.castiel.common.widget.MultiStateView
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.Exception

typealias Block<T> = suspend () -> BaseResponse<T>
typealias Success<T> = (T?) -> Unit
typealias Failure = (BaseResponse<*>) -> Unit
typealias Error = (e: String) -> Unit
typealias Complete = () -> Unit

open class BaseViewModel : ViewModel() {
    var loading: MutableLiveData<Boolean> = MutableLiveData()
    var toast: MutableLiveData<String> = MutableLiveData()
    var state: MutableLiveData<MultiStateView.ViewState> = MutableLiveData()

    protected fun <T> lauch(
        block: Block<T>,
        success: Success<T>,
        failure: Failure? = null,
        error: Error? = null,
        complete: Complete? = null
    ): Job {
        return viewModelScope.launch {
            try {
                val response = block()
                if (response.code == 0) {
                    success(response.data)
                } else {
                    failure?.run {
                        invoke(response)
                    } ?: run {
                        if (!TextUtils.isEmpty(response.message))
                            toast.value = response.message
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                error?.run { invoke(e.toString()) } ?: run { toast.value = "网络异常，请稍后重试" }
            } finally {
                complete?.invoke()
            }
        }
    }
}