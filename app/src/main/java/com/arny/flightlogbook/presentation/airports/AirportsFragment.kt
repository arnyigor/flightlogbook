package com.arny.flightlogbook.presentation.airports


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arny.domain.models.Airport
import com.arny.flightlogbook.R
import moxy.MvpAppCompatFragment
import moxy.ktx.moxyPresenter

class AirportsFragment : MvpAppCompatFragment(), AirportsView {
    companion object {
        fun getInstance() = AirportsFragment()
    }

    private lateinit var airportsAdapter: AirportsAdapter
    private val presenter by moxyPresenter { AirportsPresenter() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.f_airports, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        airportsAdapter = AirportsAdapter()
    }

    override fun setAirports(list: List<Airport>) {
        TODO("Not yet implemented")
    }

    override fun showProgress() {
        TODO("Not yet implemented")
    }

    override fun hideProgress() {
        TODO("Not yet implemented")
    }

    override fun showError(message: String?) {
        TODO("Not yet implemented")
    }
}
