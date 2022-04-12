package com.arny.flightlogbook.presentation.airports.edit

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResult
import com.arny.core.CONSTS
import com.arny.core.utils.getExtra
import com.arny.core.utils.toastError
import com.arny.flightlogbook.R
import com.arny.flightlogbook.databinding.FAirportEditBinding
import com.arny.flightlogbook.domain.models.Airport
import com.arny.flightlogbook.presentation.common.BaseMvpFragment
import com.arny.flightlogbook.presentation.main.AppRouter
import dagger.android.support.AndroidSupportInjection
import moxy.ktx.moxyPresenter
import javax.inject.Inject
import javax.inject.Provider

class AirportEditFragment : BaseMvpFragment(), AirportEditView {
    companion object {
        fun getInstance(bundle: Bundle? = null) = AirportEditFragment().apply {
            bundle?.let { arguments = it }
        }
    }

    private lateinit var binding: FAirportEditBinding

    @Inject
    lateinit var presenterProvider: Provider<AirportEditPresenter>

    private val presenter by moxyPresenter { presenterProvider.get() }

    private var appRouter: AppRouter? = null

    override fun getTitle(): String = getString(R.string.edit_airport)

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        appRouter = context as? AppRouter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        presenter.airportId = arguments?.getExtra<Long>(CONSTS.EXTRAS.EXTRA_AIRPORT_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FAirportEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            tiedtIcaoCode.doAfterTextChanged { tilIcaoCode.error = null }
            tiEdtIataCode.doAfterTextChanged { tilIataCode.error = null }
            tiedtNameEng.doAfterTextChanged { tilNameEng.error = null }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> {
                with(binding) {
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
        }
        return true
    }

    override fun setAirport(airport: Airport) {
        with(binding) {
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
    }

    override fun toastError(errorRes: Int, message: String?) {
        toastError(getString(errorRes, message))
    }

    override fun setIcaoError(errorRes: Int?) {
        binding.tilIcaoCode.error = errorRes?.let { getString(it) }
    }

    override fun setIataError(errorRes: Int?) {
        binding.tilIataCode.error = errorRes?.let { getString(it) }
    }

    override fun setNameEngError(errorRes: Int?) {
        binding.tilNameEng.error = errorRes?.let { getString(it) }
    }

    override fun setSuccessOk() {
        setFragmentResult(
            CONSTS.REQUESTS.REQUEST_AIRPORT_EDIT,
            bundleOf(CONSTS.EXTRAS.EXTRA_ACTION_EDIT_AIRPORT to true)
        )
        requireActivity().onBackPressed()
    }
}
