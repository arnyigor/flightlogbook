package com.arny.flightlogbook.presentation.airports.edit

import androidx.annotation.StringRes
import com.arny.domain.models.Airport
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution

@OneExecution
interface AirportEditView : MvpView {
    @AddToEndSingle
    fun setAirport(airport: Airport)
    fun toastError(@StringRes errorRes: Int, message: String?)
    fun setSuccessOk()
    fun setIcaoError(@StringRes errorRes: Int?)
    fun setNameEngError(@StringRes errorRes: Int?)
    fun setIataError(@StringRes errorRes: Int?)
}
