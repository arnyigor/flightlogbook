package com.arny.flightlogbook.domain.airports

import com.arny.flightlogbook.data.models.Airport

interface IAirportsRepository {
    fun getAirports(): List<Airport>
    fun getAirportsLike(query: String): List<Airport>
    fun getAirport(airportId: Long?): Airport?
    fun getAirport(iata: String?, icao: String?): Airport?
    fun getAirportByIcao(icao: String?): Airport?
    fun getAirportByIata(iata: String?): Airport?
    fun addAirport(airport: Airport): Boolean
    fun updateAirport(airport: Airport): Int
}
