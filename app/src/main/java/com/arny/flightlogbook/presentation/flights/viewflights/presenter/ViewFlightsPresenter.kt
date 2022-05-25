package com.arny.flightlogbook.presentation.flights.viewflights.presenter

import com.arny.core.utils.fromSingle
import com.arny.flightlogbook.R
import com.arny.flightlogbook.domain.common.ResourcesInteractor
import com.arny.flightlogbook.domain.flights.FlightsInteractor
import com.arny.flightlogbook.domain.models.Flight
import com.arny.flightlogbook.domain.models.Result
import com.arny.flightlogbook.presentation.common.BaseMvpPresenter
import com.arny.flightlogbook.presentation.flights.viewflights.view.ViewFlightsView
import moxy.InjectViewState
import javax.inject.Inject

@InjectViewState
class ViewFlightsPresenter @Inject constructor(
    private val flightsInteractor: FlightsInteractor,
    private val resourcesInteractor: ResourcesInteractor
) : BaseMvpPresenter<ViewFlightsView>() {
    private var flights: List<Flight> = emptyList()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        getTimeInfo()
    }

    private fun getTimeInfo() {
        fromSingle { flightsInteractor.getTotalFlightsTimeInfo() }
            .subscribeFromPresenter({
                if (it is Result.Success) {
                    viewState.showTotalsInfo(it.data)
                } else {
                    viewState.showTotalsInfo(null)
                }
            })
    }

    fun loadFlights(checkAutoExport: Boolean = false, restoreScroll: Boolean) {
        viewState.viewLoadProgress(true)
        flightsInteractor.getFilterFlightsObs(checkAutoExport)
            .subscribeFromPresenter({ result ->
                viewState.viewLoadProgress(false)
                when (result) {
                    is Result.Success -> {
                        flights = result.data
                        flights.forEach { it.selected = false }
                        viewState.updateAdapter(flights, restoreScroll)
                        viewState.invalidateMenu(flights.any { it.selected }, flights.isNotEmpty())
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
        loadFlights(checkAutoExport = false, restoreScroll = false)
    }

    fun removeItem(item: Flight) {
        flightsInteractor.removeFlight(item.id)
            .subscribeFromPresenter({
                if (it) {
                    loadFlights(checkAutoExport = false, restoreScroll = false)
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
        viewState.invalidateMenu(flights.any { it.selected }, flights.isNotEmpty())
    }

    fun removeSelectedItems() {
        flightsInteractor.removeFlights(flights.filter { it.selected }.mapNotNull { it.id })
            .subscribeFromPresenter({
                if (it) {
                    loadFlights(checkAutoExport = false, restoreScroll = false)
                } else {
                    viewState.toastError(resourcesInteractor.getString(R.string.flights_not_removed))
                }
            }, {
                viewState.toastError(it.message)
            })
    }
}
