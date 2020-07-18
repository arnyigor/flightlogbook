package com.arny.flightlogbook.presentation.customfields.list.view

import com.arny.flightlogbook.customfields.models.CustomField
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(value = AddToEndSingleStrategy::class)
interface CustomFieldsListView : MvpView {
    fun showProgress(show: Boolean)
    fun showEmptyView(show: Boolean)
    fun showList(list: List<CustomField>)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun toastError(message: String?)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun navigateToFieldEdit(id: Long? = null)

}