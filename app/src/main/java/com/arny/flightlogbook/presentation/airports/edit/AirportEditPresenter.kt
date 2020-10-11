package com.arny.flightlogbook.presentation.airports.edit

import com.arny.domain.airports.IAirportsInteractor
import com.arny.domain.models.Airport
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

    fun saveAirport(
            icao: String,
            iata: String,
            nameRus: String,
            nameEng: String,
            cityRus: String,
            cityEng: String,
            countryRus: String,
            countryEng: String,
            latitudeStr: String,
            longitudeStr: String,
            elevationStr: String
    ) {
        val airport = Airport(
                airportId,
                icao,
                iata,
                nameRus,
                nameEng,
                cityRus,
                cityEng,
                countryRus,
                countryEng,
                latitudeStr.toDoubleOrNull(),
                longitudeStr.toDoubleOrNull(),
                elevationStr.toDoubleOrNull(),
        )
        airportsInteractor.saveAirport(airport)
    }
}
