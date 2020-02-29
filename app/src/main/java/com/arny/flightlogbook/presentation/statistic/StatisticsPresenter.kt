package com.arny.flightlogbook.presentation.statistic

import android.util.Log
import com.arny.domain.common.ResourcesInteractor
import com.arny.domain.flights.FlightsInteractor
import com.arny.domain.models.Statistic
import com.arny.domain.models.StatisticFilter
import com.arny.domain.statistic.StatisticUseCase
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.R
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
    override val compositeDisposable = CompositeDisposable()
    private var filterList = listOf<StatisticFilter>()
    private var filterType = 0
    private var filterSelection = arrayListOf<Long?>()
    private var extendedStatistic = false
    private var enableFilter = false
    @Inject
    lateinit var flightsInteractor: FlightsInteractor
    @Inject
    lateinit var statisticUseCase: StatisticUseCase
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

    sealed class PeriodType(val canShowDialog: Boolean, val showCustomRange: Boolean) {
        object ALL : PeriodType(false, false)
        object DAY : PeriodType(true, false)
        object MONTH : PeriodType(true, false)
        object YEAR : PeriodType(true, false)
        object CUSTOM : PeriodType(false, true)
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

    override fun detachView(view: StatisticsView?) {
        super.detachView(view)
        compositeDisposable.clear()
    }

    fun onPeriodChanged(position: Int) {
        currentPeriodType = getPeriod(position)
        correctTimes()
    }

    private fun correctTimes() {
        viewState?.setPeriodTypeVisible(currentPeriodType.canShowDialog)
        viewState?.setCustomPeriodVisible(currentPeriodType.showCustomRange)
        fromCompletable { correctDateTime() }
                .completableSubscribeAdd({
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
        return statisticUseCase.getFightsMinMaxDateTimes()
                .doOnNext {
                    startdatetime = it.first
                    enddatetime = it.second
                }
    }

    private fun setPeriodStartEnd(format: String) {
        fromCallable { Pair(DateTimeUtils.getDateTime(startdatetime, format), DateTimeUtils.getDateTime(enddatetime, format)) }
                .observeSubscribeAdd({
                    viewState?.setStartDateText(it.first)
                    viewState?.setEndDateText(it.second)
                })
    }

    private fun setPeriod(format: String) {
        fromCallable {
            DateTimeUtils.getDateTime(startdatetime, format)
        }.observeSubscribeAdd({
            viewState?.setPeriodItemText(it)
        })
    }

    fun onPeriodTypeClick() {
        initDateStart()
    }

    fun onDateStartSet(year: Int, monthOfYear: Int, dayOfMonth: Int) {
        fromCallable {
            startDateChange(year, monthOfYear, dayOfMonth)
        }.observeSubscribeAdd({ ranged ->
            if (ranged) {
                if (startdatetime > enddatetime) {
                    viewState?.toastError(resourcesInteractor.getString(R.string.stat_error_end_less_start))
                    enddatetime = startdatetime
                }
            }
            correctTimes()
        }, {
            viewState?.toastError(it.message)
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
                0 -> filterBySelectedPlanetypes(filterSelection)
                1 -> filterBySelectedTimetypes(filterSelection)
                2 -> filterBySelectedFlightTypes(filterSelection)
            }
        } else {
            loadStat()
                    .observeSubscribeAdd({
                        viewState?.clearAdapter()
                        viewState?.updateAdapter(it)
                        viewState?.showEmptyView(it.isEmpty())
                    })
        }
    }

    private fun loadStat(): Observable<ArrayList<Statistic>> {
        return if (isRanged()) {
            getMinMaxDateRange().flatMap { statisticUseCase.loadDBFlights(startdatetime, enddatetime, extendedStatistic, true) }
        } else {
            statisticUseCase.loadDBFlights(startdatetime, enddatetime, extendedStatistic, false)
        }
    }

    private fun filterBySelectedTimetypes(filterSelection: List<Long?>) {
        return if (isRanged()) {
            getMinMaxDateRange().flatMap { statisticUseCase.loadDBFlightsByTimes(startdatetime, enddatetime, extendedStatistic, filterSelection, true) }
        } else {
            statisticUseCase.loadDBFlightsByTimes(startdatetime, enddatetime, extendedStatistic, filterSelection, false)
        }
                .observeOnMain()
                .subscribe({
                    viewState?.clearAdapter()
                    viewState?.updateAdapter(it)
                    viewState?.showEmptyView(it.isEmpty())
                }, {
                    it.printStackTrace()
                })
                .addTo(compositeDisposable)
    }

    private fun filterBySelectedFlightTypes(filterSelection: List<Long?>) {
        return if (isRanged()) {
            getMinMaxDateRange().flatMap { statisticUseCase.loadFilteredFlightsByFlightTypes(startdatetime, enddatetime, extendedStatistic, filterSelection, true) }
        } else {
            statisticUseCase.loadFilteredFlightsByFlightTypes(startdatetime, enddatetime, extendedStatistic, filterSelection, false)
        }
                .observeOnMain()
                .subscribe({
                    viewState?.clearAdapter()
                    viewState?.updateAdapter(it)
                    viewState?.showEmptyView(it.isEmpty())
                }, {
                    it.printStackTrace()
                })
                .addTo(compositeDisposable)
    }

    fun initDateStart() {
        fromCallable {
            dateAndTimeStart.timeInMillis = startdatetime
            val y = dateAndTimeStart.get(Calendar.YEAR)
            val m = dateAndTimeStart.get(Calendar.MONTH)
            val d = dateAndTimeStart.get(Calendar.DAY_OF_MONTH)
            Triple(y, m, d)
        }.observeSubscribeAdd({
            viewState?.showDateDialogStart(it.first, it.second, it.third)
        })

    }

    fun initDateEnd() {
        fromCallable {
            dateAndTimeEnd.timeInMillis = enddatetime
            val y = dateAndTimeEnd.get(Calendar.YEAR)
            val m = dateAndTimeEnd.get(Calendar.MONTH)
            val d = dateAndTimeEnd.get(Calendar.DAY_OF_MONTH)
            Triple(y, m, d)
        }.observeSubscribeAdd({
            viewState?.showDateDialogEnd(it.first, it.second, it.third)
        })
    }

    fun onDateEndSet(year: Int, monthOfYear: Int, dayOfMonth: Int) {
        fromCallable {
            endDateTimeChange(year, monthOfYear, dayOfMonth)
        }.observeSubscribeAdd({ ranged ->
            if (ranged) {
                if (startdatetime > enddatetime) {
                    viewState?.toastError(resourcesInteractor.getString(R.string.stat_error_end_less_start))
                    enddatetime = startdatetime
                }
            }
            correctTimes()
        }, {
            viewState?.toastError(it.message)
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
        }.observeSubscribeAdd({
            setPeriod(it)
            setPeriodStartEnd(it)
            loadStatisticData()
        })
    }

    fun increasePeriod() {
        fromCallable {
            onAddPeriod(1)
        }.observeSubscribeAdd({
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
        //                startOfDay.withZone(DateTimeZone.UTC)
        startdatetime = startOfDay.millis
        dateAndTimeStart.timeInMillis = startdatetime
        enddatetime = startOfDay.plusYears(1).millis
        dateAndTimeEnd.timeInMillis = enddatetime
    }

    private fun correctMonthFirst(plusMonths: DateTime) {
        val startOfDay = plusMonths.withDayOfMonth(1).withTimeAtStartOfDay()
        //                startOfDay.withZone(DateTimeZone.UTC)
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
        viewState?.setFilterStatisticVisible(isChecked)
        correctTimes()
    }

    fun onFilter(selectedfilterPosition: Int, mSelection: List<Int>) {
        filterType = selectedfilterPosition
        filterSelection.clear()
        filterSelection.addAll(filterList.withIndex().filter { it.index in mSelection }.map { it.value.id })
        loadStatisticData()
    }

    private fun filterBySelectedPlanetypes(types: List<Long?>) {
        if (isRanged()) {
            getMinMaxDateRange().flatMap { statisticUseCase.loadFilteredFlightsByPlaneTypes(types, startdatetime, enddatetime, extendedStatistic, true) }
        } else {
            statisticUseCase.loadFilteredFlightsByPlaneTypes(types, startdatetime, enddatetime, extendedStatistic, false)
        }
                .observeOnMain()
                .subscribe({
                    viewState?.clearAdapter()
                    viewState?.updateAdapter(it)
                    viewState?.showEmptyView(it.isEmpty())
                }, {
                    it.printStackTrace()
                })
                .addTo(compositeDisposable)
    }

    private fun isRanged(): Boolean {
        return currentPeriodType == PeriodType.ALL || currentPeriodType == PeriodType.CUSTOM
    }

    fun onFilterTypeSelect(position: Int) {
        Log.i(StatisticsPresenter::class.java.simpleName, "onFilterTypeSelect: $position")
        when (position) {
            0 -> loadFilterPlaneTypes()
            2 -> loadFilterFlightTypes()
//            3 -> loadFilterRegNums()
        }
    }

    private fun loadFilterFlightTypes() {
        statisticUseCase.loadFlightTypes()
                .map { flTypes -> flTypes.map { StatisticFilter(2, it.id, it.typeTitle ?: "") } }
                .observeOnMain()
                .subscribe({
                    filterList = it
                    initFilter()
                }, {
                    it.printStackTrace()
                })
                .addTo(compositeDisposable)
    }

    private fun loadFilterPlaneTypes() {
        statisticUseCase.loadPlaneTypes()
                .map { types -> types.map { StatisticFilter(0, it.typeId, it.typeName ?: "") } }
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
                    viewState?.setFilterSpinnerItems(it)
                }, {
                    it.printStackTrace()
                })
                .addTo(compositeDisposable)
    }

}