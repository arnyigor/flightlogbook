package com.arny.flightlogbook.domain.airports

import com.arny.core.utils.OptionalNull
import com.arny.core.utils.toOptionalNull
import com.arny.flightlogbook.data.models.Airport
import javax.inject.Inject

class AirportsInteractor @Inject constructor(
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

    override fun saveAirport(airport: Airport): Boolean = if (airport.id == null) {
        airportsRepository.addAirport(airport)
    } else {
        airportsRepository.updateAirport(airport) > 0
    }
}
