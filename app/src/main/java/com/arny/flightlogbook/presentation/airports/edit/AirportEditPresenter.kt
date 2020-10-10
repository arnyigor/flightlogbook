package com.arny.flightlogbook.presentation.airports.edit

import com.arny.domain.airports.IAirportsInteractor
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.presentation.common.BaseMvpPresenter
import com.arny.helpers.utils.fromCallable
import moxy.InjectViewState
import javax.inject.Inject

@InjectViewState
class AirportEditPresenter : BaseMvpPresenter<AirportEditView>() {
    init {
        FlightApp.appComponent.inject(this)
    }


    @Inject
    lateinit var airportsInteractor: IAirportsInteractor

    var airportId: Long? = null

    override fun onFirstViewAttach() {
        airportId?.let {
            fromCallable { airportsInteractor.getAirport(it) }
                    .subscribeFromPresenter({
                        val airport = it.value
                        if (airport != null) {
                            viewState.setAirport(airport)
                        }
                    })
        }
    }

    fun saveAirport() {
    }
}
