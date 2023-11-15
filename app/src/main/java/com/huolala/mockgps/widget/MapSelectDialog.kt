package com.huolala.mockgps.widget

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.baidu.mapapi.map.TextureMapView
import com.baidu.mapapi.search.route.DrivingRouteLine
import com.blankj.utilcode.util.ClickUtils
import com.blankj.utilcode.util.ScreenUtils
import com.castiel.common.dialog.BaseDialog
import com.huolala.mockgps.R
import kotlinx.android.synthetic.main.dialog_select_navi_map.*

/**
 * @author jiayu.liu
 */
class MapSelectDialog(context: Context, var routeLines: List<DrivingRouteLine>) :
    BaseDialog(context) {

    init {
        layoutInflater.inflate(R.layout.dialog_select_navi_map, null, false).apply {
            setContentView(this)
        }

        texture_mapview.onCreate(context, null)
        btn_change.setOnClickListener {

        }
        btn_select.setOnClickListener {
            dismiss()
        }

        window?.let {
            val lp: WindowManager.LayoutParams = it.attributes
            lp.width = ScreenUtils.getScreenWidth()
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            lp.gravity = Gravity.CENTER
            it.attributes = lp
        }
    }
}