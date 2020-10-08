package com.arny.domain.airports

import com.arny.domain.models.Airport
import com.arny.helpers.utils.OptionalNull

interface IAirportsInteractor {
    fun getAirports(): List<Airport>
    fun getAirport(airportId:Long?): OptionalNull<Airport?>
    fun queryAirports(query: String): List<Airport>
}
