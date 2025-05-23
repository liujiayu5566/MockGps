package com.huolala.mockgps.widget

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.baidu.mapapi.model.LatLng
import com.blankj.utilcode.util.ClickUtils
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.ToastUtils
import com.huolala.mockgps.R
import com.huolala.mockgps.databinding.DialogInputLatlngBinding

/**
 * @author jiayu.liu
 *
 */
enum class LatLngType {
    LngLat,
    LatLng
}

class InputLatLngDialog(
    context: Context,
    listener: InputLatLngDialogListener?
) : Dialog(context) {
    private var latLngType = LatLngType.LngLat

    init {
        DataBindingUtil.bind<DialogInputLatlngBinding>(
            LayoutInflater.from(context)
                .inflate(R.layout.dialog_input_latlng, null, false)
        )?.let { dataBinding ->
            setContentView(dataBinding.root)
            window?.run {
                setBackgroundDrawableResource(R.color.transparent)
                val lp = attributes
                lp.width = ScreenUtils.getScreenWidth() - ConvertUtils.dp2px(20f)
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
                attributes = lp
            }

            dataBinding.latLng =
                "输入经纬度: ${if (latLngType == LatLngType.LngLat) "lng,lat" else "lat,lng"}"
            dataBinding.hint = "116.01894,39.28375"

            ClickUtils.applySingleDebouncing(dataBinding.btnConfirm) {
                dataBinding.editLatlng.text?.toString()?.let { text ->
                    var input = text
                    if (text.contains("，")) {
                        input = text.replace("，", ",")
                    }

                    val split = input.split(",")

                    if (split.size == 2) {
                        val latLng = try {
                            if (latLngType == LatLngType.LngLat) {
                                LatLng(split[1].toDouble(), split[0].toDouble())
                            } else {
                                LatLng(split[0].toDouble(), split[1].toDouble())
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            ToastUtils.showShort("请输入正确的经纬度")
                            return@let
                        }
                        listener?.onConfirm(latLng)
                        dismiss()
                    }
                }
            }

            ClickUtils.applySingleDebouncing(dataBinding.btnChange) {
                latLngType = if (latLngType == LatLngType.LngLat) {
                    LatLngType.LatLng
                } else {
                    LatLngType.LngLat
                }
                val text = if (latLngType == LatLngType.LngLat) "lng,lat" else "lat,lng"
                dataBinding.hint =
                    if (latLngType == LatLngType.LngLat) "116.01894,39.28375" else "39.28375,116.01894"
                dataBinding.latLng = "输入经纬度: $text"
            }

            ClickUtils.applySingleDebouncing(dataBinding.btnCancel) {
                dismiss()
            }
        }
    }

    interface InputLatLngDialogListener {
        fun onConfirm(latLng: LatLng)
    }
}