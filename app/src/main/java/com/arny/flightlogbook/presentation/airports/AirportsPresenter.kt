package com.arny.flightlogbook.presentation.airports

import com.arny.domain.airports.IAirportsInteractor
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.presentation.common.BaseMvpPresenter
import com.arny.helpers.utils.fromCallable
import moxy.InjectViewState
import javax.inject.Inject

@InjectViewState
class AirportsPresenter : BaseMvpPresenter<AirportsView>() {

    init {
        FlightApp.appComponent.inject(this)
    }

    @Inject
    lateinit var airportsInteractor: IAirportsInteractor

    override fun onFirstViewAttach() {
        fromCallable { airportsInteractor.getAirports() }
                .doOnSubscribe { viewState.showProgress() }
                .doFinally { viewState.hideProgress() }
                .subscribeFromPresenter({
                    viewState.setAirports(it)
                }, {
                    viewState.showError(it.message)
                })
    }
}
