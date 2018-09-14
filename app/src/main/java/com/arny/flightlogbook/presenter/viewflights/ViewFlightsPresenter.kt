package com.arny.flightlogbook.presenter.viewflights

import com.arny.flightlogbook.R
import com.arny.flightlogbook.data.Consts
import com.arny.flightlogbook.data.models.Flight
import com.arny.flightlogbook.data.source.MainRepositoryImpl
import com.arny.flightlogbook.presenter.base.BaseMvpPresenterImpl
import com.arny.flightlogbook.utils.DateTimeUtils
import com.arny.flightlogbook.utils.Utility
import io.reactivex.Observable
import io.reactivex.functions.BiFunction


class ViewFlightsPresenter : BaseMvpPresenterImpl<ViewFlightsContract.View>(), ViewFlightsContract.Presenter {
    private val repository = MainRepositoryImpl.instance

    private fun getTotals() {
        Utility.mainThreadObservable(Observable.zip<Int, Int, String>(
                repository.getFlightsTime(),
                repository.getFlightsCount(),
                BiFunction { time: Int, cnt: Int ->
                    String.format("%s %s\n%s %d",
                            repository.getString(R.string.str_totaltime),
                            DateTimeUtils.strLogTime(time),
                            repository.getString(R.string.total_records),
                            cnt)
                }))
                .subscribe({ time ->
                    getView()?.displayTotalTime(time)
                }, {
                    it.printStackTrace()
                })
    }

    override fun loadFlights() {
        val order = getFilterflights(repository.getPrefInt(Consts.PrefsConsts.CONFIG_USER_FILTER_FLIGHTS)
                ?: 0)
        Utility.mainThreadObservable(repository.getDbFlights(order))
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
        Utility.mainThreadObservable(repository.removeAllFlights())
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
            Utility.mainThreadObservable(repository.removeFlight(item.id!!))
                    .subscribe({
                        loadFlights()
                    }, {
                        it.printStackTrace()
                    })
        }
    }
}
