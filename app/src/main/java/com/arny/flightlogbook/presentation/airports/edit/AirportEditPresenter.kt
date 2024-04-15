package com.arny.flightlogbook.presentation.airports.edit

import com.arny.flightlogbook.R
import com.arny.flightlogbook.data.models.Airport
import com.arny.flightlogbook.domain.airports.IAirportsInteractor
import com.arny.flightlogbook.presentation.mvp.BaseMvpPresenter
import io.reactivex.Single
import moxy.InjectViewState
import javax.inject.Inject

@InjectViewState
class AirportEditPresenter @Inject constructor(
    private val airportsInteractor: IAirportsInteractor
) : BaseMvpPresenter<AirportEditView>() {
    var airportId: Long? = null
        set(value) {
            field = if (value != -1L) value else null
        }

    override fun onFirstViewAttach() {
        airportId?.let {
            Single.fromCallable { airportsInteractor.getAirport(it) }
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
            id = airportId,
            icao = icao.trimIndent(),
            iata = iata.trimIndent(),
            nameRus = nameRus.trimIndent(),
            nameEng = nameEng.trimIndent(),
            cityRus = cityRus.trimIndent(),
            cityEng = cityEng.trimIndent(),
            countryRus = countryRus.trimIndent(),
            countryEng = countryEng.trimIndent(),
            latitude = latitudeStr.toDoubleOrNull(),
            longitude = longitudeStr.toDoubleOrNull(),
            elevation = elevationStr.toDoubleOrNull(),
        )
        Single.fromCallable { airportsInteractor.saveAirport(airport) }
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
