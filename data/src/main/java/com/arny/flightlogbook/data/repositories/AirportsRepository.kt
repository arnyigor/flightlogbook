package com.arny.flightlogbook.data.repositories

import com.arny.domain.airports.IAirportsRepository
import com.arny.domain.models.Airport
import com.arny.flightlogbook.data.db.daos.AirportsDAO
import javax.inject.Inject

class AirportsRepository @Inject constructor(
        private val airportsDAO: AirportsDAO
) : IAirportsRepository {
    override fun getAirports(): List<Airport> {
        return airportsDAO.getDbAirports().map { it.toAirport() }
    }

    override fun getAirportsLike(query: String): List<Airport> {
        return airportsDAO.getDbAirportsLike("%$query", "%$query", "%$query", "%$query", "%$query").map { it.toAirport() }
    }
}
