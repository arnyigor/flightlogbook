package com.arny.domain.airports

import com.arny.domain.models.Airport
import javax.inject.Inject

class AirportsInteractor @Inject constructor(
        private val airportsRepository: IAirportsRepository
) : IAirportsInteractor {
    override fun getAirports(): List<Airport> = airportsRepository.getAirports()
    override fun queryAirports(query: String): List<Airport> = airportsRepository.getAirportsLike(query)
}
