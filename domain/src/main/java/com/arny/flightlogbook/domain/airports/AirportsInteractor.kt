package com.arny.flightlogbook.domain.airports

import com.arny.core.utils.OptionalNull
import com.arny.core.utils.toOptionalNull
import com.arny.flightlogbook.domain.models.Airport

class AirportsInteractor(
    private val airportsRepository: IAirportsRepository
) : IAirportsInteractor {
    override fun getAirports(): List<Airport> = airportsRepository.getAirports()
    override fun queryAirports(query: String): List<Airport> {
        return if (query.isBlank()) {
            return emptyList()
        } else {
            airportsRepository.getAirportsLike(query)
        }
    }

    override fun getAirport(airportId: Long?): OptionalNull<Airport?> =
        airportsRepository.getAirport(airportId).toOptionalNull()

    override fun saveAirport(airport: Airport): Boolean {
        return if (airport.id == null) {
            airportsRepository.addAirport(airport) > 0
        } else {
            airportsRepository.updateAirport(airport) > 0
        }
    }
}
