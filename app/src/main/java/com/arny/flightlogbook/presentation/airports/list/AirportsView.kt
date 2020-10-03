package com.arny.flightlogbook.presentation.airports.list

import com.arny.domain.models.Airport
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution

@OneExecution
interface AirportsView : MvpView {
    @AddToEndSingle
    fun setAirports(list: List<Airport>)
    fun showProgress()
    fun hideProgress()
    fun showError(message: String?)
}
