package com.arny.flightlogbook.presentation.flights.viewflights.presenter

import com.arny.domain.common.ResourcesInteractor
import com.arny.domain.flights.FlightsInteractor
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.presentation.flights.viewflights.view.ViewFlightsView
import com.arny.helpers.utils.CompositeDisposableComponent
import io.reactivex.disposables.CompositeDisposable
import moxy.InjectViewState
import moxy.MvpPresenter
import javax.inject.Inject


@InjectViewState
class ViewFlightsPresenter : MvpPresenter<ViewFlightsView>(),CompositeDisposableComponent {
    override val compositeDisposable = CompositeDisposable()
    @Inject
    lateinit var flightsInteractor: FlightsInteractor
    @Inject
    lateinit var resourcesInteractor: ResourcesInteractor

    init {
        FlightApp.appComponent.inject(this)
    }

    override fun detachView(view: ViewFlightsView?) {
        super.detachView(view)
        resetCompositeDisposable()
    }

    fun loadFlights() {
        viewState?.viewLoadProgress(true)
        flightsInteractor.getFilterFlightsObs()
                .observeSubscribeAdd({
                    viewState?.updateAdapter(it)
                    viewState?.showEmptyView(it.isEmpty())
                    viewState?.viewLoadProgress(false)
                }, {
                    viewState?.viewLoadProgress(false)
                    viewState?.toastError(it.message)
                })
    }

    fun changeOrder(orderType: Int) {
        flightsInteractor.setFlightsOrder(orderType)
        loadFlights()
    }
}
