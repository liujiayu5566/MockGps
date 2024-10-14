package com.huolala.mockgps.model

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.IntDef
import com.baidu.mapapi.model.LatLng


@IntDef(PoiInfoType.DEFAULT, PoiInfoType.LOCATION, PoiInfoType.NAVI_START, PoiInfoType.NAVI_END)
@Retention(AnnotationRetention.SOURCE)
annotation class PoiInfoType {
    companion object {
        const val DEFAULT = -1
        const val LOCATION = 0
        const val NAVI_START = 1
        const val NAVI_END = 2
    }
}

/**
 * @author jiayu.liu
 */
data class PoiInfoModel(
    var latLng: LatLng? = null,
    var uid: String? = null,
    var name: String? = "",
    /**
     * -1.默认选址模式  0.模拟定位  1.模拟导航起点  2.模拟导航终点
     */
    @PoiInfoType
    var poiInfoType: Int = PoiInfoType.LOCATION,
    var city: String? = "北京市"
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(LatLng::class.java.classLoader),
        parcel.readString(), parcel.readString(), parcel.readInt(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(latLng, flags)
        parcel.writeString(uid)
        parcel.writeString(name)
        parcel.writeInt(poiInfoType)
        parcel.writeString(city)
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