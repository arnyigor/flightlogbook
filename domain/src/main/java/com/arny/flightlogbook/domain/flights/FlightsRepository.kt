package com.arny.flightlogbook.domain.flights

import com.arny.flightlogbook.domain.models.Flight
import com.arny.flightlogbook.domain.models.Result
import io.reactivex.Observable
import io.reactivex.Single

interface FlightsRepository {
    fun getPrefOrderflights(filtertype: Int): String
    fun getDbFlightsOrdered(): Observable<Result<List<Flight>>>
    fun getDbFlights(order: String): Result<List<Flight>>
    fun getDbFlights(): List<Flight>
    fun getStatisticDbFlights(startDate: Long, endDate: Long, includeEnd: Boolean): List<Flight>
    fun getStatisticDbFlightsByFlightTypes(
            startDate: Long,
            endDate: Long,
            flightTypes: List<Long?>,
            includeEnd: Boolean
    ): List<Flight>

    fun getStatisticDbFlightsByAircraftTypes(
            startDate: Long,
            endDate: Long,
            planeTypes: List<Long?>,
            includeEnd: Boolean
    ): List<Flight>

    fun getStatisticDbFlightsByColor(
            startDate: Long,
            endDate: Long,
            includeEnd: Boolean,
            query: String
    ): List<Flight>

    fun getStatisticDbFlightsMinMax(): Pair<Long, Long>
    fun updateFlight(flight: Flight): Boolean
    fun insertFlight(flight: Flight): Boolean
    fun insertFlightAndGet(flight: Flight): Long
    fun insertFlights(flights: List<Flight>): Boolean
    fun getFlight(id: Long?): Flight?
    fun getFlightsTime(): Int
    fun getNightTime(): Int
    fun getGroundTime(): Int
    fun getFlightsCount(): Int
    fun removeAllFlights(): Boolean
    fun removeFlight(id: Long?): Boolean
    fun removeFlights(ids: List<Long>): Boolean
    fun resetTableFlights(): Boolean
    fun getNotEmptyColors(): Single<List<String>>
}
