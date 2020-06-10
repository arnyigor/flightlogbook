package com.arny.domain.statistic

import com.arny.constants.CONSTS.STRINGS.PARAM_COLOR
import com.arny.domain.R
import com.arny.domain.common.ResourcesProvider
import com.arny.domain.flights.FlightsRepository
import com.arny.domain.flighttypes.FlightTypesRepository
import com.arny.domain.models.Flight
import com.arny.domain.models.FlightType
import com.arny.domain.models.PlaneType
import com.arny.domain.models.Statistic
import com.arny.domain.planetypes.PlaneTypesRepository
import com.arny.helpers.utils.DateTimeUtils
import com.arny.helpers.utils.fromCallable
import com.arny.helpers.utils.toHexColor
import com.arny.helpers.utils.toIntColorsArray
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatisticInteractor @Inject constructor(
        private val flightsRepository: FlightsRepository,
        private val planeTypesRepository: PlaneTypesRepository,
        private val flightTypesRepository: FlightTypesRepository,
        private val resourcesProvider: ResourcesProvider
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

    fun loadFilteredFlightsByColor(color: Int, startdatetime: Long, enddatetime: Long, extendedStatistic: Boolean, includeEnd: Boolean): Observable<ArrayList<Statistic>> {
        return returnStatistic(extendedStatistic, fromCallable {
            val query = "%\"$PARAM_COLOR\":\"${color.toHexColor()}\"%"
            flightsRepository.getStatisticDbFlightsByColor(startdatetime, enddatetime, includeEnd, query)
        })
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
        builder.append("<b>${resourcesProvider.getString(R.string.stat_total_flights)}:</b>")
        builder.append(list.size).append("<br>")
        builder.append("<b>${resourcesProvider.getString(R.string.stat_total_flight_time)}:</b>")
        val sumFlightTime = list.sumBy { it.totalTime }
        builder.append(getTime(sumFlightTime)).append("<br>")
        builder.append("<b>${resourcesProvider.getString(R.string.stat_total_night_time)}:</b>")
        val sumNightFlightTime = list.sumBy { it.nightTime }
        builder.append(getTime(sumNightFlightTime)).append("<br>")
        builder.append("<b>${resourcesProvider.getString(R.string.stat_total_ground_time)}:</b>")
        val sumGroundTime = list.sumBy { it.groundTime }
        builder.append(getTime(sumGroundTime)).append("<br>")
        builder.append("<b>${resourcesProvider.getString(R.string.stat_total_time)}:</b>")
        val totalTime = list.sumBy { it.groundTime + it.flightTime }
        builder.append(getTime(totalTime)).append("<br>")
        statistic.data = builder.toString()
        return statistic
    }

    private fun fightsStringStatisticBuilder(list: List<Flight>): ArrayList<Statistic> {
        val stats = arrayListOf<Statistic>()
        for (flightInd in list.withIndex()) {
            val statistic = Statistic()
            val flight = flightInd.value
            statistic.dateTimeStart = flight.datetimeFormatted
            val totalTime = flight.flightTime + flight.groundTime
            val builder = StringBuilder().apply {
                append("<b>${resourcesProvider.getString(R.string.stat_total_flight_time)}:</b>")
                append(getTime(flight.flightTime))
                append("<br>")
                append("<b>${resourcesProvider.getString(R.string.stat_total_night_time)}:</b>")
                append(getTime(flight.nightTime))
                append("<br>")
                append("<b>${resourcesProvider.getString(R.string.cell_ground_time)}:</b>")
                append(getTime(flight.groundTime))
                append("<br>")
                append("<b>${resourcesProvider.getString(R.string.stat_total_time)}:</b>")
                append(getTime(totalTime))
                append("<br>")
                append("<b>${resourcesProvider.getString(R.string.str_type)}:</b>")
                append(flight.planeType?.typeName ?: "-")
                append("<br>")
                append("<b>${resourcesProvider.getString(R.string.stat_flight_type)}:</b>")
                append(flight.flightType?.typeTitle ?: "-")
            }
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

    fun loadColors(): Single<IntArray> {
        return flightsRepository.getNotEmptyColors()
                .map { it.toIntColorsArray() }
    }
}
