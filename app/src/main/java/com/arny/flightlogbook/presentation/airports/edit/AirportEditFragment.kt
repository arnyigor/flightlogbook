package com.arny.flightlogbook.presentation.airports.edit


import android.os.Bundle
import android.view.View
import com.arny.flightlogbook.R
import com.arny.flightlogbook.presentation.common.BaseMvpFragment
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnSave.setOnClickListener {
            presenter.saveAirport(
                    tiedtIcaoCode.text.toString(),
                    tiEdtIataCode.text.toString()
            )
        }
    }
}
