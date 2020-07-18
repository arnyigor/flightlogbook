package com.arny.flightlogbook.presentation.customfields.edit.view

import androidx.annotation.StringRes
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

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showError(@StringRes strRes: Int)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onReturnBack()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showResult(@StringRes strRes: Int)
    fun showProgress(show: Boolean)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun hideKeyboard()
}
