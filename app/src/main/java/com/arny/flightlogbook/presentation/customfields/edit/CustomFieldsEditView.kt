package com.arny.flightlogbook.presentation.customfields.edit

import androidx.annotation.StringRes
import com.arny.flightlogbook.customfields.models.CustomFieldType
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution

@AddToEndSingle
interface CustomFieldsEditView : MvpView {
    fun setName(name: String?)
    fun setType(type: CustomFieldType?)
    fun setDefaultChecked(showByDefault: Boolean)
    fun showNameError(stringRes: Int?)

    @OneExecution
    fun onResultOk()

    @OneExecution
    fun showError(@StringRes strRes: Int)

    @OneExecution
    fun showResult(@StringRes strRes: Int)
    fun showProgress(show: Boolean)
    fun setAddTimeChecked(checked: Boolean)
    fun setChBoxAddTimeVisible(visible: Boolean)
}
