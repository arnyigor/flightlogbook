package com.arny.flightlogbook.presentation.airports.edit


import android.content.Intent
import android.os.Bundle
import android.view.View
import com.arny.domain.models.Airport
import com.arny.flightlogbook.R
import com.arny.flightlogbook.constants.CONSTS
import com.arny.flightlogbook.presentation.common.BaseMvpFragment
import com.arny.flightlogbook.presentation.common.FragmentContainerActivity
import com.arny.helpers.utils.getExtra
import com.arny.helpers.utils.toastError
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
        airport.icao?.let { tiedtIcaoCode.setText(it) }
        airport.iata?.let { tiEdtIataCode.setText(it) }
        airport.nameRus?.let { tiedtNameRus.setText(it) }
        airport.nameEng?.let { tiedtNameEng.setText(it) }
        airport.cityRus?.let { tiedtCityRus.setText(it) }
        airport.cityEng?.let { tiedtCityEng.setText(it) }
        airport.countryRus?.let { tiedtCountryRus.setText(it) }
        airport.countryEng?.let { tiedtCountryEng.setText(it) }
        airport.latitude?.let { tiedtLatitude.setText(it.toString()) }
        airport.longitude?.let { tiedtLongitude.setText(it.toString()) }
        airport.elevation?.let { tiedtElevation.setText(it.toString()) }
    }

    override fun toastError(errorRes: Int, message: String?) {
        toastError(getString(errorRes, message))
    }

    override fun setSuccessOk() {
        val requireActivity = requireActivity()
        if (requireActivity is FragmentContainerActivity) {
            requireActivity.onSuccess(Intent())
            requireActivity.onBackPressed()
        }
    }
}
