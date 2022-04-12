package com.arny.flightlogbook.presentation.statistic.presenter

import android.graphics.Color
import com.arny.core.utils.*
import com.arny.flightlogbook.R
import com.arny.flightlogbook.domain.common.ResourcesInteractor
import com.arny.flightlogbook.domain.models.FilterType
import com.arny.flightlogbook.domain.models.Statistic
import com.arny.flightlogbook.domain.models.StatisticFilter
import com.arny.flightlogbook.domain.statistic.StatisticInteractor
import com.arny.flightlogbook.presentation.common.BaseMvpPresenter
import com.arny.flightlogbook.presentation.statistic.view.StatisticsView
import io.reactivex.Observable
import moxy.InjectViewState
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.util.*
import javax.inject.Inject

@InjectViewState
class StatisticsPresenter @Inject constructor(
    private val statisticInteractor: StatisticInteractor,
    private val resourcesInteractor: ResourcesInteractor
) : BaseMvpPresenter<StatisticsView>() {
    private var color: Int = Color.BLACK
    private var filterList = listOf<StatisticFilter>()
    private var filterType = FilterType.AIRCRAFT_NAME
    private var longsSelection = mutableListOf<Long>()
    private var stringsSelection = mutableListOf<String>()
    private var extendedStatistic = false
    private var enableFilter = false
    private var colors: IntArray? = null

    @Volatile
    private var currentPeriodType: PeriodType = PeriodType.ALL
    private val dateAndTimeStart = GregorianCalendar.getInstance()
    private val dateAndTimeEnd = GregorianCalendar.getInstance()

    @Volatile
    private var startDateTime: Long = dateAndTimeStart.timeInMillis

    @Volatile
    private var endDateTime: Long = dateAndTimeEnd.timeInMillis

    private fun getPeriod(position: Int): PeriodType {
        return when (position) {
            0 -> PeriodType.ALL
            1 -> PeriodType.DAY
            2 -> PeriodType.MONTH
            3 -> PeriodType.YEAR
            4 -> PeriodType.CUSTOM
            else -> PeriodType.ALL
        }
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        statisticInteractor.loadColors()
            .subscribeFromPresenter({ colors = it })
    }

    fun onPeriodChanged(position: Int) {
        currentPeriodType = getPeriod(position)
        correctTimes()
    }

    private fun correctTimes() {
        viewState.setPeriodTypeVisible(currentPeriodType.canShowDialog)
        viewState.setCustomPeriodVisible(currentPeriodType.showCustomRange)
        fromCompletable { correctDateTime() }
            .subscribeFromPresenter({
                when (currentPeriodType) {
                    PeriodType.DAY -> setPeriod("dd.MM.yyyy")
                    PeriodType.MONTH -> setPeriod("MMMM yyyy")
                    PeriodType.YEAR -> setPeriod("yyyy")
                    PeriodType.CUSTOM, PeriodType.ALL -> setPeriodStartEnd("dd.MM.yyyy")
                }
                loadStatisticData()
            })
    }

    private fun correctDateTime() {
        when (currentPeriodType) {
            PeriodType.DAY -> correctDayToFirst(DateTimeUtils.getJodaDateTime(dateAndTimeStart))
            PeriodType.MONTH -> correctMonthFirst(DateTimeUtils.getJodaDateTime(dateAndTimeStart))
            PeriodType.YEAR -> correctYearFirst(DateTimeUtils.getJodaDateTime(dateAndTimeStart))
            else -> {}
        }
    }

    private fun getMinMaxDateRange(): Observable<Pair<Long, Long>> {
        return statisticInteractor.getFightsMinMaxDateTimes()
            .doOnNext {
                startDateTime = it.first
                endDateTime = it.second
            }
    }

    private fun setPeriodStartEnd(format: String) {
        fromCallable {
            Pair(
                DateTimeUtils.getDateTime(startDateTime, format),
                DateTimeUtils.getDateTime(endDateTime, format)
            )
        }
            .subscribeFromPresenter({
                viewState.setStartDateText(it.first)
                viewState.setEndDateText(it.second)
            })
    }

    private fun setPeriod(format: String) {
        fromCallable {
            DateTimeUtils.getDateTime(startDateTime, format)
        }.subscribeFromPresenter({
            viewState.setPeriodItemText(it)
        })
    }

    fun onPeriodTypeClick() {
        initDateStart()
    }

    fun onDateStartSet(year: Int, monthOfYear: Int, dayOfMonth: Int) {
        fromCallable {
            startDateChange(year, monthOfYear, dayOfMonth)
        }.subscribeFromPresenter({ ranged ->
            if (ranged) {
                if (startDateTime > endDateTime) {
                    viewState.toastError(resourcesInteractor.getString(R.string.stat_error_end_less_start))
                    endDateTime = startDateTime
                }
            }
            correctTimes()
        }, {
            viewState.toastError(it.message)
        })
    }

    private fun startDateChange(year: Int, monthOfYear: Int, dayOfMonth: Int): Boolean {
        dateAndTimeStart.set(Calendar.YEAR, year)
        dateAndTimeStart.set(Calendar.MONTH, monthOfYear)
        dateAndTimeStart.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        startDateTime = dateAndTimeStart.timeInMillis
        return isRanged()
    }

    private fun loadStatisticData() {
        if (enableFilter) {
            when {
                stringsSelection.isNotEmpty()
                        && filterType == FilterType.AIRCRAFT_NAME -> filterBySelectedAircraftNames(
                    stringsSelection
                )
                stringsSelection.isNotEmpty()
                        && filterType == FilterType.AIRCRAFT_REG_NO -> filterBySelectedAircraftRegNo(
                    stringsSelection
                )
                stringsSelection.isNotEmpty()
                        && filterType == FilterType.AIRCRAFT_TYPE -> filterBySelectedAircraftTypes(
                    stringsSelection
                )
                longsSelection.isNotEmpty()
                        && filterType == FilterType.FLIGHT_TYPE -> filterBySelectedFlightTypes(
                    longsSelection
                )
                filterType == FilterType.COLOR -> filterBySelectedColor()
            }
        } else {
            loadAllFlights()
                .subscribeFromPresenter({
                    viewState.clearAdapter()
                    viewState.updateAdapter(it)
                    viewState.showEmptyView(it.isEmpty())
                })
        }
    }

    private fun filterBySelectedColor() {
        updateStatistic(statColor())
    }

    private fun statColor(): Observable<ArrayList<Statistic>> {
        return if (isRanged()) {
            getMinMaxDateRange().flatMap {
                statisticInteractor.loadFilteredFlightsByColor(
                    color, startDateTime, endDateTime, extendedStatistic, true
                )
            }
        } else {
            statisticInteractor.loadFilteredFlightsByColor(
                color,
                startDateTime,
                endDateTime,
                extendedStatistic,
                false
            )
        }
    }

    private fun loadAllFlights(): Observable<ArrayList<Statistic>> {
        return if (isRanged()) {
            getMinMaxDateRange().flatMap {
                statisticInteractor.loadDBFlights(
                    startDateTime,
                    endDateTime,
                    extendedStatistic,
                    true
                )
            }
        } else {
            statisticInteractor.loadDBFlights(startDateTime, endDateTime, extendedStatistic, false)
        }
    }

    private fun filterBySelectedFlightTypes(filterSelection: List<Long?>) {
        updateStatistic(getFlightTypeStat(filterSelection))
    }

    private fun getFlightTypeStat(filterSelection: List<Long?>): Observable<ArrayList<Statistic>> {
        return if (isRanged()) {
            getMinMaxDateRange().flatMap {
                statisticInteractor.loadFilteredFlightsByFlightTypes(
                    startDateTime,
                    endDateTime,
                    extendedStatistic,
                    filterSelection,
                    true
                )
            }
        } else {
            statisticInteractor.loadFilteredFlightsByFlightTypes(
                startDateTime, endDateTime, extendedStatistic, filterSelection, false
            )
        }
    }

    fun initDateStart() {
        fromCallable {
            dateAndTimeStart.timeInMillis = startDateTime
            val y = dateAndTimeStart.get(Calendar.YEAR)
            val m = dateAndTimeStart.get(Calendar.MONTH)
            val d = dateAndTimeStart.get(Calendar.DAY_OF_MONTH)
            Triple(y, m, d)
        }.subscribeFromPresenter({
            viewState.showDateDialogStart(it.first, it.second, it.third)
        })

    }

    fun initDateEnd() {
        fromCallable {
            dateAndTimeEnd.timeInMillis = endDateTime
            val y = dateAndTimeEnd.get(Calendar.YEAR)
            val m = dateAndTimeEnd.get(Calendar.MONTH)
            val d = dateAndTimeEnd.get(Calendar.DAY_OF_MONTH)
            Triple(y, m, d)
        }.subscribeFromPresenter({
            viewState.showDateDialogEnd(it.first, it.second, it.third)
        })
    }

    fun onDateEndSet(year: Int, monthOfYear: Int, dayOfMonth: Int) {
        fromCallable {
            endDateTimeChange(year, monthOfYear, dayOfMonth)
        }.subscribeFromPresenter({ ranged ->
            if (ranged) {
                if (startDateTime > endDateTime) {
                    viewState.toastError(resourcesInteractor.getString(R.string.stat_error_end_less_start))
                    endDateTime = startDateTime
                }
            }
            correctTimes()
        }, {
            viewState.toastError(it.message)
        })
    }

    private fun endDateTimeChange(year: Int, monthOfYear: Int, dayOfMonth: Int): Boolean {
        dateAndTimeEnd.set(Calendar.YEAR, year)
        dateAndTimeEnd.set(Calendar.MONTH, monthOfYear)
        dateAndTimeEnd.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        endDateTime = dateAndTimeEnd.timeInMillis
        return isRanged()
    }

    fun decreasePeriod() {
        fromCallable {
            onAddPeriod(-1)
        }.subscribeFromPresenter({
            setPeriod(it)
            setPeriodStartEnd(it)
            loadStatisticData()
        })
    }

    fun increasePeriod() {
        fromCallable {
            onAddPeriod(1)
        }.subscribeFromPresenter({
            setPeriod(it)
            setPeriodStartEnd(it)
            loadStatisticData()
        })
    }

    private fun onAddPeriod(period: Int): String {
        return when (currentPeriodType) {
            PeriodType.DAY -> {
                val jodaDateTime = DateTimeUtils.getJodaDateTime(dateAndTimeStart)
                val plusDays =
                    if (period == -1) jodaDateTime.minusDays(1) else jodaDateTime.plusDays(period)
                correctDayToFirst(plusDays)
                "dd.MM.yyyy"
            }
            PeriodType.MONTH -> {
                val jodaDateTime = DateTimeUtils.getJodaDateTime(dateAndTimeStart)
                val plusMonths =
                    if (period == -1) jodaDateTime.minusMonths(1) else jodaDateTime.plusMonths(
                        period
                    )
                correctMonthFirst(plusMonths)
                "MMMM yyyy"
            }
            PeriodType.YEAR -> {
                val jodaDateTime = DateTimeUtils.getJodaDateTime(dateAndTimeStart)
                jodaDateTime.withZone(DateTimeZone.UTC)
                val plusYears =
                    if (period == -1) jodaDateTime.minusYears(1) else jodaDateTime.plusYears(period)
                correctYearFirst(plusYears)
                "yyyy"
            }
            else -> "dd.MM.yyyy"
        }
    }

    private fun correctYearFirst(plusYears: DateTime) {
        val startOfDay = plusYears.withDayOfYear(1).withTimeAtStartOfDay()
        startDateTime = startOfDay.millis
        dateAndTimeStart.timeInMillis = startDateTime
        endDateTime = startOfDay.plusYears(1).millis
        dateAndTimeEnd.timeInMillis = endDateTime
    }

    private fun correctMonthFirst(plusMonths: DateTime) {
        val startOfDay = plusMonths.withDayOfMonth(1).withTimeAtStartOfDay()
        startDateTime = startOfDay.millis
        dateAndTimeStart.timeInMillis = startDateTime
        endDateTime = startOfDay.plusMonths(1).millis
        dateAndTimeEnd.timeInMillis = endDateTime
    }

    private fun correctDayToFirst(plusDays: DateTime) {
        val startOfDay = plusDays.withTimeAtStartOfDay()
        startDateTime = startOfDay.millis
        dateAndTimeStart.timeInMillis = startDateTime
        endDateTime = startOfDay.plusDays(1).millis
        dateAndTimeEnd.timeInMillis = endDateTime
    }

    fun onExtendedStatisticChanged(checked: Boolean) {
        extendedStatistic = checked
        correctTimes()
    }

    fun onFilterChanged(isChecked: Boolean) {
        enableFilter = isChecked
        viewState.setFilterStatisticVisible(isChecked)
        correctTimes()
    }

    fun onFilterTypeSelected(filterTypePosition: Int, mSelection: List<Int>) {
        filterType = getFilterType(filterTypePosition)
        longsSelection.clear()
        stringsSelection.clear()
        when (filterType) {
            FilterType.FLIGHT_TYPE -> {
                longsSelection.addAll(getFilterSelection(mSelection)
                    .mapNotNull { it.value.id })
            }
            FilterType.AIRCRAFT_NAME,
            FilterType.AIRCRAFT_TYPE,
            FilterType.AIRCRAFT_REG_NO -> {
                stringsSelection.addAll(getFilterSelection(mSelection)
                    .mapNotNull { it.value.title })
            }
            FilterType.COLOR -> {
            }
        }
        loadStatisticData()
    }

    private fun getFilterType(position: Int): FilterType = FilterType.values()
        .find { it.index == position }
        ?: FilterType.AIRCRAFT_TYPE

    private fun getFilterSelection(mSelection: List<Int>) = filterList.withIndex()
        .filter { it.index in mSelection }

    private fun filterBySelectedAircraftTypes(types: List<String>) {
        val statList = if (isRanged()) {
            getMinMaxDateRange().flatMap {
                statisticInteractor.loadFilteredFlightsByAircraftTypes(
                    types, startDateTime, endDateTime, extendedStatistic, true
                )
            }
        } else {
            statisticInteractor.loadFilteredFlightsByAircraftTypes(
                types,
                startDateTime,
                endDateTime,
                extendedStatistic,
                false
            )
        }
        updateStatistic(statList)
    }

    private fun filterBySelectedAircraftNames(names: List<String>) {
        updateStatistic(if (isRanged()) {
            getMinMaxDateRange().flatMap {
                statisticInteractor.loadFilteredFlightsByAircraftNames(
                    names, startDateTime, endDateTime, extendedStatistic, true
                )
            }
        } else {
            statisticInteractor.loadFilteredFlightsByAircraftNames(
                names,
                startDateTime,
                endDateTime,
                extendedStatistic,
                false
            )
        })
    }

    private fun filterBySelectedAircraftRegNo(regNumbers: List<String>) {
        updateStatistic(if (isRanged()) {
            getMinMaxDateRange().flatMap {
                statisticInteractor.loadFilteredFlightsByAircraftRegNo(
                    regNumbers, startDateTime, endDateTime, extendedStatistic, true
                )
            }
        } else {
            statisticInteractor.loadFilteredFlightsByAircraftRegNo(
                regNumbers,
                startDateTime,
                endDateTime,
                extendedStatistic,
                false
            )
        })
    }

    private fun updateStatistic(statList: Observable<ArrayList<Statistic>>) {
        statList.observeOnMain()
            .subscribeFromPresenter({
                viewState.clearAdapter()
                viewState.updateAdapter(it)
                viewState.showEmptyView(it.isEmpty())
            }, {
                it.printStackTrace()
            })
    }

    private fun isRanged(): Boolean {
        return currentPeriodType == PeriodType.ALL || currentPeriodType == PeriodType.CUSTOM
    }

    fun onFilterSelected(position: Int) {
        viewState.setViewColorVisible(position == FilterType.COLOR.index)
        viewState.setFilterTypeVisible(position != FilterType.COLOR.index)
        when (getFilterType(position)) {
            FilterType.AIRCRAFT_NAME -> loadFilterPlaneNames()
            FilterType.AIRCRAFT_REG_NO -> loadFilterPlaneRegNumbers()
            FilterType.AIRCRAFT_TYPE -> loadFilterPlaneTypes()
            FilterType.FLIGHT_TYPE -> loadFilterFlightTypes()
            FilterType.COLOR -> loadFilterColor()
        }
    }

    private fun loadFilterFlightTypes() {
        fromSingle { statisticInteractor.loadFlightTypes() }
            .map { flTypes ->
                flTypes.map {
                    StatisticFilter(FilterType.FLIGHT_TYPE, it.id, it.typeTitle ?: "")
                }
            }
            .subscribeFromPresenter({
                filterList = it
                initFilter()
            }, {
                it.printStackTrace()
            })
    }

    private fun loadFilterColor() {
        filterType = FilterType.COLOR
        filterList = listOf(StatisticFilter(FilterType.COLOR, 0, color.toHexColor()))
        loadStatisticData()
    }

    private fun loadFilterPlaneTypes() {
        fromSingle { statisticInteractor.loadAircraftsTypes() }
            .map { types ->
                types.map {
                    StatisticFilter(FilterType.AIRCRAFT_TYPE, null, it)
                }
            }
            .subscribeFromPresenter({
                filterList = it
                initFilter()
            }, {
                it.printStackTrace()
            })
    }

    private fun loadFilterPlaneNames() {
        fromSingle { statisticInteractor.loadAircrafts() }
            .map { types ->
                types.distinctBy { it.typeName }
                    .map {
                        StatisticFilter(FilterType.AIRCRAFT_NAME, it.typeId, it.typeName)
                    }
            }
            .subscribeFromPresenter({
                filterList = it
                initFilter()
            }, {
                it.printStackTrace()
            })
    }

    private fun loadFilterPlaneRegNumbers() {
        fromSingle { statisticInteractor.loadAircrafts() }
            .map { types ->
                types.distinctBy { it.regNo }
                    .map {
                        StatisticFilter(FilterType.AIRCRAFT_NAME, it.typeId, it.regNo)
                    }
            }
            .subscribeFromPresenter({
                filterList = it
                initFilter()
            }, {
                it.printStackTrace()
            })
    }

    private fun initFilter() {
        fromCallable { filterList }
            .map { list -> list.map { it.title ?: "" } }
            .subscribeFromPresenter({
                if (it.isNotEmpty()) {
                    viewState.setFilterSpinnerItems(it)
                }
            }, {
                it.printStackTrace()
            })
    }

    fun colorClick() {
        fromCallable { colors ?: getColorsIntArray() }
            .subscribeFromPresenter({ colors ->
                viewState.onColorSelect(colors)
            })
    }

    fun onColorSelected(color: Int) {
        this.color = color
        viewState.setViewColor(color)
        loadFilterColor()
    }
}