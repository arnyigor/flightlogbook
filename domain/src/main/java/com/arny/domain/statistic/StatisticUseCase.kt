package com.arny.domain.statistic

import com.arny.domain.flights.FlightsRepository
import com.arny.domain.flighttypes.FlightTypesRepository
import com.arny.domain.models.Flight
import com.arny.domain.models.FlightType
import com.arny.domain.models.PlaneType
import com.arny.domain.models.Statistic
import com.arny.domain.planetypes.PlaneTypesRepository
import com.arny.helpers.utils.DateTimeUtils
import com.arny.helpers.utils.fromCallable
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

/**
 *Created by Sedoy on 27.08.2019
 */
@Singleton
class StatisticUseCase @Inject constructor(
        private val flightsRepository: FlightsRepository,
        private val planeTypesRepository: PlaneTypesRepository,
        private val flightTypesRepository: FlightTypesRepository
) {
    fun loadDBFlights(startDate: Long, endDate: Long, extendedStatistic: Boolean, includeEnd: Boolean): Observable<ArrayList<Statistic>> {
        return returnStatistic(extendedStatistic, fromCallable { flightsRepository.getStatisticDbFlights(startDate, endDate, includeEnd) })
    }

    fun loadDBFlightsByTimes(startdatetime: Long, enddatetime: Long, extendedStatistic: Boolean, filterSelection: List<Long?>, includeEnd: Boolean): Observable<ArrayList<Statistic>> {
        val filghtList = toFilghtList(fromCallable { flightsRepository.getStatisticDbFlights(startdatetime, enddatetime, includeEnd) })
        return toStatisticList(extendedStatistic, filghtList)
    }

    fun loadFilteredFlightsByPlaneTypes(types: List<Long?>, startdatetime: Long, enddatetime: Long, extendedStatistic: Boolean, includeEnd: Boolean): Observable<ArrayList<Statistic>> {
        return returnStatistic(extendedStatistic, fromCallable { flightsRepository.getStatisticDbFlightsByPlanes(startdatetime, enddatetime, types, includeEnd) })
    }

    fun loadFilteredFlightsByFlightTypes(startdatetime: Long, enddatetime: Long, extendedStatistic: Boolean, types: List<Long?>, includeEnd: Boolean): Observable<ArrayList<Statistic>> {
        return returnStatistic(extendedStatistic, fromCallable { flightsRepository.getStatisticDbFlightsByFlightTypes(startdatetime, enddatetime, types, includeEnd) })
    }

    private fun returnStatistic(extendedStatistic: Boolean, observable: Observable<List<Flight>>): Observable<ArrayList<Statistic>> {
        return toStatisticList(extendedStatistic, toFilghtList(observable))
    }

    fun getFightsMinMaxDateTimes(): Observable<Pair<Long, Long>> {
        return fromCallable { flightsRepository.getStatisticDbFlightsMinMax() }
    }

    private fun toStatisticList(extendedStatistic: Boolean, observable: Observable<List<Flight>>): Observable<ArrayList<Statistic>> {
        return observable
                .map { list ->
                    if (list.isNotEmpty()) {
                        if (extendedStatistic) {
                            val stats = fightsStringStatisticBuilder(list)
                            if (list.size > 1) {
                                stats.add(totalSumTimes(list))
                            }
                            stats
                        } else {
                            arrayListOf(totalSumTimes(list))
                        }
                    } else {
                        arrayListOf()
                    }
                }
    }

    private fun toFilghtList(observable: Observable<List<Flight>>): Observable<List<Flight>> {
        return observable
                .map { list ->
                    list.map { flight ->
                        flight.planeType = planeTypesRepository.loadPlaneType(flight.planeId)
                        flight.flightType = flightTypesRepository.loadDBFlightType(flight.flightTypeId?.toLong())
                        flight
                    }.sortedBy { it.datetime }
                }
    }

    private fun totalSumTimes(list: List<Flight>): Statistic {
        val builder = StringBuilder()
        val statistic = Statistic(type = 1)
        statistic.dateTimeStart = list.first().datetimeFormatted
        statistic.dateTimeEnd = list.last().datetimeFormatted
        builder.append("<b>Всего полетов:</b>")
        builder.append(list.size).append("<br>")
        builder.append("<b>Общее летное время:</b>")
        val sumFlightTime = list.sumBy { it.totalTime }
        builder.append(getTime(sumFlightTime)).append("<br>")
        builder.append("<b>Общее ночное время:</b>")
        val sumNightFlightTime = list.sumBy { it.nightTime }
        builder.append(getTime(sumNightFlightTime)).append("<br>")
        builder.append("<b>Общее время на земле:</b>")
        val sumGroundTime = list.sumBy { it.groundTime }
        builder.append(getTime(sumGroundTime)).append("<br>")
        builder.append("<b>Общее время:</b>")
        val totalTime = list.sumBy { it.groundTime + it.flightTime }
        builder.append(getTime(totalTime)).append("<br>")
        statistic.data = builder.toString()
        return statistic
    }

    private fun fightsStringStatisticBuilder(list: List<Flight>): ArrayList<Statistic> {
        val stats = arrayListOf<Statistic>()
        for (flightInd in list.withIndex()) {
            val builder = StringBuilder()
            val statistic = Statistic()
            val flight = flightInd.value
            statistic.dateTimeStart = flight.datetimeFormatted
            builder.append("Налет:").append(getTime(flight.flightTime)).append("<br>")
            builder.append("<b>Время на земле:</b>").append(getTime(flight.groundTime)).append("<br>")
            val totalTime = flight.flightTime + flight.groundTime
            builder.append("<b>Время общее:</b>").append(getTime(totalTime)).append("<br>")
            builder.append("<b>Тип ВС:</b>").append(flight.planeType?.typeName
                    ?: "-").append("<br>")
            builder.append("<b>Тип полета:</b>").append(flight.flightType?.typeTitle ?: "-")
            statistic.data = builder.toString()
            stats.add(statistic)
        }
        return stats
    }

    private fun getTime(time: Int) = DateTimeUtils.strLogTime(time)

    fun loadPlaneTypes(): Observable<List<PlaneType>> {
        return fromCallable { planeTypesRepository.loadPlaneTypes() }
    }

    fun loadFlightTypes(): Observable<List<FlightType>> {
        return fromCallable { flightTypesRepository.loadDBFlightTypes() }
    }

    fun loadPlanesRegNums(): Observable<List<String>> {
        return fromCallable { flightsRepository.getDbFlights() }
                .map { flts -> flts.filter { it.regNo.isNullOrBlank() } }
                .map { list -> list.map { it.regNo!! } }
    }

}