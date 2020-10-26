package com.arny.flightlogbook.presentation.airports.edit


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.widget.doAfterTextChanged
import com.arny.core.CONSTS
import com.arny.core.utils.getExtra
import com.arny.core.utils.toastError
import com.arny.domain.models.Airport
import com.arny.flightlogbook.R
import com.arny.flightlogbook.presentation.common.BaseMvpFragment
import com.arny.flightlogbook.presentation.main.AppRouter
import kotlinx.android.synthetic.main.f_airport_edit.*
import moxy.ktx.moxyPresenter

class AirportEditFragment : BaseMvpFragment(), AirportEditView {

    companion object {
        fun getInstance(bundle: Bundle? = null) = AirportEditFragment().apply {
            bundle?.let { arguments = it }
        }
    }

    private val presenter by moxyPresenter { AirportEditPresenter() }

    private var appRouter: AppRouter? = null

    override fun getLayoutId(): Int = R.layout.f_airport_edit

    override fun getTitle(): String = getString(R.string.edit_airport)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        appRouter = context as? AppRouter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        presenter.airportId = arguments?.getExtra<Long>(CONSTS.EXTRAS.EXTRA_AIRPORT_ID)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tiedtIcaoCode.doAfterTextChanged { tilIcaoCode.error = null }
        tiEdtIataCode.doAfterTextChanged { tilIataCode.error = null }
        tiedtNameEng.doAfterTextChanged { tilNameEng.error = null }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> {
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
        return true
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

    override fun setIcaoError(errorRes: Int?) {
        tilIcaoCode.error = errorRes?.let { getString(it) }
    }

    override fun setIataError(errorRes: Int?) {
        tilIataCode.error = errorRes?.let { getString(it) }
    }

    override fun setNameEngError(errorRes: Int?) {
        tilNameEng.error = errorRes?.let { getString(it) }
    }

    override fun setSuccessOk() {
        appRouter?.onReturnResult(Intent(), Activity.RESULT_OK)
    }
}
