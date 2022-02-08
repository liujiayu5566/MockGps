package com.huolala.mockgps.model

import android.os.Parcel
import android.os.Parcelable
import com.baidu.mapapi.model.LatLng


/**
 * @author jiayu.liu
 */
data class PoiInfoModel(
    var latLng: LatLng? = null,
    var uid: String? = null,
    var name: String? = "",
    /**
     * 0.模拟定位  1.模拟导航起点  2.模拟导航终点
     */
    var fromTag: Int = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(LatLng::class.java.classLoader),
        parcel.readString(), parcel.readString(), parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(latLng, flags)
        parcel.writeString(uid)
        parcel.writeString(name)
        parcel.writeInt(fromTag)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PoiInfoModel> {
        override fun createFromParcel(parcel: Parcel): PoiInfoModel {
            return PoiInfoModel(parcel)
        }

        override fun newArray(size: Int): Array<PoiInfoModel?> {
            return arrayOfNulls(size)
        }
    }
}