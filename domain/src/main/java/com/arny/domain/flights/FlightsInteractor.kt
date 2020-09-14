package com.arny.domain.flights

import android.graphics.Color
import com.arny.domain.R
import com.arny.domain.common.PreferencesProvider
import com.arny.domain.common.ResourcesProvider
import com.arny.domain.files.FilesRepository
import com.arny.domain.flighttypes.FlightTypesRepository
import com.arny.domain.models.*
import com.arny.domain.planetypes.PlaneTypesRepository
import com.arny.flightlogbook.constants.CONSTS
import com.arny.flightlogbook.constants.CONSTS.STRINGS.PARAM_COLOR
import com.arny.helpers.utils.*
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlightsInteractor @Inject constructor(
        private val flightTypesRepository: FlightTypesRepository,
        private val flightsRepository: FlightsRepository,
        private val resourcesProvider: ResourcesProvider,
        private val planeTypesRepository: PlaneTypesRepository,
        private val preferencesProvider: PreferencesProvider,
        private val filesRepository: FilesRepository
) {

    fun updateFlight(flight: Flight): Single<Boolean> {
        return fromSingle { flightsRepository.updateFlight(flight) }
    }

    fun insertFlightAndGet(flight: Flight): Single<Long> {
        return fromSingle { flightsRepository.insertFlightAndGet(flight) }
    }

    fun getFlight(id: Long?): Flight? {
        return flightsRepository.getFlight(id)
                ?.apply {
                    colorInt = params?.getParam(PARAM_COLOR, "")?.toIntColor()
                    planeType = planeTypesRepository.loadPlaneType(planeId)
                    flightType = flightTypesRepository.loadDBFlightType(flightTypeId?.toLong())
                }
    }

    fun loadPlaneType(id: Long?): PlaneType? {
        return planeTypesRepository.loadPlaneType(id)
    }

    fun loadFlightType(id: Long?): FlightType? {
        return flightTypesRepository.loadDBFlightType(id)
    }

    private fun getFormattedFlightTimes(): Result<String> {
        val flightsCount = flightsRepository.getFlightsCount()
        if (flightsCount == 0) {
            return BusinessException("Flights not found").toResult()
        }
        val flightsTime = flightsRepository.getFlightsTime()
        val groundTime = flightsRepository.getGroundTime()
        val nightTime = flightsRepository.getNightTime()
        val sumlogTime = flightsTime + groundTime
        return formattedInfo(flightsTime, nightTime, groundTime, sumlogTime, flightsCount)
    }

    private fun formattedInfo(
            flightsTime: Int,
            nightTime: Int,
            groundTime: Int,
            sumlogTime: Int,
            flightsCount: Int
    ): Result<String> {
        return String.format("%s %s\n%s %s\n%s %s\n%s %s\n%s %d",
                resourcesProvider.getString(R.string.str_total_flight_time),
                DateTimeUtils.strLogTime(flightsTime),
                resourcesProvider.getString(R.string.stat_total_night_time),
                DateTimeUtils.strLogTime(nightTime),
                resourcesProvider.getString(R.string.cell_ground_time) + ":",
                DateTimeUtils.strLogTime(groundTime),
                resourcesProvider.getString(R.string.str_total_time),
                DateTimeUtils.strLogTime(sumlogTime),
                resourcesProvider.getString(R.string.total_records),
                flightsCount).toResult()
    }

    fun getTotalflightsTimeInfo(): Observable<Result<String>> {
        return fromCallable { getFormattedFlightTimes() }
    }

    fun setFlightsOrder(orderType: Int) {
        preferencesProvider.setPrefInt(CONSTS.PREFS.PREF_USER_FILTER_FLIGHTS, orderType)
    }

    fun loadDBFlights(): List<Flight> {
        return flightsRepository.getDbFlights()
                .map { flight ->
                    flight.planeType = planeTypesRepository.loadPlaneType(flight.planeId)
                    flight.flightType =
                            flightTypesRepository.loadDBFlightType(flight.flightTypeId?.toLong())
                    flight.totalTime = flight.flightTime + flight.groundTime
                    flight
                }
    }

    private fun getDbFlightsObs(checkAutoExport: Boolean = false): Observable<Result<List<Flight>>> {
        return flightsRepository.getDbFlightsOrdered()
                .doOnNext {
                    if (checkAutoExport && preferencesProvider.getPrefBoolean(CONSTS.PREFS.AUTO_EXPORT_XLS, false)) {
                        if (it is Result.Success) {
                            filesRepository.saveExcelFile(it.data)
                        }
                    }
                }
    }

    fun getFilterFlightsObs(checkAutoExport: Boolean = false): Observable<Result<List<Flight>>> {
        val orderType = preferencesProvider.getPrefInt(CONSTS.PREFS.PREF_USER_FILTER_FLIGHTS)
        return getDbFlightsObs(checkAutoExport)
                .flatMap { flightsResult ->
                    fromCallable {
                        val flightTypes = flightTypesRepository.loadDBFlightTypes()
                        val planeTypes = planeTypesRepository.loadPlaneTypes()
                        when (flightsResult) {
                            is Result.Success -> {
                                flightsResult.data.map { flight ->
                                    flight.colorInt = flight.params?.getParam(PARAM_COLOR, "")?.toIntColor()
                                    val masked = flight.colorInt?.let { colorWillBeMasked(it) }
                                            ?: false
                                    flight.colorText = if (masked) Color.WHITE else null
                                    flight.planeType = planeTypes.find { it.typeId == flight.planeId }
                                    flight.flightType = flightTypes.find { it.id == flight.flightTypeId?.toLong() }
                                    flight.totalTime = flight.flightTime + flight.groundTime
                                    flight
                                }
                            }
                            is Result.Error -> throw BusinessException(flightsResult.exception)
                        }
                    }
                }
                .map { flights ->
                    val res = when (orderType) {
                        0 -> flights.sortedBy { it.datetime }
                        1 -> flights.sortedByDescending { it.datetime }
                        2 -> flights.sortedBy { it.flightTime }
                        3 -> flights.sortedByDescending { it.flightTime }
                        else -> flights
                    }
                    res.toResult()
                }
    }

    fun removeFlight(id: Long?): Single<Boolean> {
        return fromSingle { flightsRepository.removeFlight(id) }
    }

    fun removeFlights(selectedIds: List<Long>): Single<Boolean> {
        return fromSingle { flightsRepository.removeFlights(selectedIds) }
                .subscribeOn(Schedulers.io())
    }
}
