package com.arny.data.repositories

import android.database.Cursor
import com.arny.constants.CONSTS.STRINGS.PARAM_COLOR
import com.arny.data.db.MainDB
import com.arny.data.db.daos.FlightDAO
import com.arny.data.models.toFlightEntity
import com.arny.data.utils.DBUtils
import com.arny.domain.flights.FlightsRepository
import com.arny.domain.models.Flight
import com.arny.domain.models.Params
import com.arny.helpers.utils.*
import io.reactivex.Single
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

    override fun getNotEmptyColors(): Single<List<String>> {
        return fromSingle { queryDBColors() }
    }

    private fun queryDBColors(): List<String> {
        val colors = mutableListOf<String>()
        flightDAO.queryNotEmptyColorParams().toList {
            val params = Params(it.getStringValue("params"))
            params.getParam(PARAM_COLOR)?.let { it1 -> colors.add(it1) }
        }
        return colors.distinct()
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

    override fun getStatisticDbFlightsByColor(startDate: Long, endDate: Long, includeEnd: Boolean, query: String): List<Flight> {
        if (includeEnd) {
            return flightDAO.queryFlightsByColorInclude(startDate, endDate, query)
                    .map { it.toFlight() }
        }
        return flightDAO.queryFlightsByColor(startDate, endDate, query)
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

    private fun getCursorInt(cursor: Cursor): Int {
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
            cursor.close()
        }
        return count
    }

    override fun getFlightsTime(): Int = getCursorInt(flightDAO.queryFlightTime())

    override fun getNightTime(): Int = getCursorInt(flightDAO.queryNightTime())

    override fun getGroundTime(): Int = getCursorInt(flightDAO.queryGroundTime())

    override fun getFlightsCount(): Int = getCursorInt(flightDAO.queryFlightsCount())

    override fun removeAllFlights(): Boolean = flightDAO.delete() > 0

    override fun removeFlight(id: Long?): Boolean = flightDAO.delete(id) > 0

}