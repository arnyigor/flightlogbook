package com.arny.flightlogbook.presentation.customfields.list.view

import com.arny.flightlogbook.customfields.models.CustomField
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface CustomFieldsListView : MvpView {
    @StateStrategyType(value = AddToEndSingleStrategy::class)
    fun showProgress(show: Boolean)

    @StateStrategyType(value = AddToEndSingleStrategy::class)
    fun showEmptyView(show: Boolean)
    @StateStrategyType(value = AddToEndSingleStrategy::class)
    fun showList(list: List<CustomField>)
    fun toastError(message: String?)
    fun navigateToFieldEdit(id: Long? = null)

}