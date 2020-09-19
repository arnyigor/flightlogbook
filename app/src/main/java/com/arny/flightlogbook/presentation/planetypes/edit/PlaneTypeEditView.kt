package com.arny.flightlogbook.presentation.planetypes.edit

import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution

@AddToEndSingle
interface PlaneTypeEditView : MvpView {
    @OneExecution
    fun showError(message: String?)
    fun setPlaneTypeName(typeName: String?)
    fun setMainPlaneType(index: Int)
    fun setRegNo(regNo: String?)
}
