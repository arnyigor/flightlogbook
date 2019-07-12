package com.arny.flightlogbook.presentation.flights.viewflights

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.R
import com.arny.flightlogbook.data.Consts
import com.arny.flightlogbook.data.models.Flight
import com.arny.flightlogbook.data.source.MainRepositoryImpl
import com.arny.flightlogbook.data.utils.DateTimeUtils
import com.arny.flightlogbook.data.utils.addTo
import com.arny.flightlogbook.data.utils.observeOnMain
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import javax.inject.Inject


@InjectViewState
class ViewFlightsPresenterImpl : MvpPresenter<ViewFlightsView>(), ViewFlightsPresenter {
    private val compositeDisposable = CompositeDisposable()
    @Inject
    lateinit var repository: MainRepositoryImpl

    init {
        FlightApp.appComponent.inject(this)
    }

    override fun detachView(view: ViewFlightsView?) {
        super.detachView(view)
        compositeDisposable.clear()
    }

    private fun getTotals() {
         Observable.zip<Int, Int, String>(
                repository.getFlightsTime(),
                repository.getFlightsCount(),
                BiFunction { time: Int, cnt: Int ->
                    String.format("%s %s\n%s %d",
                            repository.getString(R.string.str_totaltime),
                            DateTimeUtils.strLogTime(time),
                            repository.getString(R.string.total_records),
                            cnt)
                }).observeOnMain()
                .subscribe({ time ->
                    viewState?.displayTotalTime(time)
                }, {
                    it.printStackTrace()
                }).addTo(compositeDisposable)
    }

    private fun getFilterflights(filtertype: Int): String = when (filtertype) {
        0 -> Consts.DB.COLUMN_DATETIME + " ASC"
        1 -> Consts.DB.COLUMN_DATETIME + " DESC"
        3 -> Consts.DB.COLUMN_LOG_TIME + " DESC"
        2 -> Consts.DB.COLUMN_LOG_TIME + " ASC"
        else -> Consts.DB.COLUMN_DATETIME + " ASC"
    }

    override fun loadFlights() {
        Observable.fromCallable { getFilterflights(repository.getPrefInt(Consts.PrefsConsts.CONFIG_USER_FILTER_FLIGHTS)) }
                .map { repository.getDbFlights(it) }
                .observeOnMain()
                .subscribe({ flights ->
                    if (flights.isNotEmpty()) {
                        viewState?.updateAdapter(flights)
                    }
                    getTotals()
                }, {
                    it.printStackTrace()
                    viewState?.toastError(it.message)
                }).addTo(compositeDisposable)
    }

    override fun removeAllFlights() {
        val subscribe = repository.removeAllFlights().observeOnMain()
                .subscribe({ removed ->
                    if (removed) {
                        loadFlights()
                    } else {
                        viewState?.toastError("Записи не удалены")
                    }
                }, {
                    it.printStackTrace()
                })
    }

    override fun removeItem(item: Flight?) {
        if (item?.id != null) {
            val disposable = repository.removeFlight(item.id!!)
                    .observeOnMain()
                    .subscribe({
                        loadFlights()
                    }, {
                        it.printStackTrace()
                    })
        }
    }
}
