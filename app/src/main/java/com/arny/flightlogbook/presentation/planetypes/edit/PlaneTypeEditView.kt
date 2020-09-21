package com.arny.flightlogbook.presentation.planetypes.edit

import androidx.annotation.StringRes
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution

@AddToEndSingle
interface PlaneTypeEditView : MvpView {
    @OneExecution
    fun showError(message: String?)

    @OneExecution
    fun showError(strRes: Int)
    fun setPlaneTypeName(typeName: String?)
    fun setMainPlaneType(index: Int)
    fun setRegNo(regNo: String?)
    fun showTitleError(@StringRes strRes: Int)
    fun showRegNoError(@StringRes strRes: Int)

    @OneExecution
    fun setResultOk(id: Long)
}
