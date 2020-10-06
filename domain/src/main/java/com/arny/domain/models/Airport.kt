package com.arny.domain.models

import android.os.Parcel
import android.os.Parcelable

data class Airport(
        val id: Long? = null,
        val icao: String? = null,
        val iata: String? = null,
        val nameRus: String? = null,
        val nameEng: String? = null,
        val cityRus: String? = null,
        val cityEng: String? = null,
        val countryRus: String? = null,
        val countryEng: String? = null,
        val latitude: Double? = null,
        val longitude: Double? = null,
        val elevation: Double? = null,
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
