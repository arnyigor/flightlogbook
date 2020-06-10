package com.arny.flightlogbook.presentation.statistic.presenter

import android.graphics.Color
import com.arny.domain.common.ResourcesInteractor
import com.arny.domain.flights.FlightsInteractor
import com.arny.domain.models.FilterType
import com.arny.domain.models.Statistic
import com.arny.domain.models.StatisticFilter
import com.arny.domain.statistic.StatisticInteractor
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.R
import com.arny.flightlogbook.presentation.statistic.view.StatisticsView
import com.arny.helpers.utils.*
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import moxy.InjectViewState
import moxy.MvpPresenter
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.util.*
import javax.inject.Inject

@InjectViewState
class StatisticsPresenter : MvpPresenter<StatisticsView>(), CompositeDisposableComponent {
    private var color: Int = Color.BLACK
    override val compositeDisposable = CompositeDisposable()
    private var filterList = listOf<StatisticFilter>()
    private var filterType = FilterType.FLIGHT
    private var filterSelection = arrayListOf<Long?>()
    private var extendedStatistic = false
    private var enableFilter = false
    private var colors: IntArray? = null

    @Inject
    lateinit var flightsInteractor: FlightsInteractor

    @Inject
    lateinit var statisticInteractor: StatisticInteractor

    @Inject
    lateinit var resourcesInteractor: ResourcesInteractor

    @Volatile
    private var currentPeriodType: PeriodType = PeriodType.ALL
    private val dateAndTimeStart = GregorianCalendar.getInstance()
    private val dateAndTimeEnd = GregorianCalendar.getInstance()

    @Volatile
    private var startdatetime: Long = dateAndTimeStart.timeInMillis

    @Volatile
    private var enddatetime: Long = dateAndTimeEnd.timeInMillis

