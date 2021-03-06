package com.arny.flightlogbook.presentation.airports.edit

import com.arny.core.utils.fromCallable
import com.arny.domain.airports.IAirportsInteractor
import com.arny.domain.models.Airport
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.R
import com.arny.flightlogbook.presentation.common.BaseMvpPresenter
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
        resetAllErrors()
        val codesError = icao.isBlank() && iata.isBlank()
        if (codesError) {
            when {
                codesError -> {
                    viewState.setIcaoError(R.string.error_empty_text_field)
                    viewState.setIataError(R.string.error_empty_text_field)
                    return
                }
                icao.isBlank() -> {
                    viewState.setIcaoError(R.string.error_empty_text_field)
                    return
                }
                iata.isBlank() -> {
                    viewState.setIataError(R.string.error_empty_text_field)
                    return
                }
            }
        }
        if (nameEng.isBlank()) {
            viewState.setNameEngError(R.string.error_empty_text_field)
            return
        }
        val airport = Airport(
                airportId,
                icao.trimIndent(),
                iata.trimIndent(),
                nameRus.trimIndent(),
                nameEng.trimIndent(),
                cityRus.trimIndent(),
                cityEng.trimIndent(),
                countryRus.trimIndent(),
                countryEng.trimIndent(),
                latitudeStr.toDoubleOrNull(),
                longitudeStr.toDoubleOrNull(),
                elevationStr.toDoubleOrNull(),
        )
        fromCallable { airportsInteractor.saveAirport(airport) }
                .subscribeFromPresenter({ save ->
                    if (save) {
                        viewState.setSuccessOk()
                    } else {
                        viewState.toastError(R.string.save_error, "")
                    }
                }, {
                    viewState.toastError(R.string.save_error, it.message)
                })
    }

    private fun resetAllErrors() {
        viewState.setIcaoError(null)
        viewState.setIataError(null)
        viewState.setIataError(null)
    }
}
