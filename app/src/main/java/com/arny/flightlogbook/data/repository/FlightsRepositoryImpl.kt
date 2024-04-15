package com.arny.flightlogbook.data.repository

import android.database.Cursor
import com.arny.flightlogbook.data.CONSTS
import com.arny.flightlogbook.data.CONSTS.STRINGS.PARAM_COLOR
import com.arny.flightlogbook.data.db.MainDB
import com.arny.flightlogbook.data.db.daos.FlightDAO
import com.arny.flightlogbook.data.models.Flight
import com.arny.flightlogbook.data.db.intity.flights.toFlightEntity
import com.arny.flightlogbook.data.db.intity.flights.toParams
import com.arny.flightlogbook.data.models.toResult
import com.arny.flightlogbook.data.utils.DBUtils
import com.arny.flightlogbook.data.utils.getLongValue
import com.arny.flightlogbook.data.utils.getStringValue
import com.arny.flightlogbook.data.utils.toItem
import com.arny.flightlogbook.data.utils.toList
import com.arny.flightlogbook.domain.common.PreferencesProvider
import com.arny.flightlogbook.domain.flights.FlightsRepository
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject
import com.arny.flightlogbook.data.models.AppResult

class FlightsRepositoryImpl @Inject constructor(
    private val flightDAO: FlightDAO,
    private val mainDB: MainDB,
    private val preferencesProvider: PreferencesProvider
) : FlightsRepository {
    override fun getDbFlights(order: String): AppResult<List<Flight>> =
        flightDAO.queryFlightsWithOrder(order)
            .map { it.toFlight() }
            .toResult()

    override fun getPrefOrderflights(filtertype: Int): String = when (filtertype) {
        0 -> CONSTS.DB.COLUMN_DATETIME
        1 -> CONSTS.DB.COLUMN_DATETIME + " DESC"
        2 -> CONSTS.DB.COLUMN_LOG_TIME
        3 -> CONSTS.DB.COLUMN_LOG_TIME + " DESC"
        else -> CONSTS.DB.COLUMN_DATETIME + " ASC"
    }

    override fun getDbFlightsOrdered(): Observable<AppResult<List<Flight>>> = Observable.fromCallable {
        getDbFlights(
            getPrefOrderflights(
                preferencesProvider.getPrefInt(CONSTS.PREFS.PREF_USER_FILTER_FLIGHTS)
            )
        )
    }

    override fun resetTableFlights(): Boolean = DBUtils.runSQl(
        db = mainDB,
        sql = "UPDATE sqlite_sequence SET seq = (SELECT count(*) FROM main_table) WHERE name='main_table'",
        closeDb = false
    )

    override fun getNotEmptyColors(): Single<List<String>> = Single.fromCallable {
        queryDBColors()
    }

    private fun queryDBColors(): List<String> {
        val colors = mutableListOf<String>()
        flightDAO.queryNotEmptyColorParams().toList {
            val params = it.getStringValue("params")?.toParams()
            params?.get(PARAM_COLOR)?.let { color ->
                colors.add(color.toString())
            }
        }
        return colors.distinct()
    }

    override fun getDbFlights(): List<Flight> = flightDAO.queryFlights()
        .map { it.toFlight() }

    override fun getStatisticDbFlights(
        startDate: Long,
        endDate: Long,
        includeEnd: Boolean
    ): List<Flight> = if (includeEnd) {
        flightDAO.queryFlightsInclude(startDate, endDate)
    } else {
        flightDAO.queryFlights(startDate, endDate)
    }
        .map { it.toFlight() }

    override fun getStatisticDbFlightsByColor(
        startDate: Long,
        endDate: Long,
        includeEnd: Boolean,
        query: String
    ): List<Flight> = if (includeEnd) {
        flightDAO.queryFlightsByColorInclude(startDate, endDate, query)
    } else {
        flightDAO.queryFlightsByColor(startDate, endDate, query)
    }
        .map { it.toFlight() }

    override fun getStatisticDbFlightsByFlightTypes(
        startDate: Long,
        endDate: Long,
        flightTypes: List<Long?>,
        includeEnd: Boolean
    ): List<Flight> = if (includeEnd) {
        flightDAO.queryFlightsByFlightTypesInclude(startDate, endDate, flightTypes)
            .map { it.toFlight() }
    } else {
        flightDAO.queryFlightsByFlightTypes(startDate, endDate, flightTypes)
            .map { it.toFlight() }
    }

    override fun getStatisticDbFlightsByAircraftTypes(
        startDate: Long,
        endDate: Long,
        planeTypes: List<Long?>,
        includeEnd: Boolean
    ): List<Flight> = if (includeEnd) {
        flightDAO.queryFlightsByPlanesIncludeEnd(startDate, endDate, planeTypes)
            .map { it.toFlight() }
    } else {
        flightDAO.queryFlightsByPlanes(startDate, endDate, planeTypes)
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

    override fun updateFlight(flight: Flight): Boolean =
        flightDAO.updateReplace(flight.toFlightEntity()) > 0

    override fun insertFlight(flight: Flight): Boolean =
        flightDAO.insertReplace(flight.toFlightEntity()) > 0

    override fun insertFlightAndGet(flight: Flight): Long =
        flightDAO.insertReplace(flight.toFlightEntity())

    override fun insertFlights(flights: List<Flight>): Boolean = flightDAO.insertReplace(
        flights.map { it.toFlightEntity() }
    ).any { it > 0 }

    override fun getFlight(id: Long?): Flight? = flightDAO.queryFlight(id)?.toFlight()

    private fun getCursorInt(cursor: Cursor): Int {
        var count = 0
        cursor.use {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0)
            }
        }
        return count
    }

    override fun getFlightsTime(): Int = getCursorInt(flightDAO.queryFlightTime())

    override fun getNightTime(): Int = getCursorInt(flightDAO.queryNightTime())

    override fun getGroundTime(): Int = getCursorInt(flightDAO.queryGroundTime())

    override fun getFlightsCount(): Int = getCursorInt(flightDAO.queryFlightsCount())

    override fun removeAllFlights(): Boolean = flightDAO.delete() > 0

    override fun removeFlight(id: Long?): Boolean = flightDAO.delete(id) > 0

    override fun removeFlights(ids: List<Long>): Boolean = flightDAO.delete(ids) > 0
}