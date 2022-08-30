package com.castiel.common.dialog

import android.content.Context
import android.view.View
import com.castiel.common.R

class LoadingDialog(context: Context) : BaseDialog(context) {

    init {
        val view: View = layoutInflater.inflate(R.layout.dialog_loading, null)
        setContentView(view)
    }

}