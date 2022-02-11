package com.arny.domain.flights

import android.graphics.Color
import com.arny.core.CONSTS.STRINGS.PARAM_COLOR
import com.arny.core.utils.*
import com.arny.domain.R
import com.arny.domain.common.IPreferencesInteractor
import com.arny.domain.common.IResourceProvider
import com.arny.domain.files.FilesRepository
import com.arny.domain.flighttypes.FlightTypesRepository
import com.arny.domain.models.*
import com.arny.domain.planetypes.AircraftTypesRepository
import com.arny.flightlogbook.customfields.models.CustomFieldType
import com.arny.flightlogbook.customfields.models.CustomFieldValue
import com.arny.flightlogbook.customfields.repository.ICustomFieldsRepository
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
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
    private val filesRepository: FilesRepository
) {
    fun updateFlight(flight: Flight): Boolean = flightsRepository.updateFlight(flight)

    fun insertFlightAndGet(flight: Flight): Long = flightsRepository.insertFlightAndGet(flight)

    fun getFlight(id: Long?): Flight? {
        return flightsRepository.getFlight(id)
            ?.apply {
                colorInt = params?.getParam(PARAM_COLOR, "")?.toIntColor()
                planeType = aircraftTypesRepository.loadAircraftType(planeId)
                flightType = flightTypesRepository.loadDBFlightType(flightTypeId?.toLong())
            }
    }

    fun loadPlaneType(id: Long?): PlaneType? {
        return aircraftTypesRepository.loadAircraftType(id)
    }

    fun loadPlaneTypeByRegNo(regNo: String?): PlaneType? {
        return aircraftTypesRepository.loadPlaneTypeByRegNo(regNo)
    }

    fun loadFlightType(id: Long?): FlightType? {
        return flightTypesRepository.loadDBFlightType(id)
    }

    fun getAddTimeSum(values: List<CustomFieldValue>): Int {
        return if (values.isNotEmpty()) {
            values.filter {
                val type = it.type
                type is CustomFieldType.Time && type.addTime && it.value != null
            }.map {
                DateTimeUtils.convertStringToTime(it.value.toString())
            }.sum()
        } else {
            0
        }
    }

    private fun getFormattedFlightTimes(): Result<String> {
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
    ): Result<String> {
        return String.format(
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

    fun getTotalflightsTimeInfo(): Result<String> = getFormattedFlightTimes()

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

    private fun getDbFlightsObs(checkAutoExport: Boolean = false): Observable<Result<List<Flight>>> {
        return flightsRepository.getDbFlightsOrdered()
            .doOnNext {
                if (checkAutoExport && prefsInteractor.isAutoImportEnabled()) {
                    if (it is Result.Success) {
                        filesRepository.saveExcelFile(it.data, prefsInteractor.getSavedExportPath())
                    }
                }
            }
    }

    fun getFilterFlightsObs(checkAutoExport: Boolean = false): Observable<Result<List<Flight>>> {
        val orderType = prefsInteractor.getFlightsOrderType()
        return getDbFlightsObs(checkAutoExport)
            .flatMap { flightsResult ->
                fromCallable {
                    val flightTypes = flightTypesRepository.loadDBFlightTypes()
                    val planeTypes = aircraftTypesRepository.loadAircraftTypes()
                    val allAdditionalTime = customFieldsRepository.getAllAdditionalTime()
                    when (flightsResult) {
                        is Result.Success -> getFlight(
                            flightsResult.data,
                            planeTypes,
                            flightTypes,
                            allAdditionalTime
                        )
                        is Result.Error -> throw BusinessException(flightsResult.exception)
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
    ): List<Flight> {
        return list.map { flight ->
            flight.colorInt = flight.params?.getParam(PARAM_COLOR, "")?.toIntColor()
            val masked = flight.colorInt?.let { colorWillBeMasked(it) } ?: false
            flight.colorText = if (masked) Color.WHITE else null
            flight.planeType = flight.planeId?.let { plId ->
                planeTypes.find { it.typeId == plId }
                    ?: flight.regNo?.let { regNo ->
                        planeTypes.find { it.regNo?.trimIndent() == regNo.trimIndent() }
                    }
            }
            flight.flightType = flightTypes.find { it.id == flight.flightTypeId?.toLong() }
            val flightAddTime = allAdditionalTime.firstOrNull { it.externalId == flight.id }
                ?.value?.toString()
                ?.toInt() ?: 0
            flight.flightTime += flightAddTime
            flight.totalTime = flight.flightTime + flight.groundTime
            flight
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
