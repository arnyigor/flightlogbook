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
    fun showEmptyIcao(@StringRes errorRes: Int)
    fun showEmptyNameEng(@StringRes errorRes: Int)
    fun showEmptyIata(@StringRes errorRes: Int)
}
