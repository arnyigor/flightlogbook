package com.arny.flightlogbook.presentation.statistic

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.arny.domain.common.CommonUseCase
import com.arny.domain.flights.FlightsUseCase
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.R
import com.arny.helpers.coroutins.launchAsync
import com.arny.helpers.utils.DateTimeUtils
import com.arny.helpers.utils.addTo
import com.arny.helpers.utils.fromCallable
import com.arny.helpers.utils.observeOnMain
import io.reactivex.disposables.CompositeDisposable
import org.joda.time.DateTimeZone
import java.util.*
import javax.inject.Inject

/**
 *Created by Sedoy on 21.08.2019
 */
@InjectViewState
class StatisticsPresenter : MvpPresenter<StatisticsView>() {
    private val compositeDisposable = CompositeDisposable()
    @Inject
    lateinit var flightsUseCase: FlightsUseCase
    @Inject
    lateinit var commonUseCase: CommonUseCase
    private var currentPeriodType = 0
    private val dateAndTimeStart = GregorianCalendar.getInstance()
    private val dateAndTimeEnd = GregorianCalendar.getInstance()
    private var startdatetime: Long = dateAndTimeStart.timeInMillis
    private var enddatetime: Long = dateAndTimeEnd.timeInMillis

    init {
        FlightApp.appComponent.inject(this)
    }

    override fun detachView(view: StatisticsView?) {
        super.detachView(view)
        compositeDisposable.clear()
    }

    fun loadData() {
        fromCallable { flightsUseCase.loadDBFlights() }
                .observeOnMain()
                .subscribe({

                }, {

                })
                .addTo(compositeDisposable)

    }

    fun onPeriodChanged(position: Int) {
        viewState?.setPeriodTypeVisible(position != 3)
        viewState?.setCustomPeriodVisible(position == 3)
        currentPeriodType = position
        when (currentPeriodType) {
            0 -> {
                setPeriod("dd.MM.yyyy")
            }
            1 -> {
                setPeriod("MMMM yyyy")
            }
            2 -> {
                setPeriod("yyyy")
            }
            else -> {
                setPeriodStartEnd("dd.MM.yyyy")
            }
        }
    }

    private fun setPeriodStartEnd(format: String) {
        launchAsync({
            Pair(DateTimeUtils.getDateTime(startdatetime, format), DateTimeUtils.getDateTime(enddatetime, format))
        }, {
            viewState?.setStartDateText(it.first)
            viewState?.setEndDateText(it.second)
        }, {
            it.printStackTrace()
        })
    }

    private fun setPeriod(format: String) {
        launchAsync({
            DateTimeUtils.getDateTime(startdatetime, format)
        }, {
            viewState?.setPeriodItemText(it)
        }, {
            it.printStackTrace()
        })
    }

    fun onPeriodTypeClick() {
        when (currentPeriodType) {
            0 -> {

            }
            else -> {

            }
        }
    }

    fun onDateStartSet(year: Int, monthOfYear: Int, dayOfMonth: Int) {
        launchAsync({
            dateAndTimeStart.set(Calendar.YEAR, year)
            dateAndTimeStart.set(Calendar.MONTH, monthOfYear)
            dateAndTimeStart.set(Calendar.DAY_OF_MONTH, dayOfMonth)
//            dateAndTimeStart.timeZone = TimeZone.getTimeZone("UTC")
            startdatetime = dateAndTimeStart.timeInMillis
            if (startdatetime > enddatetime) {
                viewState?.toastError(commonUseCase.getString(R.string.stat_error_end_less_start))
                startInitDateTime()
            }
            DateTimeUtils.getDateTime(startdatetime, "dd.MM.yyyy")
        }, {
            viewState?.setStartDateText(it)
        }, {
            it.printStackTrace()
            viewState?.toastError(it.message)
            startInitDateTime()
        })
    }

    private fun startInitDateTime() {

    }

    fun initDateStart() {
        launchAsync({
            dateAndTimeStart.timeInMillis = startdatetime
            val y = dateAndTimeStart.get(Calendar.YEAR)
            val m = dateAndTimeStart.get(Calendar.MONTH)
            val d = dateAndTimeStart.get(Calendar.DAY_OF_MONTH)
            Triple(y, m, d)
        }, {
            viewState?.showDateDialogStart(it.first, it.second, it.third)
        }, {
            it.printStackTrace()
        })
    }

