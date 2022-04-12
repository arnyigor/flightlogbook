package com.arny.flightlogbook.domain.airports

import com.arny.core.utils.OptionalNull
import com.arny.flightlogbook.domain.models.Airport

interface IAirportsInteractor {
    fun getAirports(): List<Airport>
    fun getAirport(airportId: Long?): OptionalNull<Airport?>
    fun queryAirports(query: String): List<Airport>
    fun saveAirport(airport: Airport): Boolean
}
