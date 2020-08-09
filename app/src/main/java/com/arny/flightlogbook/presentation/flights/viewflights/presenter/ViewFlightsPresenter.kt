package com.arny.flightlogbook.presentation.flights.viewflights.presenter

import com.arny.domain.common.ResourcesInteractor
import com.arny.domain.flights.FlightsInteractor
import com.arny.domain.models.Flight
import com.arny.domain.models.Result
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.R
import com.arny.flightlogbook.presentation.common.BaseMvpPresenter
import com.arny.flightlogbook.presentation.flights.viewflights.view.ViewFlightsView
import moxy.InjectViewState
import javax.inject.Inject


@InjectViewState
class ViewFlightsPresenter : BaseMvpPresenter<ViewFlightsView>() {
    private var flights: List<Flight> = emptyList()

    @Inject
    lateinit var flightsInteractor: FlightsInteractor

    @Inject
    lateinit var resourcesInteractor: ResourcesInteractor

    init {
        FlightApp.appComponent.inject(this)
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        getTimeInfo()
    }

    fun getTimeInfo() {
        flightsInteractor.getTotalflightsTimeInfo()
                .subscribeFromPresenter({
                    if (it is Result.Success) {
                        viewState.showTotalsInfo(it.data)
                    } else {
                        viewState.showTotalsInfo(null)
                    }
                })
    }

    fun loadFlights(checkAutoExport: Boolean = false) {
        viewState.viewLoadProgress(true)
        flightsInteractor.getFilterFlightsObs(checkAutoExport)
                .subscribeFromPresenter({ result ->
                    viewState.viewLoadProgress(false)
                    when (result) {
                        is Result.Success -> {
                            flights = result.data
                            flights.forEach { it.selected = false }
                            viewState.updateAdapter(flights)
                            viewState.invalidateMenuSelected(flights.any { it.selected })
                            viewState.showEmptyView(flights.isEmpty())
                            getTimeInfo()
                        }
                        is Result.Error -> {
                            viewState.showError(result.exception?.message)
                        }
                    }
                }, {
                    it.printStackTrace()
                    viewState.viewLoadProgress(false)
                    viewState.toastError(it.message)
                })
    }

    fun changeOrder(orderType: Int) {
        flightsInteractor.setFlightsOrder(orderType)
        loadFlights(false)
    }

    fun removeItem(item: Flight) {
        flightsInteractor.removeFlight(item.id)
                .subscribeFromPresenter({
                    if (it) {
                        loadFlights(false)
                    } else {
                        viewState.toastError(resourcesInteractor.getString(R.string.flight_not_removed))
                    }
                }, {
                    viewState.toastError(it.message)
                })
    }

    fun onFlightSelect(position: Int, item: Flight) {
        item.selected = !item.selected
        viewState.invalidateAdapter(position)
        viewState.invalidateMenuSelected(flights.any { it.selected })
    }

    fun removeSelectedItems() {
        flightsInteractor.removeFlights(flights.filter { it.selected }.mapNotNull { it.id })
                .subscribeFromPresenter({
                    if (it) {
                        loadFlights(false)
                    } else {
                        viewState.toastError(resourcesInteractor.getString(R.string.flights_not_removed))
                    }
                }, {
                    viewState.toastError(it.message)
                })
    }
}
