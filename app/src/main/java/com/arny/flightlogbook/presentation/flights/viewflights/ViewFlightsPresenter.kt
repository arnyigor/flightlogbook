package com.arny.flightlogbook.presentation.flights.viewflights

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.arny.domain.common.CommonUseCase
import com.arny.domain.flights.FlightsUseCase
import com.arny.domain.models.Flight
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.R
import com.arny.helpers.utils.addTo
import com.arny.helpers.utils.observeOnMain
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject


@InjectViewState
class ViewFlightsPresenter : MvpPresenter<ViewFlightsView>() {
    private val compositeDisposable = CompositeDisposable()
    @Inject
    lateinit var flightsUseCase: FlightsUseCase
    @Inject
    lateinit var commonUseCase: CommonUseCase

    init {
        FlightApp.appComponent.inject(this)
    }

    override fun detachView(view: ViewFlightsView?) {
        super.detachView(view)
        compositeDisposable.clear()
    }

    fun loadFlights() {
        flightsUseCase.getFilterflights()
                .observeOnMain()
                .subscribe({
                    if (it.isNotEmpty()) {
                        viewState?.updateAdapter(it)
                    }
                }, {
                    viewState?.toastError(it.message)
                })
                .addTo(compositeDisposable)
    }

    fun removeAllFlights() {
        flightsUseCase.removeAllFlights()
                .observeOnMain()
                .subscribe({ removed ->
                    if (removed) {
                        loadFlights()
                    } else {
                        viewState?.toastError(commonUseCase.getString(R.string.flights_not_removed))
                    }
                }, {
                    it.printStackTrace()
                }).addTo(compositeDisposable)
    }

    fun removeItem(item: Flight?) {
        flightsUseCase.removeFlight(item?.id)
                .observeOnMain()
                .subscribe({
                    loadFlights()
                }, {
                    viewState?.toastError(it.message)
                }).addTo(compositeDisposable)
    }
}
