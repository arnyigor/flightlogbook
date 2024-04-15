package com.arny.flightlogbook.presentation.flights.viewflights.presenter

import com.arny.flightlogbook.R
import com.arny.flightlogbook.data.models.AppResult
import com.arny.flightlogbook.data.models.Flight
import com.arny.flightlogbook.domain.common.ResourcesInteractor
import com.arny.flightlogbook.domain.flights.FlightsInteractor
import com.arny.flightlogbook.presentation.flights.viewflights.view.ViewFlightsView
import com.arny.flightlogbook.presentation.mvp.BaseMvpPresenter
import io.reactivex.Single
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
        Single.fromCallable { flightsInteractor.getTotalFlightsTimeInfo() }
            .subscribeFromPresenter({
                if (it is AppResult.Success) {
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
                    is AppResult.Success -> {
                        flights = result.data
                        flights.forEach { it.selected = false }
                        viewState.updateAdapter(flights, restoreScroll)
                        viewState.invalidateMenu(flights.any { it.selected }, flights.isNotEmpty())
                        viewState.showEmptyView(flights.isEmpty())
                        getTimeInfo()
                    }

                    is AppResult.Error -> {
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
