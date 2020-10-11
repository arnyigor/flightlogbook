package com.arny.domain.airports

import com.arny.domain.models.Airport

interface IAirportsRepository {
    fun getAirports(): List<Airport>
    fun getAirportsLike(query: String): List<Airport>
    fun getAirport(airportId: Long?): Airport?
    fun addAirport(airport: Airport): Long
    fun updateAirport(airport: Airport): Int
}
