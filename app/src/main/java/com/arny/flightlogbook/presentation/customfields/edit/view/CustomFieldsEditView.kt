package com.arny.flightlogbook.presentation.customfields.edit.view

import com.arny.flightlogbook.customfields.models.CustomFieldType
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface CustomFieldsEditView : MvpView {
    fun setTitle(name: String?)
    fun setType(type: CustomFieldType?)
    fun showNameError(stringRes: Int?)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onResultOk()
}
