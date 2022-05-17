package com.castio.common.dialog

import android.content.Context
import android.view.View
import com.castio.common.R

class LoadingDialog(context: Context) : BaseDialog(context) {

    init {
        val view: View = layoutInflater.inflate(R.layout.dialog_loading, null)
        setContentView(view)
    }

}