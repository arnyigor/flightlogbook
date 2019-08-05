package com.arny.domain.flights

import com.arny.constants.CONSTS
import com.arny.data.db.intities.TimeToFlightEntity
import com.arny.data.repositories.MainRepositoryImpl
import com.arny.domain.R
import com.arny.domain.models.*
import com.arny.helpers.utils.DateTimeUtils
import com.arny.helpers.utils.OptionalNull
import com.arny.helpers.utils.fromCallable
import com.arny.helpers.utils.fromNullable
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlightsUseCase @Inject constructor(private val repository: MainRepositoryImpl) {

    fun insertFlights(flights: List<Flight>): Observable<Boolean> {
        return fromCallable { repository.insertFlights(flights.map { it.toFlightEntiry() }) }
    }

    fun removeAllFlights(): Observable<Boolean> {
        return fromCallable { repository.removeAllFlights() }
    }

    fun updateFlight(flight: Flight): Observable<Boolean> {
        return fromCallable { repository.updateFlight(flight.toFlightEntiry()) }
    }

    fun insertFlight(flight: Flight): Observable<Boolean> {
        return fromCallable { repository.insertFlight(flight.toFlightEntiry()) }
    }

    fun insertFlightAndGet(flight: Flight): Observable<Long> {
        return fromCallable { repository.insertFlightAndGet(flight.toFlightEntiry()) }
    }

    fun getFlight(id: Long?): Observable<OptionalNull<Flight?>> {
        return fromNullable { repository.getFlight(id)?.toFlight() }
    }

    fun loadPlaneTypes(): Observable<List<PlaneType>> {
        return fromCallable { repository.loadPlaneTypes().map { it.toPlaneType() } }
    }

    fun loadPlaneType(id: Long?): Observable<OptionalNull<PlaneType?>> {
        return fromNullable { repository.loadPlaneType(id)?.toPlaneType() }
    }

    fun loadFlightType(id: Long?): Observable<OptionalNull<FlightType?>> {
        return fromNullable { repository.loadDBFlightType(id)?.toFlightType() }
    }

    fun loadDBFlightsToTimes(): Observable<List<TimeToFlightEntity>> {
        return fromCallable { repository.queryDBFlightsTimes() }
    }

    fun loadDBFlightToTimes(flightId: Long?): Observable<List<TimeToFlightEntity>> {
        return fromCallable { repository.queryDBFlightTimes(flightId) }
    }

    fun insertDBFlightToTime(entity: TimeToFlightEntity): Observable<Boolean> {
        return fromCallable { repository.insertDBFlightTime(entity) }
    }

    fun insertDBFlightToTimes(entities: List<TimeToFlightEntity>): Observable<Boolean> {
        return fromCallable { repository.insertDBFlightTimes(entities) }
    }

    fun updateDBFlightToTime(flightId: Long?, timeType: Long?, time: Int, addToFlight: Boolean = false): Observable<Boolean> {
        return fromCallable { repository.updateDBFlightTime(flightId, timeType, time, addToFlight) }
    }

    fun removeDBFlightToTime(flightId: Long?, timeType: Long?): Observable<Boolean> {
        return fromCallable { repository.removeDBFlightTime(flightId, timeType) }
    }

    private fun getFormattedFlightTimes(): String {
        val flightsTime = repository.getFlightsTime()
        val totalTimes = repository.queryDBFlightsTimesSum(false)
        val sumlogTime = flightsTime + totalTimes
        val totalFlightTimes = repository.queryDBFlightsTimesSum(true)
        val sumFlightTime = flightsTime + totalFlightTimes
        val flightsCount = repository.getFlightsCount()
        return String.format("%s %s\n%s %s\n%s %d",
                repository.getString(R.string.str_total_time),
                DateTimeUtils.strLogTime(sumlogTime),
                repository.getString(R.string.str_total_flight_time),
                DateTimeUtils.strLogTime(sumFlightTime),
                repository.getString(R.string.total_records),
                flightsCount)
    }

    fun getTotalflightsTimeInfo(): Observable<String> {
        return fromCallable { getFormattedFlightTimes() }
    }

    private fun getPrefOrderflights(filtertype: Int): String = when (filtertype) {
        0 -> CONSTS.DB.COLUMN_DATETIME + " ASC"
        1 -> CONSTS.DB.COLUMN_DATETIME + " DESC"
        3 -> CONSTS.DB.COLUMN_LOG_TIME + " DESC"
        2 -> CONSTS.DB.COLUMN_LOG_TIME + " ASC"
        else -> CONSTS.DB.COLUMN_DATETIME + " ASC"
    }


    fun getFilterflights(): Observable<List<Flight>> {
        return fromCallable {
            val order = getPrefOrderflights(repository.getPrefInt(CONSTS.PREFS.PREF_USER_FILTER_FLIGHTS))
            repository.getDbFlights(order)
                    .map {
                        it.airplanetypetitle = repository.loadDBFlightType(it.aircraft_id)?.typeTitle
                        it
                    }
                    .map { it.toFlight() }
        }
    }

    fun removeFlight(id: Long?): Observable<Boolean> {
        return fromCallable { repository.removeFlight(id) }
    }
}