package com.arny.flightlogbook.presentation.planetypes

import com.arny.domain.models.PlaneType
import moxy.MvpView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

/**
 *Created by Sedoy on 09.07.2019
 */
@StateStrategyType(value = OneExecutionStateStrategy::class)
interface PlaneTypesView : MvpView {
    fun updateAdapter(list: List<PlaneType>)
    fun showEditDialog(type: PlaneType, position: Int)
    fun toastSuccess(string: String)
    fun showRemoveDialog(item: PlaneType, position: Int)
    fun toastError(msg: String?)
    fun notifyItemChanged(position: Int)
    fun setEmptyViewVisible(vis: Boolean)
}