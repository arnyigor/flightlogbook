package com.arny.domain.airports

import com.arny.domain.models.Airport
import javax.inject.Inject

class AirportsInteractor @Inject constructor(
        private val airportsRepository: IAirportsRepository
) : IAirportsInteractor {
    override fun getAirports(): List<Airport> {
        return airportsRepository.getAirports()
    }
}