    init {
        FlightApp.appComponent.inject(this)
    }

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
                .subsribeFromPresenter({
                    colors = it
                })
    }

    override fun detachView(view: StatisticsView?) {
        super.detachView(view)
        compositeDisposable.clear()
    }

    fun onPeriodChanged(position: Int) {
        currentPeriodType = getPeriod(position)
        correctTimes()
    }

    private fun correctTimes() {
        viewState.setPeriodTypeVisible(currentPeriodType.canShowDialog)
        viewState.setCustomPeriodVisible(currentPeriodType.showCustomRange)
        fromCompletable { correctDateTime() }
                .subsribeFromPresenter({
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
        }
    }

    private fun getMinMaxDateRange(): Observable<Pair<Long, Long>> {
        return statisticInteractor.getFightsMinMaxDateTimes()
                .doOnNext {
                    startdatetime = it.first
                    enddatetime = it.second
                }
    }

    private fun setPeriodStartEnd(format: String) {
        fromCallable { Pair(DateTimeUtils.getDateTime(startdatetime, format), DateTimeUtils.getDateTime(enddatetime, format)) }
                .subsribeFromPresenter({
                    viewState.setStartDateText(it.first)
                    viewState.setEndDateText(it.second)
                })
    }

    private fun setPeriod(format: String) {
        fromCallable {
            DateTimeUtils.getDateTime(startdatetime, format)
        }.subsribeFromPresenter({
            viewState.setPeriodItemText(it)
        })
    }

    fun onPeriodTypeClick() {
        initDateStart()
    }

    fun onDateStartSet(year: Int, monthOfYear: Int, dayOfMonth: Int) {
        fromCallable {
            startDateChange(year, monthOfYear, dayOfMonth)
        }.subsribeFromPresenter({ ranged ->
            if (ranged) {
                if (startdatetime > enddatetime) {
                    viewState.toastError(resourcesInteractor.getString(R.string.stat_error_end_less_start))
                    enddatetime = startdatetime
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
        startdatetime = dateAndTimeStart.timeInMillis
        return isRanged()
    }

    private fun loadStatisticData() {
        if (enableFilter && filterSelection.isNotEmpty()) {
            when (filterType) {
                FilterType.FLIGHT -> filterBySelectedPlanetypes(filterSelection)
                FilterType.PLANE -> filterBySelectedFlightTypes(filterSelection)
                FilterType.COLOR -> filterBySelectedColor()
            }
        } else {
            loadAllFlights()
                    .subsribeFromPresenter({
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
                        color, startdatetime, enddatetime, extendedStatistic, true)
            }
        } else {
            statisticInteractor.loadFilteredFlightsByColor(color, startdatetime, enddatetime, extendedStatistic, false)
        }
    }

    private fun loadAllFlights(): Observable<ArrayList<Statistic>> {
        return if (isRanged()) {
            getMinMaxDateRange().flatMap { statisticInteractor.loadDBFlights(startdatetime, enddatetime, extendedStatistic, true) }
        } else {
            statisticInteractor.loadDBFlights(startdatetime, enddatetime, extendedStatistic, false)
        }
    }

    /* private fun filterBySelectedTimetypes(filterSelection: List<Long?>) {
         if (isRanged()) {
             getMinMaxDateRange().flatMap { statisticInteractor.loadDBFlightsByTimes(startdatetime, enddatetime, extendedStatistic, filterSelection, true) }
         } else {
             statisticInteractor.loadDBFlightsByTimes(startdatetime, enddatetime, extendedStatistic, filterSelection, false)
         }
                 .observeOnMain()
                 .subscribe({
                     viewState.clearAdapter()
                     viewState.updateAdapter(it)
                     viewState.showEmptyView(it.isEmpty())
                 }, {
                     it.printStackTrace()
                 })
                 .addTo(compositeDisposable)
     }*/

    private fun filterBySelectedFlightTypes(filterSelection: List<Long?>) {
        updateStatistic(getFlightTypeStat(filterSelection))
    }

    private fun getFlightTypeStat(filterSelection: List<Long?>): Observable<ArrayList<Statistic>> {
        return if (isRanged()) {
            getMinMaxDateRange().flatMap {
                statisticInteractor.loadFilteredFlightsByFlightTypes(
                        startdatetime,
                        enddatetime,
                        extendedStatistic,
                        filterSelection,
                        true
                )
            }
        } else {
            statisticInteractor.loadFilteredFlightsByFlightTypes(
                    startdatetime, enddatetime, extendedStatistic, filterSelection, false
            )
        }
    }

    fun initDateStart() {
        fromCallable {
            dateAndTimeStart.timeInMillis = startdatetime
            val y = dateAndTimeStart.get(Calendar.YEAR)
            val m = dateAndTimeStart.get(Calendar.MONTH)
            val d = dateAndTimeStart.get(Calendar.DAY_OF_MONTH)
            Triple(y, m, d)
        }.subsribeFromPresenter({
            viewState.showDateDialogStart(it.first, it.second, it.third)
        })

    }

    fun initDateEnd() {
        fromCallable {
            dateAndTimeEnd.timeInMillis = enddatetime
            val y = dateAndTimeEnd.get(Calendar.YEAR)
            val m = dateAndTimeEnd.get(Calendar.MONTH)
            val d = dateAndTimeEnd.get(Calendar.DAY_OF_MONTH)
            Triple(y, m, d)
        }.subsribeFromPresenter({
            viewState.showDateDialogEnd(it.first, it.second, it.third)
        })
    }

    fun onDateEndSet(year: Int, monthOfYear: Int, dayOfMonth: Int) {
        fromCallable {
            endDateTimeChange(year, monthOfYear, dayOfMonth)
        }.subsribeFromPresenter({ ranged ->
            if (ranged) {
                if (startdatetime > enddatetime) {
                    viewState.toastError(resourcesInteractor.getString(R.string.stat_error_end_less_start))
                    enddatetime = startdatetime
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
        enddatetime = dateAndTimeEnd.timeInMillis
        return isRanged()
    }

    fun decreasePeriod() {
        fromCallable {
            onAddPeriod(-1)
        }.subsribeFromPresenter({
            setPeriod(it)
            setPeriodStartEnd(it)
            loadStatisticData()
        })
    }

    fun increasePeriod() {
        fromCallable {
            onAddPeriod(1)
        }.subsribeFromPresenter({
            setPeriod(it)
            setPeriodStartEnd(it)
            loadStatisticData()
        })
    }

    private fun onAddPeriod(period: Int): String {
        var format = "dd.MM.yyyy"
        when (currentPeriodType) {
            PeriodType.DAY -> {
                val jodaDateTime = DateTimeUtils.getJodaDateTime(dateAndTimeStart)
                val plusDays = if (period == -1) jodaDateTime.minusDays(1) else jodaDateTime.plusDays(period)
                correctDayToFirst(plusDays)
                format = "dd.MM.yyyy"
            }
            PeriodType.MONTH -> {
                val jodaDateTime = DateTimeUtils.getJodaDateTime(dateAndTimeStart)
                val plusMonths = if (period == -1) jodaDateTime.minusMonths(1) else jodaDateTime.plusMonths(period)
                correctMonthFirst(plusMonths)
                format = "MMMM yyyy"
            }
            PeriodType.YEAR -> {
                val jodaDateTime = DateTimeUtils.getJodaDateTime(dateAndTimeStart)
                jodaDateTime.withZone(DateTimeZone.UTC)
                val plusYears = if (period == -1) jodaDateTime.minusYears(1) else jodaDateTime.plusYears(period)
                correctYearFirst(plusYears)
                format = "yyyy"
            }
        }
        return format
    }

    private fun correctYearFirst(plusYears: DateTime) {
        val startOfDay = plusYears.withDayOfYear(1).withTimeAtStartOfDay()
        startdatetime = startOfDay.millis
        dateAndTimeStart.timeInMillis = startdatetime
        enddatetime = startOfDay.plusYears(1).millis
        dateAndTimeEnd.timeInMillis = enddatetime
    }

    private fun correctMonthFirst(plusMonths: DateTime) {
        val startOfDay = plusMonths.withDayOfMonth(1).withTimeAtStartOfDay()
        startdatetime = startOfDay.millis
        dateAndTimeStart.timeInMillis = startdatetime
        enddatetime = startOfDay.plusMonths(1).millis
        dateAndTimeEnd.timeInMillis = enddatetime
    }

    private fun correctDayToFirst(plusDays: DateTime) {
        val startOfDay = plusDays.withTimeAtStartOfDay()
        startdatetime = startOfDay.millis
        dateAndTimeStart.timeInMillis = startdatetime
        enddatetime = startOfDay.plusDays(1).millis
        dateAndTimeEnd.timeInMillis = enddatetime
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

    fun onFilterTypeSelected(selectedfilterPosition: Int, mSelection: List<Int>) {
        val statisticType = when (selectedfilterPosition) {
            0 -> FilterType.FLIGHT
            1 -> FilterType.PLANE
            2 -> FilterType.COLOR
            else -> FilterType.FLIGHT
        }
        filterType = statisticType
        filterSelection.clear()
        filterSelection.addAll(filterList.withIndex().filter { it.index in mSelection }.map { it.value.id })
        loadStatisticData()
    }

    private fun filterBySelectedPlanetypes(types: List<Long?>) {
        val statList = if (isRanged()) {
            getMinMaxDateRange().flatMap {
                statisticInteractor.loadFilteredFlightsByPlaneTypes(
                        types, startdatetime, enddatetime, extendedStatistic, true)
            }
        } else {
            statisticInteractor.loadFilteredFlightsByPlaneTypes(types, startdatetime, enddatetime, extendedStatistic, false)
        }
        updateStatistic(statList)
    }

    private fun updateStatistic(statList: Observable<ArrayList<Statistic>>) {
        statList.observeOnMain()
                .subscribe({
                    viewState.clearAdapter()
                    viewState.updateAdapter(it)
                    viewState.showEmptyView(it.isEmpty())
                }, {
                    it.printStackTrace()
                })
                .addTo(compositeDisposable)
    }

    private fun isRanged(): Boolean {
        return currentPeriodType == PeriodType.ALL || currentPeriodType == PeriodType.CUSTOM
    }

    fun onFilterSelected(position: Int) {
        viewState.setViewColorVisible(position == 2)
        viewState.setFilterTypeVisible(position != 2)
        when (position) {
            0 -> loadFilterPlaneTypes()
            1 -> loadFilterFlightTypes()
            2 -> loadFilterColor()
        }
    }

    private fun loadFilterFlightTypes() {
        statisticInteractor.loadFlightTypes()
                .map { flTypes ->
                    flTypes.map {
                        StatisticFilter(FilterType.FLIGHT, it.id, it.typeTitle ?: "")
                    }
                }
                .observeOnMain()
                .subscribe({
                    filterList = it
                    initFilter()
                }, {
                    it.printStackTrace()
                })
                .addTo(compositeDisposable)
    }

    private fun loadFilterColor() {
        filterType = FilterType.COLOR
        filterList = listOf(StatisticFilter(FilterType.COLOR, 0, color.toHexColor()))
        loadStatisticData()
    }

    private fun loadFilterPlaneTypes() {
        statisticInteractor.loadPlaneTypes()
                .map { types ->
                    types.map {
                        StatisticFilter(FilterType.PLANE, it.typeId, it.typeName ?: "")
                    }
                }
                .observeOnMain()
                .subscribe({
                    filterList = it
                    initFilter()
                }, {
                    it.printStackTrace()
                })
                .addTo(compositeDisposable)
    }

    private fun initFilter() {
        fromCallable { filterList }
                .map { list -> list.map { it.title ?: "" } }
                .observeOnMain()
                .subscribe({
                    viewState.setFilterSpinnerItems(it)
                }, {
                    it.printStackTrace()
                })
                .addTo(compositeDisposable)
    }

    fun colorClick() {
        fromCallable { colors ?: getColorsIntArray() }
                .subsribeFromPresenter({ colors ->
                    viewState.onColorSelect(colors)
                })
    }

    fun onColorSelected(color: Int) {
        this.color = color
        viewState.setViewColor(color)
        loadFilterColor()
    }
}