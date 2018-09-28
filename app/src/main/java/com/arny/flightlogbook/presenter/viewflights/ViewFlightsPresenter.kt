package com.arny.flightlogbook.presenter.viewflights

import com.arny.flightlogbook.R
import com.arny.flightlogbook.data.Consts
import com.arny.flightlogbook.data.models.Flight
import com.arny.flightlogbook.data.source.MainRepositoryImpl
import com.arny.flightlogbook.presenter.base.BaseMvpPresenterImpl
import com.arny.flightlogbook.utils.DateTimeUtils
import com.arny.flightlogbook.utils.observeOnMain
import io.reactivex.Observable
import io.reactivex.functions.BiFunction


class ViewFlightsPresenter : BaseMvpPresenterImpl<ViewFlightsContract.View>(), ViewFlightsContract.Presenter {
    private val repository = MainRepositoryImpl.instance

    private fun getTotals() {
        val disposable = Observable.zip<Int, Int, String>(
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
                    getView()?.displayTotalTime(time)
                }, {
                    it.printStackTrace()
                })
    }

    override fun loadFlights() {
        val order = getFilterflights(repository.getPrefInt(Consts.PrefsConsts.CONFIG_USER_FILTER_FLIGHTS)
                ?: 0)
        val disposable = repository.getDbFlights(order).observeOnMain()
                .subscribe({ flights ->
                    if (flights.isNotEmpty()) {
                        getView()?.updateAdapter(flights)
                    }
                    getTotals()
                }, {
                    it.printStackTrace()
                })
    }

    override fun removeAllFlights() {
        val subscribe = repository.removeAllFlights().observeOnMain()
                .subscribe({ removed ->
                    if (removed) {
                        loadFlights()
                    } else {
                        getView()?.toastError("Записи не удалены")
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
