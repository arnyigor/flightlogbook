package com.arny.flightlogbook.presentation.flights.viewflights

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.arny.domain.common.CommonUseCase
import com.arny.domain.flights.FlightsUseCase
import com.arny.flightlogbook.FlightApp
import com.arny.helpers.utils.CompositeDisposableComponent
import com.arny.helpers.utils.addTo
import com.arny.helpers.utils.observeOnMain
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject


@InjectViewState
class ViewFlightsPresenter : MvpPresenter<ViewFlightsView>(),CompositeDisposableComponent {
    override val compositeDisposable = CompositeDisposable()
    @Inject
    lateinit var flightsUseCase: FlightsUseCase
    @Inject
    lateinit var commonUseCase: CommonUseCase

    init {
        FlightApp.appComponent.inject(this)
    }

    override fun detachView(view: ViewFlightsView?) {
        super.detachView(view)
        resetCompositeDisposable()
    }

     private fun getTotalsInfo(){
        flightsUseCase.getTotalflightsTimeInfo()
                .observeOnMain()
                .subscribe({
                    viewState?.showTotalsInfo(it)
                },{
                    it.printStackTrace()
                })
                .addTo(compositeDisposable)
    }

    fun loadFlights() {
        viewState?.viewLoadProgress(true)
        flightsUseCase.getFilterflightsObs()
                .observeSubscribeAdd({
                    viewState?.updateAdapter(it)
                    viewState?.showEmptyView(it.isEmpty())
                    viewState?.viewLoadProgress(false)
                }, {
                    viewState?.viewLoadProgress(false)
                    viewState?.toastError(it.message)
                })
    }

    /* fun removeAllFlightsObs() {
         flightsUseCase.removeAllFlightsObs()
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
     }*/

    /*fun removeItem(item: Flight?) {
        flightsUseCase.removeFlight(item?.id)
                .observeOnMain()
                .subscribe({
                    loadFlights()
                }, {
                    viewState?.toastError(it.message)
                }).addTo(compositeDisposable)
    }*/

    fun changeOrder(orderType: Int) {
        flightsUseCase.setFlightsOrder(orderType)
        loadFlights()
    }
}
