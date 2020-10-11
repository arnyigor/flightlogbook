package com.arny.flightlogbook.presentation.airports.edit


import android.os.Bundle
import android.view.View
import com.arny.domain.models.Airport
import com.arny.flightlogbook.R
import com.arny.flightlogbook.constants.CONSTS
import com.arny.flightlogbook.presentation.common.BaseMvpFragment
import com.arny.helpers.utils.getExtra
import kotlinx.android.synthetic.main.f_airport_edit.*
import moxy.ktx.moxyPresenter

class AirportEditFragment : BaseMvpFragment(), AirportEditView {

    companion object {
        fun getInstance(bundle: Bundle? = null) = AirportEditFragment().apply {
            bundle?.let { arguments = it }
        }
    }

    private val presenter by moxyPresenter { AirportEditPresenter() }

    override fun getLayoutId(): Int = R.layout.f_airport_edit

    override fun getTitle(): String = getString(R.string.edit_airport)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.airportId = arguments?.getExtra<Long>(CONSTS.EXTRAS.EXTRA_AIRPORT_ID)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnSave.setOnClickListener {
            presenter.saveAirport(
                    tiedtIcaoCode.text.toString(),
                    tiEdtIataCode.text.toString(),
                    tiedtNameRus.text.toString(),
                    tiedtNameEng.text.toString(),
                    tiedtCityRus.text.toString(),
                    tiedtCityEng.text.toString(),
                    tiedtCountryRus.text.toString(),
                    tiedtCountryEng.text.toString(),
                    tiedtLatitude.text.toString(),
                    tiedtLongitude.text.toString(),
                    tiedtElevation.text.toString()
            )
        }
    }

    override fun setAirport(airport: Airport) {
        tiedtIcaoCode.setText(airport.icao)
        tiEdtIataCode.setText(airport.iata)
        tiedtNameRus.setText(airport.nameRus)
        tiedtNameEng.setText(airport.nameEng)
        tiedtCityRus.setText(airport.cityRus)
        tiedtCityEng.setText(airport.cityEng)
        tiedtCountryRus.setText(airport.cityRus)
        tiedtCountryEng.setText(airport.cityEng)
        tiedtLatitude.setText(airport.latitude.toString())
        tiedtLongitude.setText(airport.longitude.toString())
        tiedtElevation.setText(airport.elevation.toString())
    }
}
