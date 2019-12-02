package com.arny.domain.statistic

import com.arny.data.models.FlightEntity
import com.arny.data.repositories.MainRepositoryImpl
import com.arny.domain.models.*
import com.arny.helpers.utils.DateTimeUtils
import com.arny.helpers.utils.fromCallable
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

/**
 *Created by Sedoy on 27.08.2019
 */
@Singleton
class StatisticUseCase @Inject constructor(private val repository: MainRepositoryImpl) {
    fun loadDBFlights(startDate: Long, endDate: Long, extendedStatistic: Boolean, includeEnd: Boolean): Observable<ArrayList<Statistic>> {
        return returnStatistic(extendedStatistic, fromCallable { repository.getStatisticDbFlights(startDate, endDate, includeEnd) })
    }

    fun loadDBFlightsByTimes(startdatetime: Long, enddatetime: Long, extendedStatistic: Boolean, filterSelection: List<Long?>, includeEnd: Boolean): Observable<ArrayList<Statistic>> {
        val filghtList = toFilghtList(fromCallable { repository.getStatisticDbFlights(startdatetime, enddatetime, includeEnd) })
        return toStatisticList(extendedStatistic, filghtList)
    }

    fun loadFilteredFlightsByPlaneTypes(types: List<Long?>, startdatetime: Long, enddatetime: Long, extendedStatistic: Boolean, includeEnd: Boolean): Observable<ArrayList<Statistic>> {
        return returnStatistic(extendedStatistic, fromCallable { repository.getStatisticDbFlightsByPlanes(startdatetime, enddatetime, types, includeEnd) })
    }

    fun loadFilteredFlightsByFlightTypes(startdatetime: Long, enddatetime: Long, extendedStatistic: Boolean, types: List<Long?>, includeEnd: Boolean): Observable<ArrayList<Statistic>> {
        return returnStatistic(extendedStatistic, fromCallable { repository.getStatisticDbFlightsByFlightTypes(startdatetime, enddatetime, types, includeEnd) })
    }

    private fun returnStatistic(extendedStatistic: Boolean, observable: Observable<ArrayList<FlightEntity>>): Observable<ArrayList<Statistic>> {
        return toStatisticList(extendedStatistic, toFilghtList(observable))
    }

    fun getFightsMinMaxDateTimes(): Observable<Pair<Long, Long>> {
        return fromCallable { repository.getStatisticDbFlightsMinMax() }
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

    private fun toFilghtList(observable: Observable<ArrayList<FlightEntity>>): Observable<List<Flight>> {
        return observable
                .map { list ->
                    list.map { it.toFlight() }
                            .map { flight ->
                                val planeType = repository.loadPlaneType(flight.planeId)
                                flight.planeType = planeType?.toPlaneType()
                                flight.flightType = repository.loadDBFlightType(flight.flightTypeId?.toLong())?.toFlightType()
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
        builder.append(DateTimeUtils.strLogTime(list.sumBy {
            it.sumFlightTime ?: 0
        })).append("<br>")
        builder.append("<b>Общее время на земле:</b>")
        builder.append(DateTimeUtils.strLogTime(list.sumBy {
            it.sumGroundTime ?: 0
        })).append("<br>")
        builder.append("<b>Общее время:</b>")
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
            builder.append("<b>Время летное:</b>").append(DateTimeUtils.strLogTime(flight.sumFlightTime)).append("<br>")
            builder.append("Налет:").append(DateTimeUtils.strLogTime(flight.flightTime)).append("<br>")
            builder.append("<b>Время на земле:</b>").append(DateTimeUtils.strLogTime(flight.sumGroundTime)).append("<br>")
            builder.append("<b>Время общее:</b>").append(DateTimeUtils.strLogTime(flight.sumGroundTime + flight.sumFlightTime)).append("<br>")
            builder.append("<b>Тип ВС:</b>").append(flight.planeType?.typeName
                    ?: "-").append("<br>")
            builder.append("<b>Тип полета:</b>").append(flight.flightType?.typeTitle ?: "-")
            statistic.data = builder.toString()
            stats.add(statistic)
        }
        return stats
    }

    fun loadPlaneTypes(): Observable<List<PlaneType>> {
        return fromCallable { repository.loadPlaneTypes().map { it.toPlaneType() } }
    }

    fun loadTimeTypes(): Observable<List<TimeType>> {
        return fromCallable { repository.queryDBTimeTypes().map { it.toTimeType() } }
    }

    fun loadFlightTypes(): Observable<List<FlightType>> {
        return fromCallable { repository.loadDBFlightTypes().map { it.toFlightType() } }
    }

    fun loadPlanesRegNums(): Observable<List<String>> {
        return fromCallable { repository.getDbFlights() }
                .map { flts -> flts.filter { it.regNo.isNullOrBlank() } }
                .map { list -> list.map { it.regNo!! } }
    }

}