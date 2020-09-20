package com.arny.flightlogbook.presentation.planetypes.edit

import androidx.annotation.StringRes
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution

@AddToEndSingle
interface PlaneTypeEditView : MvpView {
    @OneExecution
    fun toastError(message: String?)
    fun setPlaneTypeName(typeName: String?)
    fun setMainPlaneType(index: Int)
    fun setRegNo(regNo: String?)

    @OneExecution
    fun toastError(@StringRes strRes: Int)

    @OneExecution
    fun onResultSuccess()
}