    fun initDateEnd() {
        launchAsync({
            dateAndTimeEnd.timeInMillis = enddatetime
            val y = dateAndTimeEnd.get(Calendar.YEAR)
            val m = dateAndTimeEnd.get(Calendar.MONTH)
            val d = dateAndTimeEnd.get(Calendar.DAY_OF_MONTH)
            Triple(y, m, d)
        }, {
            viewState?.showDateDialogEnd(it.first, it.second, it.third)
        }, {
            it.printStackTrace()
        })
    }

    fun onDateEndSet(year: Int, monthOfYear: Int, dayOfMonth: Int) {
        launchAsync({
            dateAndTimeEnd.set(Calendar.YEAR, year)
            dateAndTimeEnd.set(Calendar.MONTH, monthOfYear)
            dateAndTimeEnd.set(Calendar.DAY_OF_MONTH, dayOfMonth)
//            dateAndTimeEnd.timeZone = TimeZone.getTimeZone("UTC")
            enddatetime = dateAndTimeEnd.timeInMillis
            if (startdatetime > enddatetime) {
                viewState?.toastError(commonUseCase.getString(R.string.stat_error_end_less_start))
                startInitDateTime()
            }
            DateTimeUtils.getDateTime(enddatetime, "dd.MM.yyyy")
        }, {
            viewState?.setEndDateText(it)
        }, {
            it.printStackTrace()
            viewState?.toastError(it.message)
            startInitDateTime()
        })
    }

    fun decreasePeriod() {
        launchAsync({
            onAddPeriod(-1)
        }, {
            setPeriod("dd.MMMM.yyyy HH:mm")
            setPeriodStartEnd("dd.MMMM.yyyy HH:mm")
        }, {
            it.printStackTrace()
        })
    }

    fun increasePeriod() {
        launchAsync({
            onAddPeriod(1)
        }, {
            setPeriod("dd.MMMM.yyyy HH:mm")
            setPeriodStartEnd("dd.MMMM.yyyy HH:mm")
        }, {
            it.printStackTrace()
        })
    }

    private fun onAddPeriod(period: Int): String {
        var format = "dd.MM.yyyy"
        when (currentPeriodType) {
            0 -> {
                val jodaDateTime = DateTimeUtils.getJodaDateTime(dateAndTimeStart)
                val plusDays = if (period == -1) jodaDateTime.minusDays(1) else jodaDateTime.plusDays(period)
                val startOfDay = plusDays.withTimeAtStartOfDay()
//                startOfDay.withZone(DateTimeZone.UTC)
                startdatetime = startOfDay.millis
                dateAndTimeStart.timeInMillis = startdatetime
                enddatetime = startOfDay.plusDays(1).millis
                dateAndTimeEnd.timeInMillis = enddatetime
                format = "dd.MM.yyyy"
            }
            1 -> {
                val jodaDateTime = DateTimeUtils.getJodaDateTime(dateAndTimeStart)
                val plusMonths = if (period == -1) jodaDateTime.minusMonths(1) else jodaDateTime.plusMonths(period)
                val startOfDay = plusMonths.withDayOfMonth(1).withTimeAtStartOfDay()
//                startOfDay.withZone(DateTimeZone.UTC)
                startdatetime = startOfDay.millis
                dateAndTimeStart.timeInMillis = startdatetime
                enddatetime = startOfDay.plusMonths(1).millis
                dateAndTimeEnd.timeInMillis = enddatetime
                format = "MMMM yyyy"
            }
            2 -> {
                val jodaDateTime = DateTimeUtils.getJodaDateTime(dateAndTimeStart)
                jodaDateTime.withZone(DateTimeZone.UTC)
                val plusYears = if (period == -1) jodaDateTime.minusYears(1) else jodaDateTime.plusYears(period)
                val startOfDay = plusYears.withDayOfYear(1).withTimeAtStartOfDay()
//                startOfDay.withZone(DateTimeZone.UTC)
                startdatetime = startOfDay.millis
                dateAndTimeStart.timeInMillis = startdatetime
                enddatetime = startOfDay.plusYears(1).millis
                dateAndTimeEnd.timeInMillis = enddatetime
                format = "yyyy"
            }
        }
        return format
    }

}