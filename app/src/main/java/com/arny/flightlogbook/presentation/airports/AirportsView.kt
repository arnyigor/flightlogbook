package com.arny.flightlogbook.presentation.airports

import com.arny.domain.models.Airport
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution

@AddToEndSingle
interface AirportsView : MvpView {
    fun setAirports(list: List<Airport>)

    @OneExecution
    fun showProgress()

    @OneExecution
    fun hideProgress()

    @OneExecution
    fun showError(message: String?)
}
