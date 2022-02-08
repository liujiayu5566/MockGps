package com.huolala.mockgps.model

import android.os.Parcel
import android.os.Parcelable

/**
 * @author jiayu.liu
 */
data class MockMessageModel(
    var locationModel: PoiInfoModel? = null,
    var startNavi: PoiInfoModel? = null,
    var endNavi: PoiInfoModel? = null,
    /**
     * 0.模拟定位  1.模拟导航
     */
    var fromTag: Int = 0,
    /***
     * 速度 单位 KM/H
     */
    var speed: Int = 60,
    var uid: String? = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(PoiInfoModel::class.java.classLoader),
        parcel.readParcelable(PoiInfoModel::class.java.classLoader),
        parcel.readParcelable(PoiInfoModel::class.java.classLoader),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(locationModel, flags)
        parcel.writeParcelable(startNavi, flags)
        parcel.writeParcelable(endNavi, flags)
        parcel.writeInt(fromTag)
        parcel.writeInt(speed)
        parcel.writeString(uid)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MockMessageModel> {
        override fun createFromParcel(parcel: Parcel): MockMessageModel {
            return MockMessageModel(parcel)
        }

        override fun newArray(size: Int): Array<MockMessageModel?> {
            return arrayOfNulls(size)
        }
    }
}
