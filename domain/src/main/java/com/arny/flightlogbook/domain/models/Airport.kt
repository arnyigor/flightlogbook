package com.arny.flightlogbook.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
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
) : Parcelable
