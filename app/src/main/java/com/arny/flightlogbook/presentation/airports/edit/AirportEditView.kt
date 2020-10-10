package com.arny.flightlogbook.presentation.airports.edit

import com.arny.domain.models.Airport
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle

@AddToEndSingle
interface AirportEditView : MvpView {
    fun setAirport(airport: Airport)
}
