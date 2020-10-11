package com.arny.flightlogbook.data.repositories

import com.arny.domain.airports.IAirportsRepository
import com.arny.domain.models.Airport
import com.arny.flightlogbook.data.db.daos.AirportsDAO
import com.arny.flightlogbook.data.models.airports.toAirportEntity
import javax.inject.Inject

class AirportsRepository @Inject constructor(
        private val airportsDAO: AirportsDAO
) : IAirportsRepository {
    override fun getAirports(): List<Airport> {
        return airportsDAO.getDbAirports().map { it.toAirport() }
    }

    override fun getAirportsLike(query: String): List<Airport> {
        return airportsDAO.getDbAirportsLike("$query%", "$query%", "$query%", "$query%", "$query%").map { it.toAirport() }
    }

    override fun getAirport(airportId: Long?): Airport? {
        return airportsDAO.getDbAirport(airportId)?.toAirport()
    }

    override fun addAirport(airport: Airport): Long {
        return airportsDAO.insertReplace(airport.toAirportEntity())
    }

    override fun updateAirport(airport: Airport): Int {
        return airportsDAO.updateReplace(airport.toAirportEntity())
    }
}
