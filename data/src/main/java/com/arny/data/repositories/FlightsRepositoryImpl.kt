package com.arny.data.repositories

import com.arny.data.db.MainDB
import com.arny.data.db.daos.FlightDAO
import com.arny.data.models.toFlightEntity
import com.arny.data.utils.DBUtils
import com.arny.domain.flights.FlightsRepository
import com.arny.domain.models.Flight
import com.arny.helpers.utils.getLongValue
import com.arny.helpers.utils.toItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlightsRepositoryImpl @Inject constructor(private val flightDAO: FlightDAO, private val mainDB: MainDB) : FlightsRepository {
    override fun getDbFlights(order: String): List<Flight> {
        return flightDAO.queryFlightsWithOrder(order)
                .map { it.toFlight() }
    }

    override fun resetTableFlights(): Boolean {
        return DBUtils.runSQl(
                mainDB,
                "UPDATE sqlite_sequence SET seq = (SELECT count(*) FROM main_table) WHERE name='main_table'",
                false
        )
    }

    override fun getDbFlights(): List<Flight> {
        return flightDAO.queryFlights()
                .map { it.toFlight() }
    }

    override fun getStatisticDbFlights(startDate: Long, endDate: Long, includeEnd: Boolean): List<Flight> {
        if (includeEnd) {
            return flightDAO.queryFlightsInclude(startDate, endDate)
                    .map { it.toFlight() }
        }
        return flightDAO.queryFlights(startDate, endDate)
                .map { it.toFlight() }
    }

    override fun getStatisticDbFlightsByFlightTypes(
            startDate: Long,
            endDate: Long,
            flightTypes: List<Long?>,
            includeEnd: Boolean
    ): List<Flight> {
        if (includeEnd) {
            return flightDAO.queryFlightsByFlightTypesInclude(startDate, endDate, flightTypes)
                    .map { it.toFlight() }
        }
        return flightDAO.queryFlightsByFlightTypes(startDate, endDate, flightTypes)
                .map { it.toFlight() }
    }

    override fun getStatisticDbFlightsByPlanes(
            startDate: Long,
            endDate: Long,
            planeTypes: List<Long?>,
            includeEnd: Boolean
    ): List<Flight> {
        if (includeEnd) {
            return flightDAO.queryFlightsByPlanesIncludeEnd(startDate, endDate, planeTypes)
                    .map { it.toFlight() }
        }
        return flightDAO.queryFlightsByPlanes(startDate, endDate, planeTypes)
                .map { it.toFlight() }
    }

    override fun getStatisticDbFlightsMinMax(): Pair<Long, Long> {
        var min = 0L
        var max = 0L
        flightDAO.queryMinMaxDateTimes().toItem {
            min = it.getLongValue("first")
            max = it.getLongValue("last")
        }
        return Pair(min, max)
    }

    override fun updateFlight(flight: Flight): Boolean {
        return flightDAO.updateReplace(flight.toFlightEntity()) > 0
    }

    override fun insertFlight(flight: Flight): Boolean {
        return flightDAO.insertReplace(flight.toFlightEntity()) > 0
    }

    override fun insertFlightAndGet(flight: Flight): Long {
        return flightDAO.insertReplace(flight.toFlightEntity())
    }

    override fun insertFlights(flights: List<Flight>): Boolean {
        return flightDAO.insertReplace(
                flights.map { it.toFlightEntity() }
        ).any { it > 0 }
    }

    override fun getFlight(id: Long?): Flight? {
        return flightDAO.queryFlight(id)?.toFlight()
    }

    override fun getFlightsTime(): Int {
        val cursor = flightDAO.queryFlightTime()
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
            cursor.close()
        }
        return count
    }

    override fun getFlightsCount(): Int {
        val cursor = flightDAO.queryFlightsCount()
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
            cursor.close()
        }
        return count
    }

    override fun removeAllFlights(): Boolean {
        return flightDAO.delete() > 0
    }

    override fun removeFlight(id: Long?): Boolean {
        return flightDAO.delete(id) > 0
    }

}