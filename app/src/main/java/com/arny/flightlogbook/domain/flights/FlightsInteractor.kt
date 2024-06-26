package com.arny.flightlogbook.domain.flights

import android.graphics.Color
import com.arny.flightlogbook.R
import com.arny.flightlogbook.data.CONSTS.STRINGS.PARAM_COLOR
import com.arny.flightlogbook.data.models.CustomFieldType
import com.arny.flightlogbook.data.models.CustomFieldValue
import com.arny.flightlogbook.data.models.Flight
import com.arny.flightlogbook.data.models.FlightType
import com.arny.flightlogbook.data.models.PlaneType
import com.arny.flightlogbook.data.models.AppResult
import com.arny.flightlogbook.data.models.toResult
import com.arny.flightlogbook.data.utils.DateTimeUtils
import com.arny.flightlogbook.data.utils.colorWillBeMasked
import com.arny.flightlogbook.data.utils.toIntColor
import com.arny.flightlogbook.domain.airports.IAirportsRepository
import com.arny.flightlogbook.domain.common.IPreferencesInteractor
import com.arny.flightlogbook.domain.common.IResourceProvider
import com.arny.flightlogbook.domain.customfields.ICustomFieldsRepository
import com.arny.flightlogbook.domain.files.FilesRepository
import com.arny.flightlogbook.domain.flighttypes.FlightTypesRepository
import com.arny.flightlogbook.domain.models.BusinessException
import com.arny.flightlogbook.domain.planetypes.AircraftTypesRepository
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlightsInteractor @Inject constructor(
    private val flightTypesRepository: FlightTypesRepository,
    private val flightsRepository: FlightsRepository,
    private val resourcesProvider: IResourceProvider,
    private val aircraftTypesRepository: AircraftTypesRepository,
    private val customFieldsRepository: ICustomFieldsRepository,
    private val prefsInteractor: IPreferencesInteractor,
    private val filesRepository: FilesRepository,
    private val airportsRepository: IAirportsRepository,
) {
    fun updateFlight(flight: Flight): Boolean = flightsRepository.updateFlight(flight)

    fun insertFlightAndGet(flight: Flight): Long = flightsRepository.insertFlightAndGet(flight)

    fun getFlight(id: Long?): Flight? = flightsRepository.getFlight(id)
        ?.apply {
            this.customParams = customParams ?: hashMapOf()
            colorInt = customParams?.get(PARAM_COLOR)?.toString()?.toIntColor()
            planeType = aircraftTypesRepository.loadAircraftType(planeId)
            flightType = flightTypesRepository.loadDBFlightType(flightTypeId)
        }

    fun loadPlaneType(id: Long?): PlaneType? = aircraftTypesRepository.loadAircraftType(id)

    fun loadPlaneTypeByRegNo(regNo: String?): PlaneType? =
        aircraftTypesRepository.loadPlaneTypeByRegNo(regNo)

    fun loadFlightType(id: Long?): FlightType? = flightTypesRepository.loadDBFlightType(id)

    fun getAddTimeSum(values: List<CustomFieldValue>): Int = if (values.isNotEmpty()) {
        values.filter {
            val type = it.field?.type
            type is CustomFieldType.Time && type.addTime && it.value != null
        }.sumOf {
            DateTimeUtils.convertStringToTime(it.value.toString())
        }
    } else {
        0
    }

    private fun getFormattedFlightTimes(): AppResult<String> {
        val flightsCount = flightsRepository.getFlightsCount()
        if (flightsCount == 0) {
            return BusinessException("Flights not found").toResult()
        }
        val flightsTime = flightsRepository.getFlightsTime()
        val groundTime = flightsRepository.getGroundTime()
        val nightTime = flightsRepository.getNightTime()
        val addTimeSum = getAddTimeSum(customFieldsRepository.getAllAdditionalTime())
        val sumlogTime = flightsTime + groundTime + addTimeSum
        return formattedInfo(flightsTime, nightTime, groundTime, sumlogTime, flightsCount)
    }

    private fun formattedInfo(
        flightsTime: Int,
        nightTime: Int,
        groundTime: Int,
        sumlogTime: Int,
        flightsCount: Int
    ): AppResult<String> {
        return String.format(
            Locale.getDefault(),
            "%s %s\n%s %s\n%s %s\n%s %s\n%s %d",
            resourcesProvider.getString(R.string.str_total_flight_time),
            DateTimeUtils.strLogTime(flightsTime),
            resourcesProvider.getString(R.string.stat_total_night_time),
            DateTimeUtils.strLogTime(nightTime),
            resourcesProvider.getString(R.string.cell_ground_time) + ":",
            DateTimeUtils.strLogTime(groundTime),
            resourcesProvider.getString(R.string.str_total_time),
            DateTimeUtils.strLogTime(sumlogTime),
            resourcesProvider.getString(R.string.total_records),
            flightsCount
        ).toResult()
    }

    fun getTotalFlightsTimeInfo(): AppResult<String> = getFormattedFlightTimes()

    fun setFlightsOrder(orderType: Int) {
        prefsInteractor.setOrderType(orderType)
    }

    fun loadDBFlights(): List<Flight> {
        return flightsRepository.getDbFlights()
            .map { flight ->
                flight.planeType = aircraftTypesRepository.loadAircraftType(flight.planeId)
                flight.flightType =
                    flightTypesRepository.loadDBFlightType(flight.flightTypeId?.toLong())
                flight.totalTime = flight.flightTime + flight.groundTime
                flight
            }
    }

    private fun getDbFlightsObs(checkAutoExport: Boolean = false): Observable<AppResult<List<Flight>>> {
        return flightsRepository.getDbFlightsOrdered()
            .doOnNext {
                if (checkAutoExport) {
                    if (it is AppResult.Success) {
                        filesRepository.saveDataToFile(it.data)
                    }
                }
            }
    }

    fun getFilterFlightsObs(checkAutoExport: Boolean = false): Observable<AppResult<List<Flight>>> {
        val orderType = prefsInteractor.getFlightsOrderType()
        return getDbFlightsObs(checkAutoExport)
            .flatMap { flightsResult ->
                Observable.fromCallable {
                    val flightTypes = flightTypesRepository.loadDBFlightTypes()
                    val planeTypes = aircraftTypesRepository.loadAircraftTypes()
                    val allAdditionalTime = customFieldsRepository.getAllAdditionalTime()
                    when (flightsResult) {
                        is AppResult.Success -> getFlight(
                            list = flightsResult.data,
                            planeTypes = planeTypes,
                            flightTypes = flightTypes,
                            allAdditionalTime = allAdditionalTime
                        )

                        is AppResult.Error -> throw BusinessException(flightsResult.exception)
                    }
                }
            }
            .map { flights ->
                when (orderType) {
                    0 -> flights.sortedBy { it.datetime }
                    1 -> flights.sortedByDescending { it.datetime }
                    2 -> flights.sortedBy { it.flightTime }
                    3 -> flights.sortedByDescending { it.flightTime }
                    else -> flights
                }.toResult()
            }
    }

    private fun getFlight(
        list: List<Flight>,
        planeTypes: List<PlaneType>,
        flightTypes: List<FlightType>,
        allAdditionalTime: List<CustomFieldValue>
    ): List<Flight> = list.map { flight ->
        flight.colorInt = flight.customParams?.get(PARAM_COLOR)?.toString()?.toIntColor()
        val masked = flight.colorInt?.let { colorWillBeMasked(it) } ?: false
        flight.colorText = if (masked) Color.WHITE else null
        flight.planeType = flight.planeId?.let { plId ->
            planeTypes.find { it.typeId == plId }
                ?: flight.regNo?.let { regNo ->
                    planeTypes.find { it.regNo?.trimIndent() == regNo.trimIndent() }
                }
        }
        flight.regNo = flight.planeType?.regNo
        flight.flightType = flightTypes.find { it.id == flight.flightTypeId }
        val flightAddTime = allAdditionalTime.firstOrNull { it.externalId == flight.id }
            ?.value?.toString()
            ?.toInt() ?: 0
        flight.flightTime += flightAddTime
        flight.totalTime = flight.flightTime + flight.groundTime
        flight.departure = airportsRepository.getAirport(flight.departureId)
        flight.arrival = airportsRepository.getAirport(flight.arrivalId)
        flight
    }

    fun removeFlight(id: Long?): Single<Boolean> = Single.fromCallable {
        flightsRepository.removeFlight(id)
    }.observeOn(Schedulers.io())

    fun removeFlights(selectedIds: List<Long>): Single<Boolean> =
        Single.fromCallable { flightsRepository.removeFlights(selectedIds) }
            .subscribeOn(Schedulers.io())
}
