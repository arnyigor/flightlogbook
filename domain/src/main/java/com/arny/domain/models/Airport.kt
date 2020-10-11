package com.arny.domain.models

import android.os.Parcel
import android.os.Parcelable

data class Airport(
        var id: Long? = null,
        var icao: String? = null,
        var iata: String? = null,
        var nameRus: String? = null,
        var nameEng: String? = null,
        var cityRus: String? = null,
        var cityEng: String? = null,
        var countryRus: String? = null,
        var countryEng: String? = null,
        var latitude: Double? = null,
        var longitude: Double? = null,
        var elevation: Double? = null,
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readValue(Long::class.java.classLoader) as? Long,
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readValue(Double::class.java.classLoader) as? Double,
            parcel.readValue(Double::class.java.classLoader) as? Double,
            parcel.readValue(Double::class.java.classLoader) as? Double)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(icao)
        parcel.writeString(iata)
        parcel.writeString(nameRus)
        parcel.writeString(nameEng)
        parcel.writeString(cityRus)
        parcel.writeString(cityEng)
        parcel.writeString(countryRus)
        parcel.writeString(countryEng)
        parcel.writeValue(latitude)
        parcel.writeValue(longitude)
        parcel.writeValue(elevation)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Airport> {
        override fun createFromParcel(parcel: Parcel): Airport {
            return Airport(parcel)
        }

        override fun newArray(size: Int): Array<Airport?> {
            return arrayOfNulls(size)
        }
    }
}
