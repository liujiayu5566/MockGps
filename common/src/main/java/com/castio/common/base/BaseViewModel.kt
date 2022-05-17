package com.castio.common.base

import android.text.TextUtils
import androidx.lifecycle.*
import com.castio.common.widget.MultiStateView
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.Exception

typealias Block<T> = suspend () -> BaseResponse<T>
typealias Success<T> = (T?) -> Unit
typealias Failure = (BaseResponse<*>) -> Unit
typealias Error = (e: String) -> Unit
typealias Complete = () -> Unit

open class BaseViewModel : ViewModel() {
    var loading: MediatorLiveData<Boolean> = MediatorLiveData()
    var toast: MediatorLiveData<String> = MediatorLiveData()
    var state: MediatorLiveData<MultiStateView.ViewState> = MediatorLiveData()

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
                if (response.errorCode == 0) {
                    success(response.data)
                } else {
                    failure?.run {
                        invoke(response)
                    } ?: if (!TextUtils.isEmpty(response.errorMsg))
                        toast.value = response.errorMsg
                }
            } catch (e: Exception) {
                e.printStackTrace()
                error?.run { invoke(e.toString()) } ?: also { toast.value = "网络异常，请稍后重试" }
            } finally {
                complete?.invoke()
            }
        }
    }
}