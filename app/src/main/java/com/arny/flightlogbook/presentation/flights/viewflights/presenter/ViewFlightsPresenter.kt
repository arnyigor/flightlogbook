package com.arny.flightlogbook.presentation.flights.viewflights.presenter

import com.arny.domain.common.ResourcesInteractor
import com.arny.domain.flights.FlightsInteractor
import com.arny.domain.models.Result
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.presentation.common.BaseMvpPresenter
import com.arny.flightlogbook.presentation.flights.viewflights.view.ViewFlightsView
import moxy.InjectViewState
import javax.inject.Inject


@InjectViewState
class ViewFlightsPresenter : BaseMvpPresenter<ViewFlightsView>() {
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
                .subscribeFromPresenter({
                    viewState.viewLoadProgress(false)
                    when (it) {
                        is Result.Success -> {
                            val data = it.data
                            viewState.updateAdapter(data)
                            viewState.showEmptyView(data.isEmpty())
                        }
                        is Result.Error -> {
                            viewState.showError(it.exception?.message)
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
}
