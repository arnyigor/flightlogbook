package com.arny.flightlogbook.data.repositories

import com.arny.domain.airports.IAirportsRepository
import com.arny.domain.models.Airport
import com.arny.flightlogbook.data.db.daos.AirportsDAO
import com.arny.flightlogbook.data.models.airports.toAirportEntity
import javax.inject.Inject

class AirportsRepository @Inject constructor(
    private val airportsDAO: AirportsDAO
) : IAirportsRepository {
    override fun getAirports(): List<Airport> = airportsDAO.getDbAirports().map { it.toAirport() }

    override fun getAirportsLike(query: String): List<Airport> =
        airportsDAO.getDbAirportsLike("$query%", "$query%", "$query%", "$query%", "$query%").map { it.toAirport() }

    override fun getAirport(airportId: Long?): Airport? = airportsDAO.getDbAirport(airportId)?.toAirport()

    override fun getAirportByIcao(icao: String?): Airport? = airportsDAO.getDbAirportByIcao(icao)?.toAirport()

    override fun getAirportByIata(iata: String?): Airport? = airportsDAO.getDbAirportByIata(iata)?.toAirport()

    override fun addAirport(airport: Airport): Long = airportsDAO.insertReplace(airport.toAirportEntity())

    override fun updateAirport(airport: Airport): Int = airportsDAO.updateReplace(airport.toAirportEntity())
}
