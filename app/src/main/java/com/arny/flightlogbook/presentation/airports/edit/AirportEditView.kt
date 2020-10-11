package com.arny.flightlogbook.presentation.airports.edit

import androidx.annotation.StringRes
import com.arny.domain.models.Airport
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution

@AddToEndSingle
interface AirportEditView : MvpView {
    fun setAirport(airport: Airport)
    @OneExecution
    fun toastError(@StringRes errorRes: Int, message: String?)

    @OneExecution
    fun setSuccessOk()
}
