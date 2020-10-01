package com.arny.domain.airports

import com.arny.domain.models.Airport

interface IAirportsInteractor {
    fun getAirports(): List<Airport>
    fun queryAirports(query: String): List<Airport>
}
