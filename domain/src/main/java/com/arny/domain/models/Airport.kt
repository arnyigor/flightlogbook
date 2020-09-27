package com.arny.domain.models

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
)
